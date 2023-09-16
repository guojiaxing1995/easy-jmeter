package io.github.guojiaxing1995.easyJmeter.service;

import io.github.guojiaxing1995.easyJmeter.dto.task.CreateOrUpdateTaskDTO;

public interface TaskService {

    boolean createTask(CreateOrUpdateTaskDTO taskDTO);
}
