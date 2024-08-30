package tech.codingless.standalone.datawork.util;

import java.util.List;

public class SQLHelper {

	public static String compress(String sql) {
		StringBuilder compresssql = new StringBuilder();
		List.of(sql.split("\n")).stream().filter(item -> StringUtil.isNotEmpty(item)).forEach(item -> {
			compresssql.append(item).append(" ");
		});
		return compresssql.toString().trim();
	}

}
