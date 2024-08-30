package tech.codingless.standalone.datawork.v1.service.process;

import java.util.Map;

import javax.annotation.Resource;

import org.elasticsearch.core.List;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import tech.codingless.standalone.datawork.util.StringUtil;
import tech.codingless.standalone.datawork.v1.data.JobDef;
import tech.codingless.standalone.datawork.v1.data.JobProcess;
import tech.codingless.standalone.datawork.v1.service.JavaScriptService;
import tech.codingless.standalone.datawork.v1.service.JobDefService;
import tech.codingless.standalone.datawork.v1.service.JobProcessService;

@Slf4j
@Service
public class JobProcessServiceOnJsImpl implements JobProcessService {

	@Resource
	private JavaScriptService javaScriptService;

	@Resource
	private JobDefService jobDefService;

	@Override
	public String command() {
		return ":on-js";
	}

	@Override
	public boolean execute(String traceid, JobDef job, JobProcess process, Map<String, Object> context) {
		if (StringUtil.isEmpty(process.getBody())) {
			log.info("Not Found Any JS Script");
			return false;
		}

		// 加载import内容
		StringBuilder importedCode = new StringBuilder();
		if (StringUtil.isNotEmpty(job.getImportPackages())) {

			List.of(job.getImportPackages().split("[\r\n]")).stream().filter(item -> StringUtil.isNotEmpty(item)).map(item -> item.trim()).forEach(packageId -> {
				JobDef def = jobDefService.get(packageId);
				if (def == null) {
					return;
				}
				if (def.getType() != JobDef.TYPE.PKG) {
					return;
				}
				if (StringUtil.isEmpty(def.getCode())) {
					return;
				}
				importedCode.append(def.getCode()).append("\r\n");
				log.info("Import Package :{}", packageId);
			});
		}

		String jscode = "";
		if (importedCode.isEmpty()) {
			jscode = process.getBody();
		} else {
			jscode += importedCode.toString();
			jscode += process.getBody();
		}
		javaScriptService.execute(context, jscode);
		return true;
	}

}
