package tech.codingless.standalone.datawork.v1.api;

import java.io.File;
import java.io.FileInputStream;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson2.JSONObject;
import com.sun.management.OperatingSystemMXBean;

import lombok.extern.slf4j.Slf4j;
import tech.codingless.standalone.datawork.config.Authed;
import tech.codingless.standalone.datawork.config.JobsConfig;
import tech.codingless.standalone.datawork.util.BooleanUtil;
import tech.codingless.standalone.datawork.util.StringUtil;
import tech.codingless.standalone.datawork.v1.data.JobDef;
import tech.codingless.standalone.datawork.v1.data.JobStat;
import tech.codingless.standalone.datawork.v1.service.JobDefService;
import tech.codingless.standalone.datawork.v1.service.JobExecuteService;
import tech.codingless.standalone.datawork.v1.service.JobRunnerService;

@Slf4j
@RestController
@RequestMapping(value = "/v1")
public class ManagerApi {

	@Resource
	private JobRunnerService jobRunnerService;
	@Resource
	private JobDefService jobDefService;

	@Resource
	private JobsConfig jobsConfig;

	@Resource
	private JobExecuteService jobExecuteService;

	@Authed
	@GetMapping(value = "/scheduled/reload")
	public String scheduledReload() {
		log.info("scheduledReload");
		return "hello" + new Date();
	}

	@Authed
	@GetMapping(value = "/conf/reload")
	public String confReload() throws Exception {

		if (StringUtil.isNotEmpty(jobsConfig.getUpscript())) {
			String cmd = jobsConfig.getUpscript().trim();
			log.info("Need Run Cmd:{}", cmd);
			Process process = Runtime.getRuntime().exec(cmd);
			String rs = IOUtils.toString(process.getInputStream(), "utf-8");
			log.info("cmd result:{}", rs);
		}

		URL url = jobDefService.getClass().getResource("/");
		File projectfile = new File(url.getFile()).getParentFile().getParentFile();
		log.info("Conf Dir:{}", new File(projectfile, "conf").getAbsolutePath());
		List<JobDef> jobs = jobDefService.loadFromLocalFile(new File(jobsConfig.getDir()).getAbsolutePath());
		jobDefService.reload(jobs);
		JSONObject response = new JSONObject();
		response.put("success", true);

		return response.toString();
	}

	@Authed
	@GetMapping(value = "/conf/prereload")
	public String confPreReload() throws Exception {

		if (StringUtil.isNotEmpty(jobsConfig.getUpscript())) {
			String cmd = jobsConfig.getUpscript().trim();
			log.info("Need Run Cmd:{}", cmd);
			Process process = Runtime.getRuntime().exec(cmd);
			String rs = IOUtils.toString(process.getInputStream(), "utf-8");
			log.info("cmd result:{}", rs);
		}

		URL url = jobDefService.getClass().getResource("/");
		File projectfile = new File(url.getFile()).getParentFile().getParentFile();
		log.info("Conf Dir:{}", new File(projectfile, "conf").getAbsolutePath());
		List<JobDef> jobs = jobDefService.loadFromLocalFile(new File(jobsConfig.getDir()).getAbsolutePath());
		jobDefService.prereload(jobs);
		JSONObject response = new JSONObject();
		response.put("success", true);
		return response.toString();
	}

	@Authed
	@GetMapping(value = "/conf/mergePreJob")
	public String mergePreJob(String id) throws Exception {
		/**
		 * 指定JOB，合并预发
		 */
		JobDef job = jobDefService.getpre(id);
		if (job != null) {
			jobDefService.reload(List.of(job));
		}
		JSONObject response = new JSONObject();
		response.put("success", true);
		return response.toString();
	}

	@Authed
	@GetMapping(value = "/jobs")
	public String jobs() throws Exception {
		List<JobDef> jobs = jobDefService.jobs();

		jobs.forEach(job -> {
			JobStat stats = jobExecuteService.stat(job.getId());
			if (stats != null) {
				job.setStat(stats);
			}
		});
		if (jobs.size() > 2) {
			jobs = jobs.stream().sorted((a, b) -> a.getTitle().compareTo(b.getTitle())).collect(Collectors.toList());
		}

		// 查看任务的运行状态
		jobs.stream().filter(item -> BooleanUtil.isTrue(item.getRunable())).forEach(job -> {
			boolean isrunning = jobRunnerService.isrunning(job.getId());
			if (job.getStat() == null) {
				job.setStat(new JobStat());
			}
			job.getStat().setRunning(isrunning);

		});

		Map<String, JobDef> predefs = new HashMap<>();
		// 获得预发配置
		jobs.forEach(job -> {
			JobDef predef = jobDefService.getpre(job.getId());
			if (predef != null && !predef.getMd5().equals(job.getMd5())) {
				predefs.put(job.getId(), predef);
			}
		});

		JSONObject response = new JSONObject();
		response.put("jobs", jobs);
		response.put("prejobs", predefs);
		return response.toString();
	}

	@Authed
	@GetMapping(value = "/run")
	public String run(String id) {

		if (jobRunnerService.isrunning(id)) {
			JSONObject response = new JSONObject();
			response.put("success", false);
			response.put("isrunning", true);
			return response.toString();
		}

		jobRunnerService.start(id);
		JSONObject response = new JSONObject();
		response.put("success", true);
		return response.toString();
	}

	@Authed
	@GetMapping(value = "/stop")
	public String stop(String id) {
		jobRunnerService.cancel(id);
		JSONObject response = new JSONObject();
		response.put("running", jobRunnerService.isrunning(id));
		return response.toString();
	}

	@Authed
	@GetMapping(value = "/jobstatus")
	public String jobstatus(String id) {
		JSONObject response = new JSONObject();
		response.put("running", jobRunnerService.isrunning(id));
		return response.toString();
	}

	@Authed
	@GetMapping(value = "/logs")
	public String logs(String id) throws Exception {

		BlockingQueue<String> list = jobDefService.logs(id);
		JSONObject response = new JSONObject();
		response.put("jobId", id);

		StringBuilder logstr = new StringBuilder();
		if (!CollectionUtils.isEmpty(list)) {
			list.forEach(item -> {
				logstr.append(item).append("\t\n");
			});
		}
		response.put("logs", logstr.toString());
		response.put("running", jobRunnerService.isrunning(id));
		return response.toString();
	}

	@Authed
	@GetMapping(value = "/logs/trace")
	public String requestlogs(String traceid) throws Exception {

		List<String> list = jobDefService.traceLogs(traceid);
		JSONObject response = new JSONObject();

		StringBuilder logstr = new StringBuilder();
		if (!CollectionUtils.isEmpty(list)) {
			list.forEach(item -> {
				logstr.append(item).append("\t\n");
			});
		}
		response.put("logs", logstr.toString());
		return response.toString();
	}

	@Authed
	@GetMapping(value = "/logsclear")
	public String logsClear(String id) throws Exception {

		BlockingQueue<String> list = jobDefService.logs(id);
		JSONObject response = new JSONObject();
		response.put("jobId", id);
		if (!CollectionUtils.isEmpty(list)) {
			list.clear();
		}
		return response.toString();
	}

	@Authed
	@GetMapping(value = "/status")
	public String status() throws Exception {
		JSONObject response = new JSONObject();
		MemoryMXBean mb = ManagementFactory.getMemoryMXBean();
		MemoryUsage heapMemoryUsage = mb.getHeapMemoryUsage();
		response.put("heapInit", heapMemoryUsage.getInit() / 1024 / 1024);
		response.put("heapUsed", heapMemoryUsage.getUsed() / 1024 / 1024);
		response.put("heapMax", heapMemoryUsage.getMax() / 1024 / 1024);
		OperatingSystemMXBean osm = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
		response.put("cpuload", osm.getProcessCpuLoad());
		response.put("systemCpuLoad", osm.getSystemCpuLoad());
		response.put("systemLoadAverage", osm.getSystemLoadAverage());
		return response.toString();
	}

	@GetMapping(value = "/version")
	public String version() throws Exception {
		JSONObject response = new JSONObject();
		String home = System.getProperty("user.home");
		File versionFile = new File(home, ".codingless-standalone-datawork.version.current");
		if (versionFile.exists() && versionFile.isFile()) {
			FileInputStream fis = null;
			try {
				fis = new FileInputStream(versionFile);
				byte[] data = new byte[128];
				int len = fis.read(data);
				String version = new String(data, 0, len);
				response.put("version", version);
			} finally {
				if (fis != null) {
					fis.close();
				}
			}
		} else {
			response.put("version", "0.0.0");

		}
		return response.toString();
	}

}
