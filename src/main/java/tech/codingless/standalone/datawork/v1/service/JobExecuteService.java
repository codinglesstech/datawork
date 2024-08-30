package tech.codingless.standalone.datawork.v1.service;

import java.util.Map;

import tech.codingless.standalone.datawork.v1.data.JobDef;
import tech.codingless.standalone.datawork.v1.data.JobStat;
import tech.codingless.standalone.datawork.v1.data.ProcessResponse;

public interface JobExecuteService {

	void execute(JobDef job, Map<String, Object> context);

	void execute(JobDef job, Map<String, Object> context, ProcessResponse response);

	JobStat stat(String jobId);

	boolean isrunning(String jobId);
}
