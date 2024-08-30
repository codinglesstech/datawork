package tech.codingless.standalone.datawork.util;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;

public class BigDecimalUtil {

	public static BigDecimal min(BigDecimal... nums) {
		if (nums.length == 1) {
			return nums[0];
		}
		List<BigDecimal> list = toList(nums);
		return list.stream().sorted((a, b) -> a.compareTo(b) > 0 ? 1 : -1).findFirst().get();
	}

	public static BigDecimal max(BigDecimal... nums) {
		if (nums.length == 1) {
			return nums[0];
		}

		List<BigDecimal> list = toList(nums);
		return list.stream().sorted((a, b) -> a.compareTo(b) > 0 ? -1 : 1).findFirst().get();
	}

	public static BigDecimal multiply(BigDecimal... nums) {
		List<BigDecimal> list = toList(nums);

		if (list.isEmpty()) {
			return BigDecimal.ZERO;
		}
		if (list.size() == 1) {
			return list.get(0);
		}
		BigDecimal rs = list.remove(0);
		for (BigDecimal val2 : list) {
			rs = rs.multiply(val2, MathContext.DECIMAL64);
		}
		return rs;
	}

	public static BigDecimal divide(BigDecimal... nums) {
		List<BigDecimal> list = toList(nums);

		if (list.isEmpty()) {
			return BigDecimal.ZERO;
		}
		if (list.size() == 1) {
			return list.get(0);
		}
		BigDecimal rs = list.remove(0);
		for (BigDecimal val2 : list) {
			rs = rs.divide(val2, MathContext.DECIMAL64);
		}
		return rs;
	}

	public static List<BigDecimal> toList(BigDecimal... nums) {
		List<BigDecimal> list = new ArrayList<>(8);
		for (BigDecimal num : nums) {
			if (num != null) {
				list.add(num);
			}
		}
		return list;
	}

	public static boolean eq(BigDecimal num1, BigDecimal num2) {
		if (num1 == null || num2 == null) {
			return false;
		}
		return num1.compareTo(num2) == 0;
	}

	public static boolean gt(BigDecimal num1, BigDecimal num2) {
		if (num1 == null || num2 == null) {
			return false;
		}
		return num1.compareTo(num2) > 0;
	}

	public static boolean gte(BigDecimal num1, BigDecimal num2) {
		if (num1 == null || num2 == null) {
			return false;
		}
		return num1.compareTo(num2) >= 0;
	}

	public static boolean lt(BigDecimal num1, BigDecimal num2) {
		if (num1 == null || num2 == null) {
			return false;
		}
		return num2.compareTo(num1) > 0;
	}

	public static boolean lte(BigDecimal num1, BigDecimal num2) {
		if (num1 == null || num2 == null) {
			return false;
		}
		return num2.compareTo(num1) >= 0;
	}

	public static BigDecimal add(BigDecimal... nums) {
		List<BigDecimal> list = toList(nums);

		if (list.isEmpty()) {
			return BigDecimal.ZERO;
		}
		if (list.size() == 1) {
			return list.get(0);
		}
		BigDecimal rs = list.remove(0);
		for (BigDecimal val2 : list) {
			rs = rs.add(val2, MathContext.DECIMAL64);
		}
		return rs;
	}

	public static BigDecimal subtract(BigDecimal... nums) {
		List<BigDecimal> list = toList(nums);
		if (list.isEmpty()) {
			return BigDecimal.ZERO;
		}
		if (list.size() == 1) {
			return list.get(0);
		}
		BigDecimal rs = list.remove(0);
		for (BigDecimal val2 : list) {
			rs = rs.subtract(val2, MathContext.DECIMAL64);
		}
		return rs;
	}
}
