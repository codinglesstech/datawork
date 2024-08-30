package tech.codingless.standalone.datawork.v1.service.impl;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;

import lombok.extern.slf4j.Slf4j;
import tech.codingless.standalone.datawork.config.SysConfig;
import tech.codingless.standalone.datawork.util.Jwts;
import tech.codingless.standalone.datawork.util.Jwts.Playload;
import tech.codingless.standalone.datawork.util.StringUtil;
import tech.codingless.standalone.datawork.v1.data.JobDef;
import tech.codingless.standalone.datawork.v1.data.RemoteSession;
import tech.codingless.standalone.datawork.v1.service.RemoteSessionService;

@Slf4j
@Service
public class RemoteSessionServiceImpl implements RemoteSessionService {
	private ConcurrentHashMap<String, SysConfig.Token> tokendefs = new ConcurrentHashMap<>();
	private ConcurrentHashMap<String, RemoteSession> sessions = new ConcurrentHashMap<>();
	private ConcurrentHashMap<String, SysConfig.JwtConfig> jwtconfigs = new ConcurrentHashMap<>();
	@Resource
	private SysConfig sysConfig;

	@Override
	public AuthStatus jwtAuthed(String tenantid, JobDef job, HttpServletRequest request) {
		AuthStatus status = new AuthStatus();
		if (!job.isJwtAuthed()) {
			status.setStatus(Status.AUTHED_OK);
			return status;
		}
		String authorizationStr = request.getHeader("Authorization");
		if (StringUtil.isEmpty(authorizationStr)) {
			status.setStatus(Status.UNAUTHORIZED);
			return status;
		}
		SysConfig.JwtConfig jwtconfig = jwtconfigs.get(tenantid.toLowerCase());
		if (jwtconfig == null) {
			for (SysConfig.JwtConfig item : sysConfig.getJwts()) {
				if (tenantid.equalsIgnoreCase(item.getTenant())) {
					jwtconfig = item;
					jwtconfigs.put(tenantid.toLowerCase(), item);
				}
			}
		}
		if (jwtconfig == null) {
			status.setStatus(Status.UNAUTHORIZED);
			return status;
		}
		String jwtString = authorizationStr.split(" ")[1];
		Playload playload = Jwts.decode(jwtString, jwtconfig.getSecret());
		if (!playload.isVaild()) {
			status.setStatus(Status.UNAUTHORIZED);
			return status;
		}
		if (playload.isExpired()) {
			// status.setStatus(Status.UNAUTHORIZED);
			// return status;
		}
		status.setStatus(Status.AUTHED_OK);
		JSONObject playloadContent = JSON.parseObject(playload.getPlayload());
		/**
		 * 该字段根据tsw来定义
		 */
		RemoteSession session = new RemoteSession();
		session.setUserId(playloadContent.getString("UserId"));
		session.setUserName(playloadContent.getString("UserName"));
		session.setUserPhone(playloadContent.getString("PhoneNumber"));
		session.setRoles(new HashMap<>());
		session.getRoles().put("userType", playloadContent.getString("UserType"));
		session.getRoles().put("DataPermissions", playloadContent.getString("DataPermissions"));
		session.getRoles().put("PermissionGroupId", playloadContent.getString("PermissionGroupId"));
		session.setCompanyId("tsw");
		status.setSession(session);
		return status;
	}

	/**
	 * 验证Token权限
	 */
	@Override
	public AuthStatus tokenAuthed(String tenantid, JobDef job, HttpServletRequest request) {
		AuthStatus status = new AuthStatus();

		if (!job.isTokenAuthed()) {
			status.setStatus(Status.AUTHED_OK);
			return status;
		}
		SysConfig.Token token = tokendefs.get(tenantid.toLowerCase());
		if (token == null) {
			for (SysConfig.Token item : sysConfig.getTokens()) {
				if (tenantid.equalsIgnoreCase(item.getTenant())) {
					token = item;
					tokendefs.put(tenantid.toLowerCase(), item);
				}
			}
		}
		if (token == null) {
			status.setStatus(Status.UNAUTHORIZED);
			return status;
		}
		// find token
		String headerToken = request.getHeader(token.getHeader());
		if (StringUtil.isEmpty(headerToken)) {
			status.setStatus(Status.UNAUTHORIZED);
			return status;
		}

		// remove expired session
		sessions.values().stream().filter(item -> item.getExpiredAt() < System.currentTimeMillis()).forEach(item -> {
			sessions.remove(item.getToken());
		});

		RemoteSession session = sessions.get(headerToken);
		if (session == null || session.getExpiredAt() < System.currentTimeMillis()) {
			// session expired
			// find new Session

		}
		if (session == null) {
			status.setStatus(Status.UNAUTHORIZED);
			return status;
		}
		status.setSession(session);
		if (CollectionUtils.isEmpty(job.getRoles())) {
			status.setStatus(Status.AUTHED_OK);
			return status;

		}
		if (session.getRoles() == null || session.getRoles().isEmpty()) {
			status.setStatus(Status.FORBIDDEN);
			return status;
		}

		RemoteSession session1 = session;
		boolean matchedRole = job.getRoles().stream().filter(role -> session1.getRoles().containsKey(role)).count() > 0;
		if (matchedRole) {
			status.setStatus(Status.AUTHED_OK);
			return status;
		}
		status.setStatus(Status.FORBIDDEN);
		return status;
	}

}
