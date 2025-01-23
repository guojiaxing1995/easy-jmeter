package io.github.guojiaxing1995.easyJmeter.controller.v1;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.github.guojiaxing1995.easyJmeter.common.util.PageUtil;
import io.github.guojiaxing1995.easyJmeter.dto.task.*;
import io.github.guojiaxing1995.easyJmeter.model.AggregateReportDO;
import io.github.guojiaxing1995.easyJmeter.model.ReportDO;
import io.github.guojiaxing1995.easyJmeter.model.TaskDO;
import io.github.guojiaxing1995.easyJmeter.repository.AggregateReportRepository;
import io.github.guojiaxing1995.easyJmeter.service.TaskInfluxdbService;
import io.github.guojiaxing1995.easyJmeter.service.TaskService;
import io.github.guojiaxing1995.easyJmeter.vo.*;
import io.github.talelin.autoconfigure.exception.ParameterException;
import io.github.talelin.core.annotation.GroupRequired;
import io.github.talelin.core.annotation.LoginRequired;
import io.github.talelin.core.annotation.PermissionMeta;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Date;
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

    @Autowired
    private TaskInfluxdbService taskInfluxdbService;

    @Autowired
    private AggregateReportRepository aggregateReportRepository;

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

    @PostMapping("/realTimeData")
    @ApiOperation(value = "获取实时数据", notes = "传入测试记录id、数据类型")
    @LoginRequired
    public Map<String, Object> getRealTimeData(@RequestBody TaskRealTimeDTO validator){
        Map<String, Object> times = taskInfluxdbService.getTimes(validator.getTaskId());
        String startTime = times.get("startTime").toString();
        String endTime = times.get("endTime").toString();
        List<OffsetDateTime> points = (List<OffsetDateTime>) times.get("points");
        String type = validator.getType();
        String taskId = validator.getTaskId();
        switch (type) {
            case "TIMES":
                return times;
            case "COUNT":
                return taskInfluxdbService.sampleCounts(taskId, startTime, endTime);
            case "THROUGHPUT":
                return taskInfluxdbService.throughputGraph(taskId, startTime, endTime, points);
            case "ERROR":
                return taskInfluxdbService.errorGraph(taskId, startTime, endTime, points);
            case "ERROR_INFO":
                return taskInfluxdbService.errorInfo(taskId, startTime, endTime);
            default:
                throw new ParameterException(12501);
        }
    }

    @PostMapping("/aggregateReport/search")
    @ApiOperation(value = "聚合报告查询", notes = "根据时间范围、应用、标签查询聚合报告，按照应用、标签进行分类")
    @PermissionMeta(value = "聚合报告记录", module = "jmeter数据")
    @LoginRequired
    public List<Map<String, Object>> aggregateReportSearch(@RequestBody @Validated JmeterParamDTO validator) {
        List<JmeterParamDTO> events = taskInfluxdbService.getEvents(validator);
        return taskInfluxdbService.getAggregateReport(events);
    }

    @PostMapping("/aggregateReport/archive")
    @ApiOperation(value = "聚合报告归档", notes = "选择聚合报告归档到目标工程下")
    @PermissionMeta(value = "聚合报告记录", module = "jmeter数据")
    @LoginRequired
    public CreatedVO aggregateReportArchive(@RequestBody @Validated JmeterAggregateReportDTO validator) {
        taskService.aggregateReportAdd(validator);
        return new CreatedVO();
    }

    @PostMapping("/aggregateReport/record/search")
    @ApiOperation(value = "聚合报告归档数据查询", notes = "根据工程、备注、事务进行查询")
    @PermissionMeta(value = "聚合报告归档数据", module = "jmeter数据")
    @LoginRequired
    public List<AggregateReportDO> aggregateReportRecordSearch(@RequestBody @Validated JmeterParamDTO validator) {
        String label = validator.getLabel() != null ? validator.getLabel() : "";
        Integer projectId = validator.getProjectId() != null ? validator.getProjectId() : 0;
        String text = validator.getText() != null ? validator.getText() : "";

        return aggregateReportRepository.getAggregateReportRecord(projectId, text, label).orElse(List.of());

    }

    @DeleteMapping("/aggregateReport/record/delete")
    @ApiOperation(value = "聚合报告归档数据删除", notes = "传入记录id列表")
    @PermissionMeta(value = "聚合报告归档数据", module = "jmeter数据")
    @LoginRequired
    public DeletedVO aggregateReportRecordRemove(@RequestBody List<String> ids) {
        for (String id: ids) {
            AggregateReportDO aggregateReportDO = aggregateReportRepository.findById(id).get();
            aggregateReportDO.setDeleteTime(new Date());
            aggregateReportRepository.save(aggregateReportDO);
        }
        return new DeletedVO();
    }
}
