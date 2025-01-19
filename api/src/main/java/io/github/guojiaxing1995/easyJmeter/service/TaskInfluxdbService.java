package io.github.guojiaxing1995.easyJmeter.service;

import io.github.guojiaxing1995.easyJmeter.dto.task.JmeterParamDTO;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

public interface TaskInfluxdbService {

    Map<String, Object> getTimes(String taskId);

    Map<String, Object> sampleCounts(String taskId, String startTime, String endTime);

    Map<String, Object> throughputGraph(String taskId, String startTime, String endTime, List<OffsetDateTime> points);

    Map<String, Object> errorGraph(String taskId, String startTime, String endTime, List<OffsetDateTime> points);

    Map<String, Object> errorInfo(String taskId, String startTime, String endTime);

    List<JmeterParamDTO> getEvents(JmeterParamDTO jmeterParamDTO);

    List<Map<String, Object>> getAggregateReport(List<JmeterParamDTO> jmeterParamDTOList);

}
