package tech.codingless.standalone.datawork.v1.service.impl;

import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Resource;
import javax.sql.DataSource;

import org.springframework.stereotype.Service;

import com.alibaba.druid.pool.DruidDataSource;

import tech.codingless.standalone.datawork.config.DataSourceConfig;
import tech.codingless.standalone.datawork.config.DataSourceConfig.Item;
import tech.codingless.standalone.datawork.v1.service.DataSourceService;

@Service
public class DataSourceServiceImpl implements DataSourceService {
	private ConcurrentHashMap<String, DataSource> cache = new ConcurrentHashMap<>();
	@Resource
	private DataSourceConfig dataSourceConfig;

	@Override
	public DataSource getDataSource(String dataSourceId) {
		dataSourceId = dataSourceId.trim().toLowerCase();
		if (cache.containsKey(dataSourceId)) {
			return cache.get(dataSourceId);
		}

		synchronized (dataSourceConfig) {
			for (Item conf : dataSourceConfig.getDatasources()) {
				if (conf.getId().equalsIgnoreCase(dataSourceId)) {

					DruidDataSource datasource = new DruidDataSource();
					datasource.setUrl(conf.getUrl());
					datasource.setUsername(conf.getUsername());
					datasource.setPassword(conf.getPassword());
					datasource.setInitialSize(5);
					datasource.setMaxActive(10);
					datasource.setMinIdle(5);
					cache.put(dataSourceId, datasource);
					return datasource;

				}
			}
		}

		return null;
	}

}
