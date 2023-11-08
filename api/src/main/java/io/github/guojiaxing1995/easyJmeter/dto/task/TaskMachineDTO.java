package io.github.guojiaxing1995.easyJmeter.dto.task;

import io.github.guojiaxing1995.easyJmeter.model.TaskDO;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TaskMachineDTO {

    private TaskDO taskDO;

    private String machineIp;

    private Boolean result;

    private Integer status;

}
