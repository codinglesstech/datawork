package tech.codingless.standalone.datawork.v1.data;

import java.util.Map;

import lombok.Data;

@Data
public class RemoteSession {
	private String token;
	private String userId;
	private String userName;
	private String userPhone;
	private String deptId;
	private String deptName;
	private String companyId;
	private long expiredAt;
	private Map<String, String> roles;
}
