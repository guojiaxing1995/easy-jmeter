package io.github.guojiaxing1995.easyJmeter.model;

import com.baomidou.mybatisplus.annotation.TableName;
import io.github.guojiaxing1995.easyJmeter.common.enumeration.LogLevelEnum;
import io.github.guojiaxing1995.easyJmeter.common.enumeration.TaskResultEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@TableName("task")
@EqualsAndHashCode(callSuper = true)
public class TaskDO extends BaseModel implements Serializable {

    private static final long serialVersionUID = -7038882032711810177L;

    private String taskId;

    private Integer creator;

    private Integer jmeterCase;

    private String jmx;

    private String csv;

    private String jar;

    private Integer numThreads;

    private Integer duration;

    private Integer rampTime;

    private Integer qpsLimit;

    private String machine;

    private Integer machineNum;

    private Integer granularity;

    private Boolean realtime;

    private LogLevelEnum logLevel;

    private String remark;

    private TaskResultEnum result;

}
