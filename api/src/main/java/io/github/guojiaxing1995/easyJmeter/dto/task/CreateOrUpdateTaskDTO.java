package io.github.guojiaxing1995.easyJmeter.dto.task;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;

@Data
@NoArgsConstructor
public class CreateOrUpdateTaskDTO {

    @NotNull(message = "{task.case.not-empty}")
    private Integer jCase;

    @NotNull(message = "{task.threads.not-empty}")
    @Min(message = "{task.threads.min}", value = 1)
    private Integer numThreads;

    @NotNull(message = "{task.duration.not-empty}")
    @Min(message = "{task.duration.min}", value = 1)
    private Integer duration;

    private Integer rampTime;

    @Min(message = "{task.qpsLimit.min}", value = 0)
    private Integer qpsLimit;

    private ArrayList<Integer> machine;

    @NotNull(message = "{task.machine-num.not-empty}")
    @Min(message = "{task.machine-num.min}", value = 1)
    private Integer machineNum;

    @Min(message = "{task.granularity.min}", value = 0)
    private Integer granularity;

    private Boolean realtime;

    private Integer logLevel;

    @Length(max = 500, message = "{task.remark.length}")
    private String remark;
}
