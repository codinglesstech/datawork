package tech.codingless.standalone.datawork.v1.service.process;

import java.util.Map;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import tech.codingless.standalone.datawork.v1.data.JobDef;
import tech.codingless.standalone.datawork.v1.data.JobProcess;
import tech.codingless.standalone.datawork.v1.service.JobProcessService;

@Slf4j
@Service
public class JobProcessServiceOnUpdateImpl implements JobProcessService {

	@Override
	public String command() {
		return ":on-update";
	}

	@Override
	public boolean execute(String traceid, JobDef job, JobProcess process, Map<String, Object> context) {
		// TODO Auto-generated method stub
		return false;
	}

}
