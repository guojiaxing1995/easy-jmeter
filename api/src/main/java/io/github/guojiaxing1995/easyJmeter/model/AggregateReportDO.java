package io.github.guojiaxing1995.easyJmeter.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.Date;

@Data
@Document(collection = "aggregate_report")
public class AggregateReportDO implements Serializable {
    private static final long serialVersionUID = 113323427779853001L;

    @Id
    private String id;

    @Indexed
    private Integer projectId;

    @Indexed
    private String label;

    private Long sample;

    private Long error;

    private Double avg;

    private Double median;

    private Double pct90;

    private Double pct95;

    private Double pct99;

    private Double min;

    private Double max;

    private Double tps;

    private Double rb;

    private Double sb;

    @Indexed
    private String text;

    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss.SSSXXX", timezone="Asia/Shanghai")
    private Date startTime;

    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss.SSSXXX", timezone="Asia/Shanghai")
    private Date endTime;

    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss", timezone="Asia/Shanghai")
    private Date createTime;

    @JsonIgnore
    private Date deleteTime;
}