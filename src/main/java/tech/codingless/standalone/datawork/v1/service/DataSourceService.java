package tech.codingless.standalone.datawork.v1.service;

import javax.sql.DataSource;

public interface DataSourceService {

	DataSource getDataSource(String dataSourceId);
}
