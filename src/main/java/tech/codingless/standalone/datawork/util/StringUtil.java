package tech.codingless.standalone.datawork.util;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtil {
	public static final String EMPTY_STR = "";
	public static final String SYMBOL_COMMA = ",";
	public static final String STR_SUCCESS = "SUCCESS";

	/**
	 * 如果为空，则返回默认值
	 * 
	 * @param str
	 * @param defaultStr
	 * @return
	 */
	public static String defaultIfEmpty(Object str, String defaultStr) {
		if (str == null) {
			return defaultStr;
		}
		return str.toString();
	}

	public static boolean success(String str) {
		return STR_SUCCESS.equalsIgnoreCase(str);
	}

	public static boolean notSuccess(String str) {
		return !STR_SUCCESS.equalsIgnoreCase(str);
	}

	public static boolean isNotEmpty(String str) {
		return !isEmpty(str);
	}

	public static boolean isAllEmpty(String... str) {
		for (String s : str) {
			if (isNotEmpty(s)) {
				return false;
			}
		}
		return true;
	}

	public static boolean isNotEmpty(String... str) {
		for (String s : str) {
			if (s == null || s.trim().equals(EMPTY_STR)) {
				return false;
			}
		}
		return true;
	}

	public static boolean isEmpty(String str) {
		return str == null || str.trim().equals(EMPTY_STR);
	}

	public static boolean isEmpty(String... str) {
		for (String s : str) {
			if (s == null || s.trim().equals(EMPTY_STR)) {
				return true;
			}
		}
		return false;
	}

	public static String genGUID() {

		return java.util.UUID.randomUUID().toString().replace("-", "");

	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static List<String> doScattered(String str, int len) {
		List<String> list = new ArrayList();
		while (true) {
			if (str.length() > len) {
				list.add(str.substring(0, len));
				str = str.substring(len);
			} else {
				list.add(str);
				break;
			}
		}

		return list;
	}

	public static String genHeadPicDiv(String str) {
		List<String> list = doScattered(str, 3);
		String path = "/";
		for (String tmp : list) {
			path += tmp + "/";
		}
		return path;
	}

	private static final String MOBILE_REGEX = "(1[0-9]{10})|([\\+]?[0-9]{11,15})";

	public static boolean isMobileNumber(String mobile) {
		if (isEmpty(mobile)) {
			return false;
		}
		return mobile.matches(MOBILE_REGEX);
	}

	private static Random random = new Random();

	public static String randomNumber(int len) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < len; i++) {
			sb.append(random.nextInt(10));
		}
		return sb.toString();
	}

	public static String format(String tmp, Object... value) {
		return String.format(tmp, value);
	}

	public static String sha(String decript) {
		try {
			MessageDigest digest = java.security.MessageDigest.getInstance("SHA");
			digest.update(decript.getBytes());
			byte messageDigest[] = digest.digest();
			// Create Hex String
			StringBuffer hexString = new StringBuffer();
			// 字节数组转换为 十六进制 数
			for (int i = 0; i < messageDigest.length; i++) {
				String shaHex = Integer.toHexString(messageDigest[i] & 0xFF);
				if (shaHex.length() < 2) {
					hexString.append(0);
				}
				hexString.append(shaHex);
			}
			return hexString.toString();

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return EMPTY_STR;
	}

	public static String md5(String input) {
		try {
			// 获得MD5摘要算法的 MessageDigest 对象
			MessageDigest mdInst = MessageDigest.getInstance("MD5");
			// 使用指定的字节更新摘要
			mdInst.update(input.getBytes());
			// 获得密文
			byte[] md = mdInst.digest();
			// 把密文转换成十六进制的字符串形式
			StringBuffer hexString = new StringBuffer();
			// 字节数组转换为 十六进制 数
			for (int i = 0; i < md.length; i++) {
				String shaHex = Integer.toHexString(md[i] & 0xFF);
				if (shaHex.length() < 2) {
					hexString.append(0);
				}
				hexString.append(shaHex);
			}
			return hexString.toString();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return EMPTY_STR;
	}

	private static final String RANDOM_CHARS = "1234pqrstuv590abcdefghijklmnowxyzAB678CDEFGHIJK1234567890LMNOPQRSTUVWXYZ";

	public static String random(int len) {
		int charSize = RANDOM_CHARS.length();
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < len; i++) {
			sb.append(random.nextInt(charSize));
		}
		return sb.toString();
	}

	private static final String NUMBER_REGEX = "^[0-9]+$";

	public static boolean isNumber(String str) {
		return isNotEmpty(str) && str.matches(NUMBER_REGEX);
	}

	private static final String FLOAT_REGEX = "^[0-9\\.]+$";

	public static boolean isFloat(String str) {
		return isNotEmpty(str) && str.matches(FLOAT_REGEX);
	}

	public static String endSubstring(String str, int len) {
		if (isEmpty(str)) {
			return EMPTY_STR;
		}
		if (str.length() <= len) {
			return str;
		}

		return str.substring(str.length() - len);
	}

	public static boolean hasEmpty(String... str) {
		for (String s : str) {
			if (s == null || s.trim().equals(EMPTY_STR)) {
				return true;
			}
		}
		return false;
	}

	public static int toInt(String str) {
		if (!isNumber(str)) {
			return 0;
		}
		return Integer.parseInt(str);
	}

	public static String concatComma(String... strs) {
		if (strs == null) {
			return EMPTY_STR;
		}
		StringBuilder sb = new StringBuilder();
		for (String str : strs) {
			sb.append(str).append(SYMBOL_COMMA);
		}
		return sb.toString();
	}

	public static char tryLuck(String luckStr) {
		return luckStr.charAt(new Random().nextInt(luckStr.length()));
	}

	public static String findOne(String orgStr, String regex) {
		Matcher matcher = Pattern.compile(regex).matcher(orgStr);
		return matcher.find() ? matcher.group() : null;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static List<String> find(String orgStr, String regex) {
		Matcher matcher = Pattern.compile(regex).matcher(orgStr);
		List<String> rs = new ArrayList();
		while (matcher.find()) {
			rs.add(matcher.group());
		}
		return rs;
	}

	public static String getMacAddress() {
		try {
			byte[] mac = NetworkInterface.getByInetAddress(InetAddress.getLocalHost()).getHardwareAddress();
			if (mac == null) {
				return EMPTY_STR;
			}
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < mac.length; i++) {
				if (i != 0) {
					sb.append("-");
				}
				String s = Integer.toHexString(mac[i] & 0xFF);
				sb.append(s.length() == 1 ? 0 + s : s);
			}
			return sb.toString().toUpperCase().replaceAll("-", EMPTY_STR);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public static String base64Encode(String str) {
		if (str == null) {
			return null;
		}
		if (EMPTY_STR.equals(str)) {
			return EMPTY_STR;
		}
		return Base64.getEncoder().encodeToString(str.getBytes());
	}

	public static String base64Decode(String str) {
		if (str == null) {
			return null;
		}
		if (EMPTY_STR.equals(str)) {
			return EMPTY_STR;
		}
		return new String(Base64.getDecoder().decode(str));
	}

	public static String coalesceEmpty(String str) {
		return str == null ? EMPTY_STR : str;
	}

	public static String[] CHARS62 = new String[] { "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z", "0", "1", "2",
			"3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z" };

	public static String genShortGUID() {
		StringBuilder stringBuilder = new StringBuilder();
		String uuid = UUID.randomUUID().toString().replace("-", "");
		for (int i = 0; i < 8; i++) {
			String str = uuid.substring(i * 4, i * 4 + 4);
			stringBuilder.append(CHARS62[Integer.parseInt(str, 16) % 0x3E]);
		}
		return stringBuilder.toString();
	}

	public static String substring(String str, int len) {
		if (len <= 0 || str == null) {
			return EMPTY_STR;
		}
		return str.length() <= len ? str : str.substring(0, len);
	}

}
