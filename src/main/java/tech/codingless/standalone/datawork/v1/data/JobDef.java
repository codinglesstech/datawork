package tech.codingless.standalone.datawork.v1.data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Data;
import tech.codingless.standalone.datawork.util.StringUtil;

@Data
public class JobDef {
	public static enum TYPE {
		API, PKG;

		public static TYPE of(String type) {
			if ("api".equalsIgnoreCase(type.trim())) {
				return API;
			}
			if ("package".equalsIgnoreCase(type.trim())) {
				return PKG;
			}
			return API;
		}
	}

	private Map<String, String> envrequire;
	private Boolean envmatched; // 环境是否匹配到了
	private String version;
	private String id;
	private String database;
	private String title;
	/**
	 * 用html格式写的说明，非常有意思，可以写架构说明，或是任何相关的说明文档
	 */
	private String htmlsee;
	private Boolean runable;
	/**
	 * 严格的，在严格的模式下，如果一个过程出错了，则退出程序
	 */
	private Boolean strict;
	private String author;
	private String md5;
	private Map<String, String> deprecatedTags = new HashMap<>();
	private List<JobProcess> process;
	private List<Tuple2<String, String>> def;
	private String content;
	private String auth;
	private List<String> roles;
	// 类型
	private TYPE type;
	private String topics;
	private Boolean listened;
	private String ws;
	/**
	 * 过滤数据用的
	 */
	private String filter;
	/**
	 * 是否允许API访问
	 */
	private String mockparam;
	private Boolean api;
	private String method;
	private String path1;
	private String path2;
	private String path3;
	/**
	 * 模版
	 */
	private String template;
	private JobStat stat = new JobStat();
	/**
	 * 代码
	 */
	private String code;
	private String importPackages;
	/**
	 * 限流,是否开启限流
	 */
	private Boolean enableRateLimited;
	private String limit;
	/**
	 * 缓存
	 */
	private String cached;

	/**
	 * 是否进行TOKEN验证
	 * 
	 * @return
	 */
	public boolean isTokenAuthed() {
		return StringUtil.isNotEmpty(auth) && auth.toLowerCase().startsWith("token/");
	}

	/**
	 * 是否加解密验证
	 * 
	 * @return
	 */
	public boolean isEncryptionAuthed() {
		return StringUtil.isNotEmpty(auth) && auth.toLowerCase().startsWith("encryption/");
	}

	public boolean isJwtAuthed() {
		return StringUtil.isNotEmpty(auth) && auth.toLowerCase().startsWith("jwt/");
	}

	public String getJobType() {
		return type != null ? type.name() : TYPE.API.name();
	}

}
