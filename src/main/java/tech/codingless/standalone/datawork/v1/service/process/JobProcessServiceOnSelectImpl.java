package tech.codingless.standalone.datawork.v1.service.process;

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

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;

import lombok.extern.slf4j.Slf4j;
import tech.codingless.standalone.datawork.helper.MemoryObservationHelper;
import tech.codingless.standalone.datawork.util.JobHelper;
import tech.codingless.standalone.datawork.util.SQLHelper;
import tech.codingless.standalone.datawork.util.StringUtil;
import tech.codingless.standalone.datawork.v1.dao.MybatiesSqlService;
import tech.codingless.standalone.datawork.v1.data.JobDef;
import tech.codingless.standalone.datawork.v1.data.JobProcess;
import tech.codingless.standalone.datawork.v1.data.RemoteSession;
import tech.codingless.standalone.datawork.v1.data.SqlTable;
import tech.codingless.standalone.datawork.v1.service.DataSourceService;
import tech.codingless.standalone.datawork.v1.service.JobProcessService;

@Slf4j
@Service
public class JobProcessServiceOnSelectImpl implements JobProcessService {

	@Resource
	private DataSourceService dataSourceService;

	@Resource
	private MybatiesSqlService mybatiesSqlService;

	@Override
	public String command() {
		return ":on-select";
	}

	@Override
	public boolean execute(String traceid, JobDef job, JobProcess process, Map<String, Object> context) {
		String select = SQLHelper.compress(process.getBody());
		DataSource datasource = dataSourceService.getDataSource(job.getDatabase());
		if (datasource == null) {
			log.warn("No Datasource {}, Skip", job.getDatabase());
			return false;
		}
		if (StringUtil.isEmpty(select)) {
			log.warn("Select SQL IS Empty");
			return false;
		}
		Connection conn = null;
		try {

			// 封装参数,来自请求的参数
			Map<String, Object> param = new HashMap<>();
			if (context.containsKey("_param_")) {
				@SuppressWarnings("unchecked")
				Map<String, String> parammap = (Map<String, String>) context.get("_param_");
				param.putAll(parammap);
			}
			if (context.containsKey("_session_") && context.get("_session_") instanceof RemoteSession) {
				RemoteSession session = (RemoteSession) context.get("_session_");
				param.put("_session_.userId", session.getUserId());
				param.put("_session_.userName", session.getUserName());
				param.put("_session_.companyId", session.getCompanyId());
				param.put("_session_.userPhone", session.getUserPhone());
				param.put("_session_.deptId", session.getDeptId());
				param.put("_session_.deptName", session.getDeptName());
				if (session.getRoles() != null && !session.getRoles().isEmpty()) {
					for (Map.Entry<String, String> entry : session.getRoles().entrySet()) {
						param.put("_session_.roles." + entry.getKey(), entry.getValue());
					}
				}
			}

			String body = (String) context.get("_body_");
			if (StringUtil.isNotEmpty(body) && body.startsWith("{")) {
				// 这种情况，认为用户通过POST传参
				JSONObject bodyjson = JSON.parseObject(body);
				for (Map.Entry<String, Object> entry : bodyjson.entrySet()) {
					param.put(entry.getKey(), entry.getValue());
				}
			}

			param.remove("LIMIT");
			param.remove("OFFSET");
			SqlSource sqlSource = mybatiesSqlService.exchangeSelectSql(select, param);
			BoundSql boundSql = sqlSource.getBoundSql(param);
			conn = datasource.getConnection();
			PreparedStatement ps = conn.prepareStatement(boundSql.getSql());
			// bind param

			StringBuilder plog = new StringBuilder();
			@SuppressWarnings("unchecked")
			HashMap<String, Object> parameter = ((HashMap<String, Object>) boundSql.getParameterObject());

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
			String varname = JobHelper.getProcessParam(process.getParam(), "result", "result");
			SqlTable sqlTable = new SqlTable();
			sqlTable.setData(list);
			MemoryObservationHelper.increment(sqlTable);
			context.put(varname, sqlTable);
			list.forEach(item -> {
				// System.out.println(JSON.toJSONString(item));

			});
		} catch (Exception e) {
			log.error("", e);
			return false;
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					log.error("", e);
				}
			}
		}

		return true;
	}

}
