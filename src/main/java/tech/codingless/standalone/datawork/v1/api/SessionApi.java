package tech.codingless.standalone.datawork.v1.api;

import javax.annotation.Resource;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson2.JSONObject;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import tech.codingless.standalone.datawork.config.Authed;
import tech.codingless.standalone.datawork.config.SysConfig;
import tech.codingless.standalone.datawork.util.StringUtil;
import tech.codingless.standalone.datawork.v1.service.SessionService;

@Slf4j
@RestController
@RequestMapping(value = "/v1/session")
public class SessionApi {

	@Resource
	private SysConfig usersConfig;
	@Resource
	private SessionService sessionService;

	private final static String SENCE = "console";

	@Data
	public static class CreateSessionParam {
		private String userName;
		private String password;
	}

	@PostMapping(value = "/create")
	public String create(@RequestBody CreateSessionParam param) {
		JSONObject response = new JSONObject();
		if (StringUtil.isEmpty(param.getUserName(), param.getPassword())) {
			return response.toString();
		}

		SysConfig.Item user = usersConfig.getUsers().stream().filter(item -> item.getName().equalsIgnoreCase(param.getUserName()) && param.getPassword().equals(item.getPassword())).findAny()
				.orElse(null);

		if (user == null) {
			response.put("status", "user_invalid");
			return response.toString();
		}
		String token = StringUtil.random(32) + StringUtil.md5(System.currentTimeMillis() + param.getUserName());

		SessionService.Session session = new SessionService.Session();
		session.setRole(user.getRole());
		session.setUserName(user.getName());
		session.setToken(token);
		sessionService.upToken(SENCE, session);
		response.put("token", token);
		return response.toString();
	}

	@Authed
	@GetMapping(value = "/clear")
	public String clear() {
		JSONObject response = new JSONObject();
		sessionService.logout();
		return response.toString();
	}

}
