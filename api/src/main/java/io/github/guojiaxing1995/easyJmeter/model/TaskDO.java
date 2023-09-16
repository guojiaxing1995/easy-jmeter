package io.github.guojiaxing1995.easyJmeter.model;

import com.baomidou.mybatisplus.annotation.TableName;
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

    private Integer jCase;

    private String jmx;

    private String csv;

    private String jar;

    private Integer threads;

    private Integer duration;

    private Integer warmupTime;

    private Integer qpsLimit;

    private String machine;

    private Integer machineNum;

    private Boolean monitor;

    private Boolean realtime;

    private Boolean log;

    private String remark;
}