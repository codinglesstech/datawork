package tech.codingless.standalone.datawork.config;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.MDC;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;
import tech.codingless.standalone.datawork.util.StringUtil;
import tech.codingless.standalone.datawork.v1.service.impl.JobDefServiceImpl;

public class JobLogFilter extends Filter<ILoggingEvent> {
	private static SimpleDateFormat sdf = new SimpleDateFormat("MM.dd HH:mm:ss.SSS");

	@Override
	public FilterReply decide(ILoggingEvent event) {
		String jobId = MDC.get("JOBID");
		if (StringUtil.isEmpty(jobId)) {
			return FilterReply.ACCEPT;
		}

		String message = event.getFormattedMessage();
		if (event.getLevel() == Level.ERROR) {
			/**
			 * 如果碰到ERROR，则错误堆打印出来，以便找问题
			 */
			StringBuilder sb = new StringBuilder();
			sb.append(event.getThrowableProxy().getClassName());
			sb.append(":");
			sb.append(event.getThrowableProxy().getMessage()).append("\n\t");
			for (StackTraceElementProxy item : event.getThrowableProxy().getStackTraceElementProxyArray()) {
				StackTraceElement te = item.getStackTraceElement();
				sb.append("at ").append(te.getClassName()).append(".").append(te.getMethodName());
				sb.append("(");
				sb.append(te.getFileName()).append(":").append(te.getLineNumber());
				sb.append(")");
				sb.append("\n\t");
			}
			message = sb.toString();
		}

		StringBuilder sb = new StringBuilder();
		sb.append(jobId).append("\t");
		sb.append(sdf.format(new Date(event.getTimeStamp())));
		sb.append(" [").append(MDC.get("TRACEID")).append("] ");
		sb.append(" [").append(StringUtil.substring(event.getLevel().toString(), 2)).append("] ");

		sb.append(StringUtil.endSubstring(event.getLoggerName(), 20)).append(":");
		sb.append(event.getCallerData()[0].getLineNumber()).append(" - ");
		sb.append(message);

		JobDefServiceImpl.LOG_QUEUE.offer(sb);
		return FilterReply.ACCEPT;
	}

}
