package io.github.guojiaxing1995.easyJmeter.dto.task;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskProgressMachineDTO implements Serializable {

    private static final long serialVersionUID = 7592545643989464081L;
    private String taskId;
    private String machineIp;
    private Integer process;
}
