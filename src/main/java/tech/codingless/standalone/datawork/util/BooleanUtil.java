package tech.codingless.standalone.datawork.util;

import org.apache.commons.lang3.BooleanUtils;

public class BooleanUtil {

	public static boolean isTrue(Boolean bool) {
		return bool != null && bool;
	}

	public static boolean isNotTrue(Boolean bool) {
		return bool == null || bool == false;
	}

	public static boolean isNotFalse(Boolean bool) {
		return BooleanUtils.isNotFalse(bool);
	}
}
