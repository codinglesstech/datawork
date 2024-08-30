package tech.codingless.standalone.datawork;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 
 * @author 王鸿雁
 * @Date 2024年5月15日
 *
 */
@EnableScheduling
@EnableTransactionManagement
@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class })
@ComponentScan(basePackages = { "tech.codingless.standalone.datawork" })
public class CodinglessStandaloneDataworkApplication {
	public static void main(String[] args) {
		SpringApplication.run(CodinglessStandaloneDataworkApplication.class, args);
		// GracefulShutdownCallback
	}

}
