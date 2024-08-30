package tech.codingless.standalone.datawork.util;

import java.util.List;
import java.util.Optional;

public class JobHelper {

	public static String getProcessParam(String param, String name, String defaultValue) {
		if (StringUtil.isEmpty(param)) {
			return defaultValue;
		}

		Optional<String> optionl = List.of(param.split("[ \t]")).stream().filter(item -> item.toLowerCase().startsWith(name.toLowerCase() + "=")).findFirst();
		if (optionl.isEmpty()) {
			return defaultValue;
		}

		return optionl.get().split("=")[1].trim();
	}

}
