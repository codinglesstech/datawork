package tech.codingless.standalone.datawork.v1.service.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import tech.codingless.standalone.datawork.helper.MemoryObservationHelper;
import tech.codingless.standalone.datawork.util.BooleanUtil;
import tech.codingless.standalone.datawork.util.SnowFlakeNumberUtil;
import tech.codingless.standalone.datawork.v1.data.JobDef;
import tech.codingless.standalone.datawork.v1.data.JobProcess;
import tech.codingless.standalone.datawork.v1.data.JobStat;
import tech.codingless.standalone.datawork.v1.data.ProcessResponse;
import tech.codingless.standalone.datawork.v1.service.JobExecuteService;
import tech.codingless.standalone.datawork.v1.service.JobProcessService;

@Slf4j
@Service
public class JobExecuteServiceImpl implements JobExecuteService {

	private Map<String, JobProcessService> services = new HashMap<>();
	private ConcurrentHashMap<String, JobStat> logstats = new ConcurrentHashMap<>();
	private ConcurrentHashMap<String, Boolean> RUNNING_STATUS = new ConcurrentHashMap<>();

	@Autowired
	public void addJobProcessService(JobProcessService[] serviceList) {
		for (JobProcessService service : serviceList) {
			services.put(service.command().toLowerCase(), service);
		}
	}

	@Override
	public JobStat stat(String jobId) {
		return logstats.get(jobId);
	}

	@Override
	public boolean isrunning(String jobId) {
		Boolean status = RUNNING_STATUS.get(jobId);
		return BooleanUtil.isTrue(status);
	}

	@Override
	public void execute(JobDef jobDef, Map<String, Object> context, ProcessResponse response) {
		if (context == null) {
			context = new HashMap<>();
		}
		long t = System.currentTimeMillis();
		try {
			RUNNING_STATUS.put(jobDef.getId(), true);
			context.put("response", response);
			context.put("_template_", jobDef.getTemplate());
			String runid = "";
			Object reqId = context.get("_REQ_ID_");
			if (reqId != null && reqId instanceof String && ((String) reqId).matches("^[0-9a-zA-Z]{10,50}$")) {
				runid = (String) reqId;
			} else {
				runid = "T" + Long.toString(SnowFlakeNumberUtil.nextId());
				context.put("_REQ_ID_", runid);
			}
			MDC.put("TRACEID", runid);
			MDC.put("JOBID", jobDef.getId());
			log.info("Start:{}", jobDef);
			boolean allsuccess = true;
			for (JobProcess process : jobDef.getProcess()) {
				JobProcessService service = services.get(process.getCommand().toLowerCase());
				if (service == null) {
					log.warn("No Implement For Process Type Of {} ,Skip", process.getCommand());
					continue;
				}
				try {
					// log.info("Start Process {} service:{}", process.getCommand(), service);
					if (log.isDebugEnabled()) {
						log.debug("Start  Process {}   service:{}", process.getCommand(), service);
					}
					boolean success = service.execute(runid, jobDef, process, context);
					if (!success && BooleanUtil.isTrue(jobDef.getStrict())) {
						log.error("break when one process return false on strict mode");
						break;
					}
					if (log.isDebugEnabled()) {
						log.debug("End  Process {}   With Status:{}", process.getCommand(), success);
					}
					// log.info("End Process {} With Status:{}", process.getCommand(), success);
				} catch (Exception e) {
					log.error("Execute Job Fail", e);
					allsuccess = false;
					break;
				}

			}
			MemoryObservationHelper.increment(context);
			long cost = System.currentTimeMillis() - t;
			reportstatus(allsuccess, jobDef, cost);
		} catch (Throwable e) {
			long cost = System.currentTimeMillis() - t;
			reportstatus(false, jobDef, cost);
			log.info("Execute Job Fail :" + jobDef.getId(), e);
		} finally {
			log.info("End:{}", jobDef.getId());
			MDC.remove("TRACEID");
			MDC.remove("JOBID");
			RUNNING_STATUS.remove(jobDef.getId());
		}

	}

	private void reportstatus(boolean success, JobDef jobDef, long cost) {
		JobStat status = logstats.get(jobDef.getId());
		if (status == null) {
			synchronized (logstats) {
				status = logstats.get(jobDef.getId());
				if (status == null) {
					status = new JobStat();
					logstats.put(jobDef.getId(), status);
				}
			}
		}
		if (success) {
			status.success(cost);
		} else {
			status.fail(cost);
		}
	}

	@Override
	public void execute(JobDef jobDef, Map<String, Object> context) {
		this.execute(jobDef, context, new ProcessResponse());
	}

}
