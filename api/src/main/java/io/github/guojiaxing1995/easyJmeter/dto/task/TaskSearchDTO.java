package io.github.guojiaxing1995.easyJmeter.dto.task;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
public class TaskSearchDTO {

    private String taskId;

    private String jCase;

    private Integer result;

    private String startTime;

    private String endTime;

    @NotNull(message = "{page.number}")
    private Integer page;
}
