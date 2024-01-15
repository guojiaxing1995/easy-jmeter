package io.github.guojiaxing1995.easyJmeter.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
@Document(collection = "statistics")
public class StatisticsDO implements Serializable {

    private static final long serialVersionUID = 113640546085727160L;

    @Id
    private String caseId;

    private String name;

    private Integer taskNum;

    private Long durationSum;

    private Long samplesSum;

    private Long errorsSum;

    private Double averageThroughput;

    private Double averageErrorRate;

    private Double averageResponseTime;

    private Double averageMinResponseTime;

    private Double averageMaxResponseTime;

    private Double average90thResponseTime;

    private Double average95thResponseTime;

    private Double average99thResponseTime;

    private Double averageMedianResponseTime;

    private Double averageReceived;

    private Double averageSent;

    private List<String> taskIdList;

    private List<List<Object>> throughputData;

    private List<List<Object>> errorRateData;

    private List<List<Object>> responseTime90thData;

    private List<List<Object>> responseTime95thData;

    private List<List<Object>> responseTime99thData;

    private List<List<Object>> responseTimeMedianData;

    private List<List<Object>> responseTimeAverageData;

    private Map<String, Object> graphData;

    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss", timezone="Asia/Shanghai")
    private Date updateTime;
}
