package io.github.guojiaxing1995.easyJmeter.service;

import io.github.guojiaxing1995.easyJmeter.common.enumeration.JmeterStatusEnum;
import io.github.guojiaxing1995.easyJmeter.model.TaskLogDO;

import java.util.List;

public interface TaskLogService {

    List<TaskLogDO> getTaskLog(String taskId, Integer jCase, JmeterStatusEnum status, String address, Boolean result);

    boolean createTaskLog(TaskLogDO taskLogDO);

    boolean updateTaskLog(TaskLogDO taskLogDO, Boolean result);
}
