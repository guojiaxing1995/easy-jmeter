package io.github.guojiaxing1995.easyJmeter.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.github.guojiaxing1995.easyJmeter.common.enumeration.JmeterStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CaseInfoVO {

    private Integer id;

    private String name;

    private JmeterStatusEnum status;

    private String project;

    private String jmx;

    private String csv;

    private String jar;

    private String description;

    private String creator;

    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    @JsonIgnore
    private Date deleteTime;
}
