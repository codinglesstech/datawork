package tech.codingless.standalone.datawork.v1.service;

import java.util.Map;

import tech.codingless.standalone.datawork.v1.data.JobDef;
import tech.codingless.standalone.datawork.v1.data.JobProcess;

/**
 * 
 * @author 王鸿雁
 * @Date 2024年5月16日
 *
 */
public interface JobProcessService {

	String command();

	/**
	 * 执行一个过程
	 * 
	 * @param job
	 * @param process
	 * @param context
	 * @return
	 */
	boolean execute(String traceid, JobDef job, JobProcess process, Map<String, Object> context);
}
