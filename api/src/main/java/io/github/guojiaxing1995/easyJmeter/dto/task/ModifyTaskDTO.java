package io.github.guojiaxing1995.easyJmeter.dto.task;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;

@Data
@NoArgsConstructor
public class ModifyTaskDTO {

    private String taskId;

    @Min(message = "{task.qpsLimit.min}", value = 0)
    private Integer qpsLimit;
}
