package io.github.guojiaxing1995.easyJmeter.controller.v1;

import io.github.guojiaxing1995.easyJmeter.dto.task.CreateOrUpdateTaskDTO;
import io.github.guojiaxing1995.easyJmeter.service.TaskService;
import io.github.guojiaxing1995.easyJmeter.vo.CreatedVO;
import io.github.talelin.core.annotation.LoginRequired;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/task")
@Api(tags = "任务管理")
@Validated
public class TaskController {

    @Autowired
    private TaskService taskService;

    @PostMapping("")
    @ApiOperation(value = "启动用例", notes = "创建任务")
    @LoginRequired
    public CreatedVO creatTask(@RequestBody @Validated CreateOrUpdateTaskDTO validator){
        taskService.createTask(validator);
        return new CreatedVO(16);
    }
}