package tech.codingless.standalone.datawork.v1.service.impl;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import tech.codingless.standalone.datawork.v1.data.JobDef;
import tech.codingless.standalone.datawork.v1.service.JobRunnerService;

@Slf4j
@Service
public class SchedulingConfigurerImpl implements JobRunnerService {

	@Override
	public JobDef get(String jobId) {
		return null;
	}

	@Override
	public void start(String jobId) {

	}

	@Override
	public void start(JobDef job) {

	}

	@Override
	public boolean isrunning(String jobId) {
		return false;
	}

	@Override
	public boolean cancel(String jobId) {
		return false;
	}

}
