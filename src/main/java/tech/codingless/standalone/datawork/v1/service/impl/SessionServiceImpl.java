package tech.codingless.standalone.datawork.v1.service.impl;

import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson2.JSON;

import tech.codingless.standalone.datawork.util.StringUtil;
import tech.codingless.standalone.datawork.v1.service.RedisService;
import tech.codingless.standalone.datawork.v1.service.SessionService;

@Service
public class SessionServiceImpl implements SessionService {
	private static final ConcurrentHashMap<String, Session> TOKENS = new ConcurrentHashMap<>();
	private static ThreadLocal<Session> localSession = new ThreadLocal<>();

	@Resource
	private RedisService redisService;

	@Override
	public boolean upToken(String sence, Session session) {
		if (redisService.hasDefaultRedis()) {
			redisService.set("default", "token." + session.getToken(), JSON.toJSONString(session));
			redisService.set("default", "user." + session.getUserName().toLowerCase(), JSON.toJSONString(session));
		} else {
			TOKENS.put("token." + session.getToken(), session);
			TOKENS.put("user." + session.getUserName().toLowerCase(), session);
		}
		return true;
	}

	@Override
	public Session getSession() {
		return localSession.get();
	}

	@Override
	public void logout() {
		Session session = localSession.get();
		if (session == null) {
			return;
		}
		String tokenKey = "token." + session.getToken();
		String userKey = "user." + session.getUserName().toLowerCase();

		if (redisService.hasDefaultRedis()) {
			redisService.del("default", tokenKey);
			redisService.del("default", userKey);
			return;
		}

		TOKENS.remove(tokenKey);
		TOKENS.remove(userKey);
	}

	@Override
	public boolean setCurrentSession(String token) {
		if (redisService.hasDefaultRedis()) {
			String sessionstr = redisService.get("default", "token." + token);
			if (StringUtil.isEmpty(sessionstr)) {
				return false;
			}
			Session ssession = JSON.parseObject(sessionstr, Session.class);
			if (StringUtil.isNotEmpty(ssession.getToken())) {
				localSession.set(ssession);
				return true;
			}
			return false;
		}

		Session session = TOKENS.get("token." + token);
		if (session == null) {
			return false;
		}
		localSession.set(session);
		return true;
	}

	@Override
	public void clearCache() {
		localSession.remove();
	}

}
