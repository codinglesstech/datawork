package tech.codingless.standalone.datawork.config;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 权限认证
 * 
 * @author WHY
 *
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Authed {

	/**
	 * 是否必要的
	 * 
	 * @return
	 */
	boolean required() default true;
}
