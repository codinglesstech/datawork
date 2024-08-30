package tech.codingless.standalone.datawork.v1.service;

import java.util.List;
import java.util.Map;

public interface RedisService {

	public void set(String redis, String key, String value, Long expiredtime);

	public void set(String redis, String key, String value);

	public String get(String redis, String key);

	public Boolean del(String redis, String key);

	public void mset(String redis, String key, Map<String, String> map);

	public List<Object> mget(String redis, String key, List<String> columns);

	public Long lpush(String redis, String key, List<String> values);

	public List<String> lpop(String redis, String key, int size);

	public Long llen(String redis, String key);

	public boolean exist(String redis);

	public boolean hasDefaultRedis();

	public Long eval(String redis, String script, List<String> parameter);

}
