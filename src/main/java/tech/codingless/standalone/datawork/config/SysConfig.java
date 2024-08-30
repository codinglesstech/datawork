package tech.codingless.standalone.datawork.config;

import java.util.List;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@ConfigurationProperties(prefix = "codingless")
@Configuration
@Data
public class SysConfig {

	private List<Item> users;
	private List<Token> tokens;
	private List<JwtConfig> jwts;
	private List<ElasticsearchConfig> elasticsearchs;
	private List<RocketmqConfig> rocketmqs;
	private List<RedisConfig> redis;
	private Map<String, String> envs;
	private List<MongoDbConfig> mongodbs;
	private ScheduledConfig scheduled;

	/**
	 * 静止自动启动定时任务，往往用在本地调试阶段
	 * 
	 * @return
	 */
	public boolean isDisabledScheduled() {
		if (scheduled == null) {
			return false;
		}
		if ("disable".equalsIgnoreCase(scheduled.getStatus())) {
			return true;
		}
		return false;
	}

	@Data
	public static class Item {
		private String name;
		private String password;
		private String role;
	}

	@Data
	public static class Token {
		private String tenant;
		private String header;
		private String method;
		private String sessionquery;
	}

	@Data
	public static class JwtConfig {
		private String tenant;
		private String alg;
		private String secret;
	}

	@Data
	public static class ElasticsearchConfig {
		private String id;
		private String hosts;
		private String username;
		private String password;
	}

	@Data
	public static class RocketmqConfig {
		private String id;
		private String namesrvAddr;
		private String accessKey;
		private String secretKey;
		private String consumer;
		private String producer;
	}

	@Data
	public static class RedisConfig {
		private String id;
		private String host;
		private Integer port;
		private String pwd;
		private Integer database;
	}

	@Data
	public static class MongoDbConfig {
		private String id;
		private String hosts;
		private String username;
		private String password;
		private String database;

	}

	@Data
	public static class ScheduledConfig {
		private String status;
		private String allowedTimeRange;
	}
}
