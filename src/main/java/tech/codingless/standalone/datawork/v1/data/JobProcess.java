package tech.codingless.standalone.datawork.v1.data;

import lombok.Data;

@Data
public class JobProcess {
	private String id;
	private String command;
	private String param;
	private String body;
	private Boolean deprecated;
}
