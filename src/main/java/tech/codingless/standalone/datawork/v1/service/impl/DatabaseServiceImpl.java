package tech.codingless.standalone.datawork.v1.service.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.sql.DataSource;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.SqlSource;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import tech.codingless.standalone.datawork.util.AssertUtil;
import tech.codingless.standalone.datawork.v1.dao.MybatiesSqlService;
import tech.codingless.standalone.datawork.v1.service.DataSourceService;
import tech.codingless.standalone.datawork.v1.service.DatabaseService;

@Slf4j
@Service
public class DatabaseServiceImpl implements DatabaseService {

	@Resource
	private DataSourceService dataSourceService;

	@Resource
	private MybatiesSqlService mybatiesSqlService;

	@Override
	public List<Map<String, Object>> select(String datasourceId, String select, Map<String, Object> param) {

		DataSource datasource = dataSourceService.getDataSource(datasourceId);
		AssertUtil.assertNotEmpty(select, "select_empty");
		AssertUtil.assertNotNull(datasource, "datasource_not_exist");

		Connection conn = null;
		BoundSql boundSql = null;
		try {
			if (param == null) {
				param = new HashMap<>();
			}
			SqlSource sqlSource = mybatiesSqlService.exchangeSelectSql(select, param);
			boundSql = sqlSource.getBoundSql(param);
			conn = datasource.getConnection();
			PreparedStatement ps = conn.prepareStatement(boundSql.getSql());
			// bind param

			StringBuilder plog = new StringBuilder();

			@SuppressWarnings("unchecked")
			Map<String, Object> parameter = ((Map<String, Object>) boundSql.getParameterObject());

			if (CollectionUtils.isNotEmpty(boundSql.getParameterMappings())) {
				for (int i = 0; i < boundSql.getParameterMappings().size(); i++) {
					ParameterMapping p = boundSql.getParameterMappings().get(i);
					Object val = boundSql.getAdditionalParameter(p.getProperty());
					if (val == null) {
						val = parameter.get(p.getProperty());
					}
					ps.setObject(i + 1, val);
					plog.append(val).append("(").append(p.getJavaType().getSimpleName()).append(")").append(",");
				}
			}
			if (plog.length() > 0) {
				plog.deleteCharAt(plog.length() - 1);
			}

			ResultSet rs = ps.executeQuery();
			List<Map<String, Object>> list = new ArrayList<>();
			ResultSetMetaData rsmd = rs.getMetaData();
			int columnCount = rsmd.getColumnCount();
			while (rs.next()) {
				Map<String, Object> row = new HashMap<>();
				for (int i = 1; i <= columnCount; i++) {
					String name = rsmd.getColumnLabel(i).toLowerCase();
					Object value = rs.getObject(i);
					row.put(name, value);
				}
				list.add(row);
			}
			if (log.isInfoEnabled()) {
				String logsql = boundSql.getSql().replaceAll("[\r\n]", "").replaceAll("[ \t]+", " ");
				log.info("SELECT_SQL: {}  \t Paramaters:{} \t Result:{}", logsql, plog.toString(), list.size());
			}
			return list;
		} catch (Exception e) {
			String logsql = "";
			if (boundSql != null) {
				logsql = boundSql.getSql().replaceAll("[\r\n]", "").replaceAll("[ \t]+", " ");
			}
			log.error(logsql, e);
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					log.error("", e);
				}
			}
		}

		return null;
	}

}
