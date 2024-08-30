package tech.codingless.standalone.datawork.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@ConfigurationProperties(prefix = "codingless")
@Configuration
@Data
public class DataSourceConfig {

	private List<Item> datasources;

	@Data
	public static class Item {
		private String id;
		private String url;
		private String username;
		private String password;
	}
}
