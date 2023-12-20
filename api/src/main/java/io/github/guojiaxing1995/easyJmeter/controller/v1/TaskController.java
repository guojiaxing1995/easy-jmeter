package io.github.guojiaxing1995.easyJmeter.controller.v1;

import io.github.guojiaxing1995.easyJmeter.dto.task.CreateOrUpdateTaskDTO;
import io.github.guojiaxing1995.easyJmeter.dto.task.ModifyTaskDTO;
import io.github.guojiaxing1995.easyJmeter.model.ReportDO;
import io.github.guojiaxing1995.easyJmeter.model.TaskDO;
import io.github.guojiaxing1995.easyJmeter.service.TaskService;
import io.github.guojiaxing1995.easyJmeter.vo.CreatedVO;
import io.github.guojiaxing1995.easyJmeter.vo.TaskInfoVO;
import io.github.guojiaxing1995.easyJmeter.vo.UpdatedVO;
import io.github.talelin.core.annotation.LoginRequired;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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

    @PutMapping("/stop")
    @ApiOperation(value = "终止测试", notes = "手动终止测试")
    @LoginRequired
    public UpdatedVO stopTask(@RequestBody @Validated ModifyTaskDTO validator){
        taskService.stopTask(validator.getTaskId());
        return new UpdatedVO(17);
    }

    @PutMapping("/modifyQPSLimit")
    @ApiOperation(value = "修改测试中的qps限制", notes = "动态控制吞吐量")
    public UpdatedVO modifyQPSLimit(@RequestBody @Validated ModifyTaskDTO validator){
        taskService.modifyQPSLimit(validator);
        return new UpdatedVO(18);
    }

    @GetMapping("/{taskId}")
    @ApiOperation(value = "获取一个任务", notes = "根据任务taskId获取任务")
    public TaskDO getTaskById(@PathVariable String taskId){
        return taskService.getTaskByTaskId(taskId);
    }

    @GetMapping("/info/{taskId}")
    @ApiOperation(value = "获取一个任务的详细信息", notes = "根据任务taskId获取任务的详细信息")
    public TaskInfoVO getTaskInfoById(@PathVariable String taskId){
        return taskService.getTaskInfo(taskId);
    }

    @GetMapping("/log/{taskId}")
    @ApiOperation(value = "获取任务日志", notes = "根据任务taskId获取一个任务的日志")
    public List<Map<String, Object>> getTaskLogById(@PathVariable String taskId){
        return taskService.getTaskLogByTaskId(taskId);
    }

    @GetMapping("/report/{taskId}")
    @ApiOperation(value = "获取任务报告", notes = "根据任务taskId获取一个任务的报告数据")
    public ReportDO getTaskReportById(@PathVariable String taskId){
        return taskService.getTaskReportByTaskId(taskId);
    }
}
