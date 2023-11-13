package io.github.guojiaxing1995.easyJmeter.service;

import io.github.guojiaxing1995.easyJmeter.common.enumeration.TaskResultEnum;
import io.github.guojiaxing1995.easyJmeter.dto.task.CreateOrUpdateTaskDTO;
import io.github.guojiaxing1995.easyJmeter.model.MachineDO;
import io.github.guojiaxing1995.easyJmeter.model.TaskDO;
import io.github.guojiaxing1995.easyJmeter.vo.CutFileVO;

import java.util.List;
import java.util.Map;

public interface TaskService {

    boolean createTask(CreateOrUpdateTaskDTO taskDTO);

    boolean updateTaskResult(TaskDO taskDO, TaskResultEnum result);

    TaskDO getTaskById(Integer id);

    Map<String, List<CutFileVO>> cutCsv(TaskDO taskDO);
}
