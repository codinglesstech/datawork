package tech.codingless.standalone.datawork.v1.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Service;

import tech.codingless.standalone.datawork.config.SysConfig;
import tech.codingless.standalone.datawork.config.SysConfig.RedisConfig;
import tech.codingless.standalone.datawork.util.AssertUtil;
import tech.codingless.standalone.datawork.util.StringUtil;
import tech.codingless.standalone.datawork.v1.service.RedisService;

@Service
public class RedisServiceImpl implements RedisService {
	private ConcurrentHashMap<String, RedisTemplate<String, String>> redismap = new ConcurrentHashMap<>();
	@Resource
	SysConfig sysConfig;

	private RedisTemplate<String, String> template(String redis, boolean asserterrorifnotexist) {
		String redisId = redis.trim().toLowerCase();
		RedisTemplate<String, String> template = redismap.get(redisId);
		if (template != null) {
			return template;
		}
		if (CollectionUtils.isEmpty(sysConfig.getRedis())) {
			if (asserterrorifnotexist) {
				AssertUtil.assertFail("Redis Not Exist");
			}
			return null;
		}

		Optional<RedisConfig> redisconfig = sysConfig.getRedis().stream().filter(item -> redisId.equalsIgnoreCase(item.getId().trim())).findAny();
		if (redisconfig.isEmpty()) {
			if (asserterrorifnotexist) {
				AssertUtil.assertFail("Redis Not Exist");

			}
			return null;
		}

		synchronized (redismap) {
			template = redismap.get(redisId);
			if (template != null) {
				return template;
			}
			RedisConfig rc = redisconfig.get();
			RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(rc.getHost().trim(), rc.getPort());
			config.setPassword(rc.getPwd().trim());
			config.setDatabase(rc.getDatabase());
			LettuceConnectionFactory factory = new LettuceConnectionFactory(config);
			factory.afterPropertiesSet();
			template = new RedisTemplate<>();
			template.setConnectionFactory(factory);
			template.setDefaultSerializer(new StringRedisSerializer());
			template.setKeySerializer(new StringRedisSerializer());
			// template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
			template.setValueSerializer(new StringRedisSerializer());
			template.afterPropertiesSet();
			redismap.put(redisId, template);
			return template;
		}
	}

	@Override
	public void set(String redis, String key, String value, Long expiredtime) {
		if (StringUtil.hasEmpty(redis, key)) {
			return;
		}
		RedisTemplate<String, String> template = template(redis, true);
		if (expiredtime != null) {
			template.opsForValue().set(key, value, expiredtime, TimeUnit.SECONDS);
		} else {
			template.opsForValue().set(key, value);
		}
	}

	@Override
	public void set(String redis, String key, String value) {
		this.set(redis, key, value, null);
	}

	@Override
	public String get(String redis, String key) {
		if (StringUtil.hasEmpty(redis, key)) {
			return null;
		}
		RedisTemplate<String, String> template = template(redis, true);

		return template.opsForValue().get(key);
	}

	@Override
	public Boolean del(String redis, String key) {
		if (StringUtil.hasEmpty(redis, key)) {
			return null;
		}
		RedisTemplate<String, String> template = template(redis, true);
		return template.delete(key);
	}

	@Override
	public void mset(String redis, String key, Map<String, String> map) {
		if (StringUtil.hasEmpty(redis, key)) {
			return;
		}
		RedisTemplate<String, String> template = template(redis, true);
		template.opsForHash().putAll(key, map);
	}

	@Override
	public List<Object> mget(String redis, String key, List<String> columns) {
		if (StringUtil.hasEmpty(redis, key)) {
			return Collections.emptyList();
		}
		RedisTemplate<String, String> template = template(redis, true);
		List<Object> list = new ArrayList<>();
		list.addAll(columns);
		return template.opsForHash().multiGet(key, list);
	}

	@Override
	public Long lpush(String redis, String key, List<String> values) {
		if (StringUtil.hasEmpty(redis, key)) {
			return null;
		}
		RedisTemplate<String, String> template = template(redis, true);
		return template.opsForList().leftPushAll(key, values);
	}

	@Override
	public List<String> lpop(String redis, String key, int size) {
		if (StringUtil.hasEmpty(redis, key)) {
			return Collections.emptyList();
		}
		RedisTemplate<String, String> template = template(redis, true);

		return template.opsForList().rightPop(key, size);
	}

	@Override
	public Long llen(String redis, String key) {
		if (StringUtil.hasEmpty(redis, key)) {
			return (long) -1;
		}
		RedisTemplate<String, String> template = template(redis, true);
		return template.opsForList().size(key);
	}

	@Override
	public boolean exist(String redis) {
		return template(redis, false) != null;
	}

	@Override
	public boolean hasDefaultRedis() {
		return this.exist("default");
	}

	@Override
	public Long eval(String redis, String script, List<String> parameters) {
		RedisTemplate<String, String> template = template(redis, true);
		DefaultRedisScript<Long> lua = new DefaultRedisScript<>(script, Long.class);
		return template.execute(lua, parameters);
	}

}
