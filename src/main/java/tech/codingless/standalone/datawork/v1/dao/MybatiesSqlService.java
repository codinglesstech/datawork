package tech.codingless.standalone.datawork.v1.dao;

import java.util.Map;

import org.apache.ibatis.mapping.SqlSource;

public interface MybatiesSqlService {

	SqlSource exchangeSelectSql(String xmlSelectSql, Map<String, Object> param) throws Exception;
}
