package tech.codingless.standalone.datawork.config;

import java.io.File;
import java.net.URL;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import tech.codingless.standalone.datawork.v1.data.JobDef;
import tech.codingless.standalone.datawork.v1.service.JobDefService;
import tech.codingless.standalone.datawork.v1.service.JobRunnerService;

@Slf4j
@Component
public class SpringLoadSuccess implements ApplicationListener<ApplicationStartedEvent> {
	@Resource
	private JobRunnerService jobRunnerService;
	@Resource
	private JobDefService jobDefService;
	@Resource
	private JobsConfig jobsConfig;

	@Override
	public void onApplicationEvent(ApplicationStartedEvent event) {
		URL url = jobDefService.getClass().getResource("/");
		File projectfile = new File(url.getFile()).getParentFile().getParentFile();
		log.info("Conf Dir:{}", new File(projectfile, "conf").getAbsolutePath());
		List<JobDef> jobs;
		try {
			jobs = jobDefService.loadFromLocalFile(new File(jobsConfig.getDir()).getAbsolutePath());
			jobDefService.reload(jobs);
		} catch (Exception e) {
			log.error("", e);
		}
	}

}
