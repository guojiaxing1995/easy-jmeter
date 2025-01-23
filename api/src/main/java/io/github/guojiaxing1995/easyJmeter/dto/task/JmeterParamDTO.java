package io.github.guojiaxing1995.easyJmeter.dto.task;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JmeterParamDTO {

    private String startTime;

    private String endTime;

    private String application;

    private String tags;

    private String text;

    private Integer projectId;

    private String label;

}