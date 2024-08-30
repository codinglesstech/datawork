package tech.codingless.standalone.datawork.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Order(2)
@Configuration
@ComponentScan(basePackages = { "tech.codingless.standalone.datawork" })
public class DataWorkConfig {

}
