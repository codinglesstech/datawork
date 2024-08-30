package tech.codingless.standalone.datawork.v1.dao;

import java.util.List;

import tech.codingless.standalone.datawork.util.StringUtil;

public class XmlSelectSqlHelper {
	/**
	 * clear comment info from sql
	 * 
	 * @param commentedSql
	 * @return
	 */
	public static String cleanCommentInfo(String commentedSql) {
		StringBuilder newsql = new StringBuilder();
		List.of(commentedSql.split("[\r\n]")).stream().filter(line -> !line.trim().startsWith("//") && StringUtil.isNotEmpty(line)).forEach(line -> {
			newsql.append(line).append("\r ");
		});

		return newsql.toString();
	}

}
