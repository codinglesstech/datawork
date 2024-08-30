package tech.codingless.standalone.datawork.v1.service.impl;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.ApplicationContext;

import lombok.extern.slf4j.Slf4j;
import tech.codingless.standalone.datawork.v1.data.JobDef;
import tech.codingless.standalone.datawork.v1.service.JobExecuteService;
import tech.codingless.standalone.datawork.v1.service.JobRunner;

@Slf4j
public class JobRunnerImpl implements JobRunner {
	private JobDef jobDef;

	private JobExecuteService jobExecuteService;

	private boolean running;

	public JobRunnerImpl(ApplicationContext context, JobDef def) {
		this.jobDef = def;
		jobExecuteService = context.getBean(JobExecuteService.class);
	}

	@Override
	public boolean isrunning() {
		return running;
	}

	@Override
	public void run() {
		try {
			running = true;
			Map<String, Object> context = new HashMap<>();
			jobExecuteService.execute(jobDef, context);

		} catch (Throwable e) {
			log.info("Execute Job Fail :" + jobDef.getId(), e);
		}
		running = false;
	}

}
