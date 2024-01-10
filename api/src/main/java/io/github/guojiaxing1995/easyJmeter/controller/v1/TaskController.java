package io.github.guojiaxing1995.easyJmeter.controller.v1;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.github.guojiaxing1995.easyJmeter.common.util.PageUtil;
import io.github.guojiaxing1995.easyJmeter.dto.task.CreateOrUpdateTaskDTO;
import io.github.guojiaxing1995.easyJmeter.dto.task.ModifyTaskDTO;
import io.github.guojiaxing1995.easyJmeter.dto.task.TaskSearchDTO;
import io.github.guojiaxing1995.easyJmeter.model.ReportDO;
import io.github.guojiaxing1995.easyJmeter.model.TaskDO;
import io.github.guojiaxing1995.easyJmeter.service.TaskService;
import io.github.guojiaxing1995.easyJmeter.vo.*;
import io.github.talelin.core.annotation.GroupRequired;
import io.github.talelin.core.annotation.LoginRequired;
import io.github.talelin.core.annotation.PermissionMeta;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    @LoginRequired
    public UpdatedVO modifyQPSLimit(@RequestBody @Validated ModifyTaskDTO validator){
        taskService.modifyQPSLimit(validator);
        return new UpdatedVO(18);
    }

    @GetMapping("/{taskId}")
    @ApiOperation(value = "获取一个任务", notes = "根据任务taskId获取任务")
    @LoginRequired
    public TaskDO getTaskById(@PathVariable String taskId){
        return taskService.getTaskByTaskId(taskId);
    }

    @GetMapping("/info/{taskId}")
    @ApiOperation(value = "获取一个任务的详细信息", notes = "根据任务taskId获取任务的详细信息")
    @LoginRequired
    public TaskInfoVO getTaskInfoById(@PathVariable String taskId){
        return taskService.getTaskInfo(taskId);
    }

    @GetMapping("/log/{taskId}")
    @ApiOperation(value = "获取任务日志", notes = "根据任务taskId获取一个任务的日志")
    @LoginRequired
    public List<Map<String, Object>> getTaskLogById(@PathVariable String taskId){
        return taskService.getTaskLogByTaskId(taskId);
    }

    @GetMapping("/report/{taskId}")
    @LoginRequired
    @ApiOperation(value = "获取任务报告", notes = "根据任务taskId获取一个任务的报告数据")
    public ReportDO getTaskReportById(@PathVariable String taskId){
        return taskService.getTaskReportByTaskId(taskId);
    }

    @PostMapping("/history")
    @ApiOperation(value = "查询测试记录", notes = "传入taskId、所属用例、结果、创建时间区间查询历史记录")
    @LoginRequired
    @GroupRequired
    @PermissionMeta(value = "测试记录", module = "用例")
    public PageResponseVO<HistoryTaskVO> searchHistoryTask(@RequestBody TaskSearchDTO validator){
        IPage<HistoryTaskVO> historyTask = taskService.getHistoryTask(validator.getPage(), validator.getJCase(), validator.getTaskId(), validator.getStartTime(), validator.getEndTime(), validator.getResult());
        return PageUtil.build(historyTask);
    }

    @DeleteMapping("/batch")
    @ApiOperation(value = "批量删除测试记录", notes = "根据测试记录id集合删除测试记录")
    @LoginRequired
    public DeletedVO batchDeleteTask(@Parameter String ids){
        List<Integer> idList= Arrays.stream(ids.split(",")).map(s->Integer.parseInt(s.trim())).collect(Collectors.toList());
        taskService.deleteTasks(idList);
        return new DeletedVO(3);
    }

}
