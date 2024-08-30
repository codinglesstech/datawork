package tech.codingless.standalone.datawork.v1.service.process;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson2.JSON;

import lombok.extern.slf4j.Slf4j;
import tech.codingless.standalone.datawork.helper.MemoryObservationHelper;
import tech.codingless.standalone.datawork.util.JobHelper;
import tech.codingless.standalone.datawork.util.StringUtil;
import tech.codingless.standalone.datawork.v1.data.JobDef;
import tech.codingless.standalone.datawork.v1.data.JobProcess;
import tech.codingless.standalone.datawork.v1.data.SqlTable;
import tech.codingless.standalone.datawork.v1.service.JobProcessService;

@Slf4j
@Service
public class JobProcessServiceOnMockImpl implements JobProcessService {

	@Override
	public String command() {
		return ":on-mock";
	}

	@Override
	public boolean execute(String traceid, JobDef job, JobProcess process, Map<String, Object> context) {
		if (StringUtil.isEmpty(process.getBody())) {
			log.info("Not Found Any Mock Data");
			return false;
		}
		/**
		 * Mock数据为 scv格式的数据，这样方便从数据库中直接复制
		 */
		List<String> lines = List.of(process.getBody().split("\n"));
		String[] columns = lines.get(0).split("\t");
		List<Map<String, Object>> data = new ArrayList<>();
		for (int j = 1; j < lines.size(); j++) {
			String line = lines.get(j);
			if (StringUtil.isEmpty(line)) {
				continue;
			}
			String[] values = line.split("\t");
			if (values.length != columns.length) {
				log.warn("Line Data Format Error:{}", line);
				continue;
			}
			Map<String, Object> row = new HashMap<>();
			for (int i = 0; i < values.length; i++) {
				row.put(columns[i].toLowerCase(), values[i]);
			}
			data.add(row);
		}
		data.forEach(row -> {
			log.info("Mock Data:{}", JSON.toJSONString(row));
		});

		String varname = JobHelper.getProcessParam(process.getParam(), "result", "result");
		SqlTable sqlTable = new SqlTable();
		sqlTable.setData(data);
		MemoryObservationHelper.increment(sqlTable);
		context.put(varname, sqlTable);
		return true;
	}

}
