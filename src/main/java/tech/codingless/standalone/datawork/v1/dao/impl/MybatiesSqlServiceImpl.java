package tech.codingless.standalone.datawork.v1.dao.impl;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.ibatis.builder.xml.XMLMapperEntityResolver;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.parsing.XNode;
import org.apache.ibatis.parsing.XPathParser;
import org.apache.ibatis.scripting.xmltags.XMLScriptBuilder;
import org.apache.ibatis.session.Configuration;
import org.springframework.stereotype.Service;

import tech.codingless.standalone.datawork.util.StringUtil;
import tech.codingless.standalone.datawork.v1.dao.MybatiesSqlService;

@Service
public class MybatiesSqlServiceImpl implements MybatiesSqlService {
	private static ConcurrentHashMap<String, SqlSource> SQL_CACHE = new ConcurrentHashMap<>();

	private static final String xml_select_1 = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\r\n"
			+ "<!DOCTYPE mapper PUBLIC \"-//mybatis.org//DTD Mapper 3.0//EN\" \"http://mybatis.org/dtd/mybatis-3-mapper.dtd\">\r\n" + "\r\n" + "<mapper namespace=\"TEST\"> \r\n"
			+ "    <select id=\"test\" resultType=\"map\"   parameterType=\"map\">";

	private static final String xml_select_2 = "    </select> \r\n" + "</mapper>";

	@Override
	public SqlSource exchangeSelectSql(String xmlSelectSql, Map<String, Object> param) throws Exception {
		String key = StringUtil.md5("SELECT:" + xmlSelectSql);
		SqlSource sql = SQL_CACHE.get(key);
		if (sql == null) {
			Configuration config = new Configuration();
			String xml = xml_select_1 + xmlSelectSql + xml_select_2;
			Properties properties = new Properties();
			XPathParser xpath = new XPathParser(new ByteArrayInputStream(xml.getBytes("utf-8")), true, properties, new XMLMapperEntityResolver());
			List<XNode> selects = xpath.evalNode("/mapper").evalNodes("select");
			XNode xnode = selects.get(0);// xpath.evalNodes("select|insert|update|delete");
			XMLScriptBuilder xmlscript = new XMLScriptBuilder(config, xnode);
			sql = xmlscript.parseScriptNode();
			SQL_CACHE.put(key, sql);
		}
		return sql;
	}
}
