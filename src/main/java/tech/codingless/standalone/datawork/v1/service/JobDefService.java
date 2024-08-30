package tech.codingless.standalone.datawork.v1.service;

import java.util.List;
import java.util.concurrent.BlockingQueue;

import tech.codingless.standalone.datawork.v1.data.JobDef;

public interface JobDefService {

	/**
	 * 从本地目录加载任务
	 * 
	 * @param confDir
	 * @return
	 */
	public List<JobDef> loadFromLocalFile(String confDir) throws Exception;

	public List<JobDef> parse(String configContent);

	public long reload(List<JobDef> jobs);

	public long prereload(List<JobDef> jobs);

	public List<JobDef> jobs();

	public JobDef get(String id);

	/**
	 * 获得预发
	 * 
	 * @param id
	 * @return
	 */
	public JobDef getpre(String id);

	public BlockingQueue<String> logs(String jobId);

	public List<String> traceLogs(String traceId);

}
