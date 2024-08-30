package tech.codingless.standalone.datawork.v1.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.Value;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import tech.codingless.standalone.datawork.v1.data.SqlTable;
import tech.codingless.standalone.datawork.v1.service.DatabaseService;
import tech.codingless.standalone.datawork.v1.service.JavaScriptService;
import tech.codingless.standalone.datawork.v1.service.RedisService;
import tech.codingless.standalone.datawork.v1.service.UtilService;

@Slf4j
@Service
public class JavaScriptServiceImpl implements JavaScriptService {
	private final static String LANGUAGE_ID = "js";

	@Resource
	private RedisService redisService;
	@Resource
	private DatabaseService databaseService;

	@Resource
	private UtilService utilService;

	private final static Map<String, Boolean> keywords = new HashMap<>();
	static {
		/**
		 * 关键字或对象
		 */
		keywords.put("http", true);
		keywords.put("log", true);
		keywords.put("es", true);
		keywords.put("mq", true);
		keywords.put("redis", true);
		keywords.put("mg", true);
		keywords.put("amazon", true);
		keywords.put("db", true);
		keywords.put("util", true);
	}

	@Override
	public void execute(Map<String, Object> context, String javascript) {
		HostAccess hostaccess = HostAccess.newBuilder().allowIteratorAccess(true).allowIterableAccess(true).allowListAccess(true).allowMapAccess(true).allowPublicAccess(true).build();
		try (Context graalvmContext = Context.newBuilder(LANGUAGE_ID).allowHostAccess(hostaccess).build()) {
			Value val = graalvmContext.getBindings(LANGUAGE_ID);
			val.putMember("log", log);
			val.putMember("redis", redisService);
			val.putMember("db", databaseService);
			val.putMember("util", utilService);
			context.entrySet().forEach(entry -> {
				if (keywords.containsKey(entry.getKey().toLowerCase())) {
					if (log.isDebugEnabled()) {
						log.debug("Key:{} Is Matched Keywords,Skip Bind to context", entry.getKey());
					}
					return;
				}
				if (log.isDebugEnabled()) {
					log.debug("PutMember,Key:{},Value:{}", entry.getKey(), entry.getValue());
				}
				Object value = entry.getValue();
				if (value instanceof SqlTable) {
					List<Map<String, Object>> list = ((SqlTable) entry.getValue()).getData();
					val.putMember(entry.getKey(), list);
				} else {
					val.putMember(entry.getKey(), entry.getValue());
				}
			});
			graalvmContext.eval(LANGUAGE_ID, javascript);

		}

	}

}
