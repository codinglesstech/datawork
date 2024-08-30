package tech.codingless.standalone.datawork.v1.api;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson2.JSON;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import tech.codingless.standalone.datawork.config.JobsConfig;
import tech.codingless.standalone.datawork.config.SysConfig;
import tech.codingless.standalone.datawork.helper.MemoryObservationHelper;
import tech.codingless.standalone.datawork.util.BooleanUtil;
import tech.codingless.standalone.datawork.util.ExcelOutputUtil;
import tech.codingless.standalone.datawork.util.SnowFlakeNumberUtil;
import tech.codingless.standalone.datawork.util.StringUtil;
import tech.codingless.standalone.datawork.v1.data.JobDef;
import tech.codingless.standalone.datawork.v1.data.ProcessResponse;
import tech.codingless.standalone.datawork.v1.service.JobDefService;
import tech.codingless.standalone.datawork.v1.service.JobExecuteService;
import tech.codingless.standalone.datawork.v1.service.JobRunnerService;
import tech.codingless.standalone.datawork.v1.service.RemoteSessionService;
import tech.codingless.standalone.datawork.v1.service.RemoteSessionService.AuthStatus;

@Slf4j
@RestController
@RequestMapping(value = "/v1/api")
public class RestApi {

	@Resource
	private JobRunnerService jobRunnerService;
	@Resource
	private JobDefService jobDefService;

	@Resource
	private JobsConfig jobsConfig;

	@Resource
	private SysConfig sysConfig;

	@Resource
	private JobExecuteService jobExecuteService;

	@Resource
	private RemoteSessionService remoteSessionService;

	@PostMapping(value = "/{tenantid}/{sys}/{module}/{function}")
	public ResponseEntity<byte[]> api(@PathVariable("tenantid") String tenantid, @PathVariable("sys") String sys, @PathVariable("module") String module, @PathVariable("function") String function,
			@RequestBody String body, HttpServletResponse response, HttpServletRequest request) {

		if (!body.startsWith("{") && body.contains("&")) {
			// 不以{开头，意味着，用户传的可能是form形的参数
			Map<String, String> map = new HashMap<>();
			List.of(body.split("&")).forEach(item -> {
				String[] strs = item.split("=");
				if (strs.length != 2) {
					return;
				}
				try {
					map.put(strs[0], URLDecoder.decode(strs[1], "utf-8"));
				} catch (UnsupportedEncodingException e) {
					log.error("", e);
				}
			});
			body = JSON.toJSONString(map);
		}
		return this.process("POST", tenantid, sys, module, function, response, request, body);

	}

	@GetMapping(value = "/{tenantid}/{path1}/{path2}/{path3}")
	public ResponseEntity<byte[]> get(@PathVariable("tenantid") String tenantid, @PathVariable("path1") String path1, @PathVariable("path2") String path2, @PathVariable("path3") String path3,
			HttpServletResponse response, HttpServletRequest request) {
		return this.process("GET", tenantid, path1, path2, path3, response, request, "");
	}

	private ResponseEntity<byte[]> process(String method, String tenantid, String path1, String path2, String path3, HttpServletResponse response, HttpServletRequest request, String body) {
		HttpHeaders headers = new HttpHeaders();
		log.info("tenantid:{},path1:{},path2:{},path3:{},body:{}", tenantid, path1, path2, path3);
		String id = "";
		if ("GET".equalsIgnoreCase(method)) {
			id = "GET:" + path1 + ":" + path2 + ":*";
		} else {
			id = "POST:" + path1 + ":" + path2 + ":" + path3;
		}

		JobDef job = jobDefService.get(id);
		// 是否调用预发代码
		boolean usePrecode = "enable".equalsIgnoreCase(request.getHeader("x-precode"));
		if (usePrecode) {
			job = jobDefService.getpre(id);
		}
		if (job == null) {
			headers.setContentType(MediaType.TEXT_HTML);
			return new ResponseEntity<byte[]>("404".getBytes(), headers, HttpStatus.NOT_FOUND);
		}

		AuthStatus authstatus = null;
		if (job.isTokenAuthed()) {
			authstatus = remoteSessionService.tokenAuthed(tenantid, job, request);
			RemoteSessionService.Status status = authstatus.getStatus();
			if (status == RemoteSessionService.Status.UNAUTHORIZED) {
				log.warn("Token authed fail");
				return new ResponseEntity<byte[]>("401".getBytes(), headers, HttpStatus.UNAUTHORIZED);
			} else if (status == RemoteSessionService.Status.FORBIDDEN) {
				log.warn("Token authed fail");
				return new ResponseEntity<byte[]>("403".getBytes(), headers, HttpStatus.FORBIDDEN);
			}
		}

		if (job.isJwtAuthed()) {
			log.info("use jwt auth");
			authstatus = remoteSessionService.jwtAuthed(tenantid, job, request);
			RemoteSessionService.Status status = authstatus.getStatus();
			if (status == RemoteSessionService.Status.UNAUTHORIZED) {
				log.warn("jwt authed fail");
				return new ResponseEntity<byte[]>("401".getBytes(), headers, HttpStatus.UNAUTHORIZED);
			} else if (status == RemoteSessionService.Status.FORBIDDEN) {
				log.warn("jwt authed fail");
				return new ResponseEntity<byte[]>("403".getBytes(), headers, HttpStatus.FORBIDDEN);
			}
		}

		Map<String, Object> context = new HashMap<>();
		Map<String, String> param = new HashMap<>();
		for (Enumeration<String> e = request.getParameterNames(); e.hasMoreElements();) {
			String name = e.nextElement();
			param.put(name, request.getParameter(name));
		}

		context.put("_path1_", path1);
		context.put("_path2_", path2);
		context.put("_path3_", path3);
		if ("GET".equalsIgnoreCase(method)) {
			context.put("_id_", path3);
		}
		context.put("_body_", body);

		context.put("_param_", param);
		context.put("_tenantid_", tenantid);
		String reqId = "R" + Long.toString(SnowFlakeNumberUtil.nextId());
		context.put("_REQ_ID_", reqId);

		if (authstatus != null) {
			context.put("_session_", authstatus.getSession());
		}

		boolean countMemoryUsed = "enable".equalsIgnoreCase(request.getHeader("x-count-memory-used"));

		try {
			if (countMemoryUsed) {
				MemoryObservationHelper.init();
			}
			ProcessResponse pr = new ProcessResponse();

			// 是否压缩Excel成zip然后输出
			AtomicBoolean excelZipCompress = new AtomicBoolean(false);
			Zip zip = new Zip();
			/**
			 * excel
			 */
			pr.onOutputExcel(result -> {

				if (BooleanUtil.isTrue(result.getZip())) {
					// 需要进行ZIP压缩
					excelZipCompress.set(true);
				}

				if (excelZipCompress.get()) {
					try {
						// 第一个excel返回的时候创建输出流
						if (zip.getZipStream() == null) {

							String zipName = result.getZipName();
							if (StringUtil.isEmpty(zipName)) {
								zipName = "unnamed";
							} else {
								zipName = URLEncoder.encode(zipName, "utf-8");
							}
							response.setContentType("application/zip");
							response.setHeader("Content-Disposition", "attachment;filename=" + zipName + ".zip");
							response.setHeader("req-id", reqId);
							zip.setZipStream(new ZipOutputStream(response.getOutputStream()));
						}
						String fileName = result.getName() + ".xlsx";
						ZipEntry entry = new ZipEntry(fileName);
						zip.getZipStream().putNextEntry(entry);
						ByteArrayOutputStream baos = new ByteArrayOutputStream();
						ExcelOutputUtil.write(result, baos);
						zip.getZipStream().write(baos.toByteArray());
						zip.getZipStream().closeEntry();

						log.info("Zip entry:{}", fileName);
					} catch (Exception e) {
						log.error("", e);
					}
					return;
				}

				try {
					String excelName = URLEncoder.encode(result.getName(), "utf-8");
					response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
					response.setHeader("Content-Disposition", "attachment;filename=" + excelName + ".xlsx");
					response.setHeader("req-id", reqId);
					if (countMemoryUsed) {
						response.setHeader("x-count-memory-used", Long.toString(MemoryObservationHelper.size()));
					}

					ExcelOutputUtil.write(result, response.getOutputStream());
					response.getOutputStream().flush();
					response.getOutputStream().close();
				} catch (IOException e) {
					log.error("", e);
				}
			});

			/**
			 * 结束
			 */
			pr.onFinished(action -> {
				if (excelZipCompress.get()) {
					try {
						zip.getZipStream().close();
						// response.getOutputStream().flush();
						// response.getOutputStream().close();
					} catch (Exception e) {
						log.error("", e);
					}

				}
				try {
					response.getOutputStream().flush();
					response.getOutputStream().close();
				} catch (Exception e) {
					log.error("", e);
				}
			});

			/**
			 * JSON
			 */
			AtomicInteger type = new AtomicInteger(0);
			StringBuilder jsonbuilder = new StringBuilder();
			pr.onOutputJson(result -> {
				type.set(1);
				jsonbuilder.append(JSON.toJSONString(result));

			});

			try {
				jobExecuteService.execute(job, context, pr);
				MemoryObservationHelper.increment(context);
			} catch (Throwable e) {
				response.setHeader("req-id", reqId);
				log.error("fail", e);
				return null;
			}

			if (countMemoryUsed) {
				headers.set("x-count-memory-used", Long.toString(MemoryObservationHelper.size()));
			}

			switch (type.get()) {
			case 1:

				String responseJsonBody = jsonbuilder.toString();

				headers.setContentType(MediaType.APPLICATION_JSON);
				headers.set("req-id", reqId);
				return new ResponseEntity<byte[]>(responseJsonBody.getBytes(), headers, HttpStatus.OK);

			}
			headers.set("req-id", reqId);
			headers.setContentType(MediaType.TEXT_HTML);
			return new ResponseEntity<byte[]>("-1".getBytes(), headers, HttpStatus.OK);
		} finally {
			MemoryObservationHelper.clear();
		}

	}

	@Data
	public static class Zip {
		ZipOutputStream zipStream;
	}

}
