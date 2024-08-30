package tech.codingless.standalone.datawork.v1.service;

import java.util.List;
import java.util.Map;

public interface DatabaseService {

	List<Map<String, Object>> select(String datasourceId, String select, Map<String, Object> param);

}
