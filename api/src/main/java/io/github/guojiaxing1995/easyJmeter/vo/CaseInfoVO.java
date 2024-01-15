package io.github.guojiaxing1995.easyJmeter.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.github.guojiaxing1995.easyJmeter.common.enumeration.JmeterStatusEnum;
import io.github.guojiaxing1995.easyJmeter.common.enumeration.TaskResultEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.HashMap;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CaseInfoVO {

    private Integer id;

    private String name;

    private JmeterStatusEnum status;

    private String projectName;

    private Integer project;

    private String jmx;

    private String csv;

    private String jar;

    private String description;

    private String creator;

    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss", timezone="Asia/Shanghai")
    private Date createTime;

    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss", timezone="Asia/Shanghai")
    private Date updateTime;

    @JsonIgnore
    private Date deleteTime;

    private String taskId;

    private TaskResultEnum taskResult;

    private HashMap<String, Object> taskProgress;
}
