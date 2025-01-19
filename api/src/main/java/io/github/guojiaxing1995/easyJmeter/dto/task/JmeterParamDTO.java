package io.github.guojiaxing1995.easyJmeter.dto.task;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JmeterParamDTO {

    @NotNull(message = "{task.time.not-empty}")
    private String startTime;

    @NotNull(message = "{task.time.not-empty}")
    private String endTime;

    private String application;

    private String tags;

    private String text;
}