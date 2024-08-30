package tech.codingless.standalone.datawork.v1.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import lombok.extern.slf4j.Slf4j;
import tech.codingless.standalone.datawork.config.SysConfig;
import tech.codingless.standalone.datawork.util.BooleanUtil;
import tech.codingless.standalone.datawork.util.StringUtil;
import tech.codingless.standalone.datawork.v1.data.JobDef;
import tech.codingless.standalone.datawork.v1.data.JobProcess;
import tech.codingless.standalone.datawork.v1.data.Tuple2;
import tech.codingless.standalone.datawork.v1.service.JobDefService;

@Slf4j
@Service
public class JobDefServiceImpl implements JobDefService {
	/**
	 * 预发API
	 */
	private static ConcurrentHashMap<String, JobDef> PRE_API = new ConcurrentHashMap<>();

	private static ConcurrentHashMap<String, JobDef> JOBS_CACHE = new ConcurrentHashMap<>();
	public static BlockingQueue<StringBuilder> LOG_QUEUE = new ArrayBlockingQueue<>(2048);
	private static ConcurrentHashMap<String, BlockingQueue<String>> JOBS_LOG_CACHE = new ConcurrentHashMap<>();

	@Resource
	private SysConfig sysconfig;

	public JobDefServiceImpl() {

		Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {
				StringBuilder event = null;
				while (true) {
					try {
						event = LOG_QUEUE.take();
						String strlog = event.toString();
						String[] columns = strlog.split("\t", 2);
						BlockingQueue<String> cache = JOBS_LOG_CACHE.get(columns[0]);
						if (cache == null) {
							cache = new ArrayBlockingQueue<>(1024);
							JOBS_LOG_CACHE.put(columns[0], cache);
						}
						if (cache.size() > 1000) {
							cache.poll();
						}
						cache.offer(columns[1]);

					} catch (InterruptedException e) {
						log.error("", e);
					}
				}

			}
		});
		thread.setName("joblogconsumer");
		thread.start();
	}

	@Override
	public BlockingQueue<String> logs(String jobId) {
		return JOBS_LOG_CACHE.get(jobId);
	}

	@Override
	public List<String> traceLogs(String traceId) {
		for (BlockingQueue<String> logslist : JOBS_LOG_CACHE.values()) {
			List<String> matchedLogs = logslist.stream().filter(item -> item.indexOf(traceId) > -1).collect(Collectors.toList());
			if (!CollectionUtils.isEmpty(matchedLogs)) {
				return matchedLogs;
			}
		}
		return Collections.emptyList();
	}

	@Override
	public long reload(List<JobDef> jobs) {
		jobs.forEach(job -> {
			JOBS_CACHE.put(job.getId(), job);
		});
		JOBS_CACHE.values().forEach(job -> {
			log.info("found job [{}]:{}", BooleanUtil.isTrue(job.getRunable()) ? "Runable" : "Disable", job);
		});
		this.prereload(jobs);
		return JOBS_CACHE.size();
	}

	@Override
	public long prereload(List<JobDef> jobs) {
		jobs.stream().filter(item -> item.getType() == JobDef.TYPE.API || BooleanUtil.isTrue(item.getApi())).forEach(api -> {
			PRE_API.put(api.getId(), api);
		});
		return PRE_API.size();
	}

	@Override
	public JobDef get(String id) {
		return JOBS_CACHE.get(id);
	}

	@Override
	public JobDef getpre(String id) {
		return PRE_API.get(id);
	}

	@Override
	public List<JobDef> jobs() {
		return JOBS_CACHE.values().stream().collect(Collectors.toList());
	}

	@Override
	public List<JobDef> parse(String configContent) {
		// log.info("ConfigContent:{}", StringUtil.substring(configContent, 100));
		//
		List<JobDef> list = new ArrayList<>();

		// clean comments
		List<String> pgs = List.of(configContent.split("[\r\n]")).stream().filter(item -> !item.trim().startsWith("#") && item.trim().length() > 0).collect(Collectors.toList());

		log.info("pgs:{}", pgs.size());

		// 解析命令
		JobDef jobdef = null;
		Tuple2<String, StringBuilder> fragment = new Tuple2<>();
		for (String line : pgs) {
			String token = line.trim();
			if (":newjob".equalsIgnoreCase(token)) {
				if (jobdef != null) {
					list.add(jobdef);
				}
				jobdef = new JobDef();
				jobdef.setDef(new ArrayList<>());
				jobdef.setProcess(new ArrayList<>());
				fragment = new Tuple2<>();
				continue;
			}
			if (token.startsWith(":")) {
				if (jobdef != null && fragment != null && StringUtil.isNotEmpty(fragment.getItem1())) {
					jobdef.getDef().add(new Tuple2<>(fragment.getItem1(), fragment.getItem2().toString().trim()));
				}
				fragment = new Tuple2<>();
				fragment.setItem1(token);
				fragment.setItem2(new StringBuilder());
				continue;
			}
			fragment.getItem2().append(token).append("\n");
		}

		if (jobdef != null) {
			list.add(jobdef);
		}

		log.info("list:{}", list.size());
		// print
		list = list.stream().filter(item -> !CollectionUtils.isEmpty(item.getDef())).collect(Collectors.toList());

		log.info("list2:{}", list.size());
		// sign
		list.forEach(job -> {
			StringBuilder sb = new StringBuilder();
			job.getDef().forEach(def -> {
				sb.append(def.getItem1()).append("\n");
				sb.append(def.getItem2()).append("\n");
			});
			job.setMd5(StringUtil.md5(sb.toString()));
		});

		list.forEach(job -> {
			StringBuilder content = new StringBuilder();
			job.getDef().forEach(def -> {
				String cmd = def.getItem1();
				String val = def.getItem2();
				content.append(cmd).append("\n");
				content.append(val).append("\n\n");
				if (":version".equalsIgnoreCase(cmd)) {
					job.setVersion(val);
				} else if (":envrequire".equalsIgnoreCase(cmd)) {
					/**
					 * 环境要求匹配
					 */
					setEvnMatchedFlag(job, val);

				} else if (":strict".equalsIgnoreCase(cmd)) {
					job.setStrict("true".equalsIgnoreCase(val));
				} else if (":id".equalsIgnoreCase(cmd)) {
					job.setId(val);
				} else if (":htmlsee".equalsIgnoreCase(cmd)) {
					job.setHtmlsee(val);
				} else if (":database".equalsIgnoreCase(cmd)) {
					job.setDatabase(val);
				} else if (":cached".equalsIgnoreCase(cmd)) {
					job.setCached(val);
				} else if (":template".equalsIgnoreCase(cmd)) {
					job.setTemplate(val);
				} else if (":title".equalsIgnoreCase(cmd)) {
					job.setTitle(val);
				} else if (":limit".equalsIgnoreCase(cmd)) {
				} else if (":mockparam".equalsIgnoreCase(cmd)) {
					job.setMockparam(val);
				} else if (":author".equalsIgnoreCase(cmd)) {
					job.setAuthor(val);
				} else if (":roles".equalsIgnoreCase(cmd)) {
					if (StringUtil.isNotEmpty(val)) {
						List<String> roles = List.of(val.split(",")).stream().filter(item -> StringUtil.isNotEmpty(item)).map(item -> item.toUpperCase()).collect(Collectors.toList());
						job.setRoles(roles);
					}
				} else if (":code".equalsIgnoreCase(cmd)) {
					job.setCode(val);
				} else if (":import".equalsIgnoreCase(cmd)) {
					job.setImportPackages(val);
				} else if (":type".equalsIgnoreCase(cmd)) {
					job.setType(JobDef.TYPE.of(val));
				} else if (":auth".equalsIgnoreCase(cmd)) {
					job.setAuth(val);
				} else if (":runable".equalsIgnoreCase(cmd)) {
					job.setRunable("true".equalsIgnoreCase(val));
				} else if (":method".equalsIgnoreCase(cmd)) {
					job.setMethod("GET".equalsIgnoreCase(val) ? "GET" : "POST");
				} else if (":path1".equalsIgnoreCase(cmd)) {
					job.setPath1(val);
				} else if (":path2".equalsIgnoreCase(cmd)) {
					job.setPath2(val);
				} else if (":path3".equalsIgnoreCase(cmd)) {
					job.setPath3(val);
				} else if (":api".equalsIgnoreCase(cmd)) {
					job.setApi("true".equalsIgnoreCase(val));
				} else if (":deprecated".equalsIgnoreCase(cmd)) {
					List.of(val.split("\n")).stream().filter(item -> StringUtil.isNotEmpty(item)).forEach(item -> {
						job.getDeprecatedTags().put(item.toLowerCase(), item);
					});
				} else if (cmd.toLowerCase().startsWith(":on-")) {
					// is process if start with on-
					JobProcess process = new JobProcess();
					String strs[] = cmd.split(" ", 2);

					if (strs.length == 2) {
						String param = strs[1].trim();
						process.setParam(param);
						List.of(param.split(" ")).stream().filter(str -> str.toLowerCase().startsWith("id=")).forEach(id -> {
							process.setId(id.split("=")[1].trim());
						});
					}
					if (StringUtil.isEmpty(process.getId())) {
						process.setId("process-" + StringUtil.genGUID());
					}
					process.setCommand(strs[0].trim());
					process.setBody(val);
					job.getProcess().add(process);
				}
			});

			if (BooleanUtil.isTrue(job.getApi())) {
				// 如果是API，则重新设置其ID
				if ("POST".equalsIgnoreCase(job.getMethod())) {
					job.setId("POST:" + job.getPath1() + ":" + job.getPath2() + ":" + job.getPath3());
				} else {
					job.setId("GET:" + job.getPath1() + ":" + job.getPath2() + ":*");
				}
			}

			job.setContent(content.toString());
			// deprecated process mark
			job.getDef().clear();
			job.getProcess().forEach(process -> {
				process.setDeprecated(job.getDeprecatedTags().containsKey(process.getId().toLowerCase()));
			});
			List<JobProcess> processList = job.getProcess().stream().filter(item -> BooleanUtil.isNotTrue(item.getDeprecated())).collect(Collectors.toList());
			job.setProcess(processList);
		});

		list.forEach(job -> {
			log.info("job:{}", job);
		});

		StringBuilder content = new StringBuilder();
		pgs.forEach(item -> {
			content.append(item).append("\n");
		});
		// 将环境不匹配的任务过滤掉
		list = list.stream().filter(item -> BooleanUtil.isNotFalse(item.getEnvmatched())).collect(Collectors.toList());
		// System.out.println(content.toString());
		return list;
	}

	private void setEvnMatchedFlag(JobDef job, String val) {
		// 匹配环境 sysconfig
		if (StringUtil.isEmpty(val)) {
			job.setEnvmatched(true);
			return;
		}

		List<String> envs = List.of(val.split("[\r\n]")).stream().filter(item -> StringUtil.isNotEmpty(item)).map(item -> item.trim()).collect(Collectors.toList());
		if (CollectionUtils.isEmpty(envs)) {
			job.setEnvmatched(true);
			return;
		}

		Map<String, String> vmenvs = sysconfig.getEnvs();
		if (MapUtils.isEmpty(vmenvs)) {
			job.setEnvmatched(false);
			return;
		}

		for (String env : envs) {
			String[] columns = env.split("=");
			String envkey = columns[0].trim().toLowerCase();
			if (!vmenvs.containsKey(envkey)) {
				job.setEnvmatched(false);
				return;
			}
			if (columns.length == 2 && !columns[1].trim().equalsIgnoreCase(vmenvs.get(envkey))) {
				job.setEnvmatched(false);
				return;
			}
		}

	}

	@Override
	public List<JobDef> loadFromLocalFile(String confDir) {
		List<JobDef> jobs = new ArrayList<>();
		File conf = new File(confDir);
		List<File> resouceList = findJobResource(conf);
		for (File jobfile : resouceList) {
			log.info("jobfile:{}", jobfile);
			FileInputStream fis = null;
			try {
				fis = new FileInputStream(jobfile);
				String jobdefcontent = IOUtils.toString(fis, Charset.forName("utf-8"));
				jobs.addAll(parse(jobdefcontent));
			} catch (Exception e) {
				log.error("", e);
			} finally {
				if (fis != null) {
					try {
						fis.close();
					} catch (IOException e) {
						log.error("", e);
					}
				}
			}

		}
		return jobs;
	}

	private List<File> findJobResource(File conf) {
		if (!conf.exists()) {
			return Collections.emptyList();
		}
		if (conf.isFile()) {
			return List.of(conf);
		}
		List<File> list = new ArrayList<>();
		for (File file : conf.listFiles()) {

			if (file.isFile() && isSupportSuffix(file.getName())) {
				list.add(file);
				continue;
			}
			if (file.isDirectory()) {
				list.addAll(this.findJobResource(file));
			}
		}
		return list;
	}

	// 支持的后缀名字，不同功能的脚本取不一样的后缀，让人更容易明白
	private static final List<String> SUFFIX_LIST = List.of(".job", ".job.md", ".process.md", ".sync.md", ".api.md", ".html.md", ".xlsx.md", ".xls.md", ".zip.md", ".pdf.md", ".json.md", ".xml.md",
			".csv.md", ".tsv.md", ".txt.md", ".package.md", ".ws.md");

	private boolean isSupportSuffix(String fileName) {
		for (String suffix : SUFFIX_LIST) {
			if (fileName.endsWith(suffix)) {
				return true;
			}
		}
		return false;
	}

}
