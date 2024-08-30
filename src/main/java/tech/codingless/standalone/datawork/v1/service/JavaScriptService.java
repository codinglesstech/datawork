package tech.codingless.standalone.datawork.v1.service;

import java.util.Map;

public interface JavaScriptService {

	public void execute(Map<String, Object> context, String javascript);
}
