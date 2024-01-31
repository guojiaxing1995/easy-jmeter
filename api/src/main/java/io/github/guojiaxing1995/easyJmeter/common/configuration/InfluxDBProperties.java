package io.github.guojiaxing1995.easyJmeter.common.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Data
@ConfigurationProperties(prefix = "spring.influx")
public class InfluxDBProperties {

    private String url;
    private String user;
    private String password;
    private String database;

}
