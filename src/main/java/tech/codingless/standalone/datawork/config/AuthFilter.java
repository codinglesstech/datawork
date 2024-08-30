package tech.codingless.standalone.datawork.config;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.AsyncHandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import lombok.extern.slf4j.Slf4j;
import tech.codingless.standalone.datawork.util.StringUtil;
import tech.codingless.standalone.datawork.v1.service.SessionService;

/**
 * 
 * @author 王鸿雁
 * @Date 2024年5月21日
 *
 */
@Slf4j
@Component
public class AuthFilter implements AsyncHandlerInterceptor {

	@Resource
	private SessionService sessionService;
	private final static String xRequestIdParan = "x-[0-9a-zA-Z]{30,60}";

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

		AsyncHandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		if (!(handler instanceof HandlerMethod)) {
			return AsyncHandlerInterceptor.super.preHandle(request, response, handler);
		}

		HandlerMethod handlerMethod = (HandlerMethod) handler;

		Authed authed = handlerMethod.getMethodAnnotation(Authed.class);
		if (authed == null || !authed.required()) {
			return AsyncHandlerInterceptor.super.preHandle(request, response, handler);
		}
		String token = request.getHeader("x-token");
		if (StringUtil.isEmpty(token)) {
			log.info("Not Found x-token From header");
			response.setStatus(403);
			return false;
		}
		boolean vaild = sessionService.setCurrentSession(token);
		if (!vaild) {
			log.info("x-token is invalid");
			response.setStatus(403);
			return false;
		}

		return AsyncHandlerInterceptor.super.preHandle(request, response, handler);
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
		try {

			AsyncHandlerInterceptor.super.afterCompletion(request, response, handler, ex);
		} finally {
			sessionService.clearCache();
			MDC.clear();

		}

	}

}
