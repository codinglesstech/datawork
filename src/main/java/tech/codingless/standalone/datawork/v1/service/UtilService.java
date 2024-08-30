package tech.codingless.standalone.datawork.v1.service;

import java.util.List;
import java.util.Map;

public interface UtilService {

	String stringify(Object obj);

	void incrementMemorySize(Object obj);

	long memorySizeUsed();

	/**
	 * 验证参数
	 * 
	 * @param param
	 * @param rules
	 * @return
	 */
	Map<String, Object> test(Map<String, Object> param, List<Map<String, Object>> rules);

	String envinfo();
}
