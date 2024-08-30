package tech.codingless.standalone.datawork.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@ConfigurationProperties(prefix = "codingless.jobs")
@Configuration
@Data
public class JobsConfig {

	private String dir;
	private String upscript;

}
