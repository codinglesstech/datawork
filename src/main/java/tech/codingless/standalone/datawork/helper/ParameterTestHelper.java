package tech.codingless.standalone.datawork.helper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tech.codingless.standalone.datawork.util.BooleanUtil;

public class ParameterTestHelper {

	/**
	 * 验证参数
	 * 
	 * @param param
	 * @param rules
	 * @return
	 */
	public static Map<String, Object> test(Map<String, Object> param, List<Map<String, Object>> rules) {
		Map<String, Object> result = new HashMap<>();
		if (param == null || rules == null) {
			result.put("passed", false);
			result.put("message", "param empty or rules empty");
			return result;
		}
		StringBuilder message = new StringBuilder();
		for (Map<String, Object> rule : rules) {
			String column = (String) rule.get("column");
			Object required = rule.get("required");
			String type = (String) rule.get("type");
			String comment = (String) rule.get("comment");
			if ((column == null) || (type == null) || (comment == null)) {
				continue;
			}
			if (required != null && !(required instanceof Boolean)) {
				continue;
			}
			Object value = param.get(column);

			if (value == null && BooleanUtil.isTrue((Boolean) required)) {
				message.append("Parameter ").append(column).append(" Is Required");
				break;
			}
			if (value == null && !BooleanUtil.isTrue((Boolean) required)) {
				// 非必填，又没填，直接跳过
				continue;
			}
			// 参数不为空的情况
			if (type.trim().toLowerCase().startsWith("varchar")) {

			}

		}
		result.put("passed", message.isEmpty());
		result.put("message", message.toString());
		return result;
	}
}
