package tech.codingless.standalone.datawork.v1.data;

import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class SqlTable {
	private List<Map<String, Object>> data;
}
