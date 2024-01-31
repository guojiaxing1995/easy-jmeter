package io.github.guojiaxing1995.easyJmeter.common.configuration;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InfluxDBConfiguration {

    @Autowired
    private InfluxDBProperties influxDBProperties;

    @Bean
    public InfluxDB influxDB() {
        InfluxDB influxDB = InfluxDBFactory.connect(influxDBProperties.getUrl(), influxDBProperties.getUser(), influxDBProperties.getPassword());
        influxDB.query(new Query("CREATE DATABASE " + influxDBProperties.getDatabase()));
        influxDB.setDatabase(influxDBProperties.getDatabase());
        return influxDB;
    }
}
