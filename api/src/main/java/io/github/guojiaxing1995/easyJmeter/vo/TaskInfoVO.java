package io.github.guojiaxing1995.easyJmeter.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.github.guojiaxing1995.easyJmeter.common.enumeration.LogLevelEnum;
import io.github.guojiaxing1995.easyJmeter.common.enumeration.TaskResultEnum;
import io.github.guojiaxing1995.easyJmeter.model.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

import java.util.Date;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TaskInfoVO {

    private String taskId;

    private String creatorName;

    private Integer jmeterCase;

    private String caseName;

    private List<JFileDO> jmxFileList;

    private List<JFileDO> csvFileList;

    private List<JFileDO> jarFileList;

    private Integer numThreads;

    private Integer duration;

    private Integer rampTime;

    private Integer qpsLimit;

    private List<MachineDO> machineList;

    private Integer machineNum;

    private Integer granularity;

    private Boolean realtime;

    private LogLevelEnum logLevel;

    private String remark;

    private TaskResultEnum result;

    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss", timezone="Asia/Shanghai")
    private Date createTime;

    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss", timezone="Asia/Shanghai")
    private Date updateTime;

    @JsonIgnore
    private Date deleteTime;

    public TaskInfoVO(TaskDO taskDO, CaseDO caseDO, UserDO userDO, List<JFileDO> jmxFileList, List<JFileDO> csvFileList, List<JFileDO> jarFileList, List<MachineDO> machineList) {
        BeanUtils.copyProperties(taskDO, this);
        this.setCsvFileList(csvFileList);
        this.setJmxFileList(jmxFileList);
        this.setJarFileList(jarFileList);
        this.setMachineList(machineList);
        if (caseDO != null) {
            this.setCaseName(caseDO.getName());
        }
        if (userDO != null) {
            this.setCreatorName(userDO.getUsername());
        }
    }
}
