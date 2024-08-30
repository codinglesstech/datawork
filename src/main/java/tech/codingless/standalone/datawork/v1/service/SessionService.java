package tech.codingless.standalone.datawork.v1.service;

import lombok.Data;

public interface SessionService {

	@Data
	public static class Session {
		private String userName;
		private String role;
		private String token;
	}

	boolean upToken(String sence, Session session);

	Session getSession();

	boolean setCurrentSession(String token);

	void clearCache();

	void logout();
}
