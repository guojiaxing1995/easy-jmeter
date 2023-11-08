package io.github.guojiaxing1995.easyJmeter.service;

import io.github.guojiaxing1995.easyJmeter.common.enumeration.TaskResultEnum;
import io.github.guojiaxing1995.easyJmeter.dto.task.CreateOrUpdateTaskDTO;
import io.github.guojiaxing1995.easyJmeter.model.TaskDO;

public interface TaskService {

    boolean createTask(CreateOrUpdateTaskDTO taskDTO);

    boolean updateTaskResult(TaskDO taskDO, TaskResultEnum result);

    TaskDO getTaskById(Integer id);
}
