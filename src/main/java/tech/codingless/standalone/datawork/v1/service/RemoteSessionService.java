package tech.codingless.standalone.datawork.v1.service;

import javax.servlet.http.HttpServletRequest;

import lombok.Data;
import tech.codingless.standalone.datawork.v1.data.JobDef;
import tech.codingless.standalone.datawork.v1.data.RemoteSession;

public interface RemoteSessionService {

	public enum Status {
		UNAUTHORIZED, FORBIDDEN, AUTHED_OK
	}

	@Data
	public static class AuthStatus {
		private Status status;
		private RemoteSession session;

	}

	/**
	 * 是否验证
	 * 
	 * @param tenantid
	 * @param request
	 * @return
	 */
	AuthStatus tokenAuthed(String tenantid, JobDef job, HttpServletRequest request);

	AuthStatus jwtAuthed(String tenantid, JobDef job, HttpServletRequest request);

}
