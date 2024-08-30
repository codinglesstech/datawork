package tech.codingless.standalone.datawork.v1.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson2.JSONObject;

import lombok.extern.slf4j.Slf4j;
import tech.codingless.standalone.datawork.helper.MemoryObservationHelper;
import tech.codingless.standalone.datawork.helper.ParameterTestHelper;
import tech.codingless.standalone.datawork.v1.service.UtilService;

@Slf4j
@Service
public class UtilServiceImpl implements UtilService {

	@Override
	public String stringify(Object obj) {
		if (obj == null) {
			return "{}";
		}
		return JSONObject.toJSONString(obj);
	}

	@Override
	public void incrementMemorySize(Object obj) {
		MemoryObservationHelper.increment(obj);
	}

	@Override
	public long memorySizeUsed() {
		return MemoryObservationHelper.size();
	}

	@Override
	public Map<String, Object> test(Map<String, Object> param, List<Map<String, Object>> rules) {
		return ParameterTestHelper.test(param, rules);
	}

	@Override
	public String envinfo() {
		StringBuilder sb = new StringBuilder();

		return sb.toString();
	}

}
