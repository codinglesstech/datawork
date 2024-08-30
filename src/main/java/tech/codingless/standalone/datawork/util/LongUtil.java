package tech.codingless.standalone.datawork.util;

import java.util.ArrayList;
import java.util.List;

public class LongUtil {

	public static boolean gt(Long num, long val) {
		return num != null && num > val;
	}

	public static boolean eq(Long num, Long val) {
		if (num == null || val == null) {
			return false;
		}
		return num.equals(val);
	}

	public static boolean lt(Long num, long val) {
		return num != null && num < val;
	}

	public static long dftIfNull(Long num, long val) {
		return num != null ? num : val;
	}

	public static long max(Long num1, Long num2) {
		return Math.max(dftIfNull(num1, 0), dftIfNull(num2, 0));
	}

	public static long add(Long num1, int num2) {
		return dftIfNull(num1, 0L) + num2;
	}

	public static long subtract(Long num1, Long num2) {
		return dftIfNull(num1, 0L) - dftIfNull(num2, 0L);
	}

	public static long add(Long num1, Long num2) {
		return dftIfNull(num1, 0L) + dftIfNull(num2, 0L);
	}

	public static long add(Long... nums) {
		List<Long> list = new ArrayList<>(8);
		for (Long num : nums) {
			if (num != null) {
				list.add(num);
			}
		}
		if (list.isEmpty()) {
			return 0;
		}
		return list.stream().mapToLong(item -> item).sum();
	}
}
