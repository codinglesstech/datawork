package tech.codingless.standalone.datawork.v1.service.process;

import java.util.List;
import java.util.Map;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.Value;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import tech.codingless.standalone.datawork.util.StringUtil;
import tech.codingless.standalone.datawork.v1.data.JobDef;
import tech.codingless.standalone.datawork.v1.data.JobProcess;
import tech.codingless.standalone.datawork.v1.data.SqlTable;
import tech.codingless.standalone.datawork.v1.service.JobProcessService;

@Slf4j
@Service
public class JobProcessServiceOnResponseImpl implements JobProcessService {

	private final static String LANGUAGE_ID = "js";

	@Override
	public String command() {
		return ":on-response";
	}

	@Override
	public boolean execute(String traceid, JobDef job, JobProcess process, Map<String, Object> context) {
		if (StringUtil.isEmpty(process.getBody())) {
			log.info("Not Found Content");
			return false;
		}

		HostAccess hostaccess = HostAccess.newBuilder().allowIteratorAccess(true).allowIterableAccess(true).allowListAccess(true).allowMapAccess(true).allowPublicAccess(true).build();
		try (Context graalvmContext = Context.newBuilder(LANGUAGE_ID).allowHostAccess(hostaccess).build()) {
			Value val = graalvmContext.getBindings(LANGUAGE_ID);
			val.putMember("log", log);
			context.entrySet().forEach(entry -> {

				if (log.isDebugEnabled()) {
					log.debug("PutMember,Key:{} ", entry.getKey());
				}
				Object value = entry.getValue();
				if (value instanceof SqlTable) {
					List<Map<String, Object>> list = ((SqlTable) entry.getValue()).getData();
					val.putMember(entry.getKey(), list);
				} else {
					val.putMember(entry.getKey(), entry.getValue());
				}
			});
			graalvmContext.eval(LANGUAGE_ID, process.getBody());
			log.info("Response 执行完毕");
		}

		return true;
	}

}
