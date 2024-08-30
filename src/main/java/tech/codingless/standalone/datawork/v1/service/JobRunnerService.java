package tech.codingless.standalone.datawork.v1.service;

import tech.codingless.standalone.datawork.v1.data.JobDef;

public interface JobRunnerService {

	public void start(JobDef job);

	public void start(String jobId);

	public JobDef get(String jobId);

	/**
	 * 是否处于运行中
	 * 
	 * @param jobId
	 * @return
	 */
	public boolean isrunning(String jobId);

	public boolean cancel(String jobId);

}
