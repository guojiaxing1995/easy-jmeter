package io.github.guojiaxing1995.easyJmeter.service.impl;

import io.github.guojiaxing1995.easyJmeter.common.enumeration.JmeterStatusEnum;
import io.github.guojiaxing1995.easyJmeter.mapper.TaskLogMapper;
import io.github.guojiaxing1995.easyJmeter.model.TaskLogDO;
import io.github.guojiaxing1995.easyJmeter.service.TaskLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class TaskLogServiceImpl implements TaskLogService {

    @Autowired
    private TaskLogMapper taskLogMapper;

    @Override
    public List<TaskLogDO> getTaskLog(String taskId, Integer jCase, JmeterStatusEnum status, String address,  Boolean result) {
        return taskLogMapper.search(taskId,jCase,status,address,result);
    }

    @Override
    public boolean createTaskLog(TaskLogDO taskLogDO) {
        return taskLogMapper.insert(taskLogDO) > 0;
    }

    @Override
    public boolean updateTaskLog(TaskLogDO taskLogDO, Boolean result) {
        taskLogDO.setResult(result);
        return taskLogMapper.updateById(taskLogDO) > 0;
    }
}
