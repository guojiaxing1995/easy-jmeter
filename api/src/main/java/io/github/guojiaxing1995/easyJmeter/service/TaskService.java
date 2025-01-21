package io.github.guojiaxing1995.easyJmeter.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.github.guojiaxing1995.easyJmeter.common.enumeration.TaskResultEnum;
import io.github.guojiaxing1995.easyJmeter.dto.task.CreateOrUpdateTaskDTO;
import io.github.guojiaxing1995.easyJmeter.dto.task.JmeterAggregateReportDTO;
import io.github.guojiaxing1995.easyJmeter.dto.task.ModifyTaskDTO;
import io.github.guojiaxing1995.easyJmeter.model.ReportDO;
import io.github.guojiaxing1995.easyJmeter.model.TaskDO;
import io.github.guojiaxing1995.easyJmeter.vo.CutFileVO;
import io.github.guojiaxing1995.easyJmeter.vo.HistoryTaskVO;
import io.github.guojiaxing1995.easyJmeter.vo.TaskInfoVO;

import java.util.List;
import java.util.Map;

public interface TaskService {

    boolean createTask(CreateOrUpdateTaskDTO taskDTO);

    boolean updateTaskResult(TaskDO taskDO, TaskResultEnum result);

    TaskDO getTaskById(Integer id);

    Map<String, List<CutFileVO>> cutCsv(TaskDO taskDO);

    TaskDO getTaskByTaskId(String taskId);

    boolean stopTask(String taskId);

    boolean modifyQPSLimit(ModifyTaskDTO validator);

    TaskInfoVO getTaskInfo(String taskId);

    List<Map<String, Object>> getTaskLogByTaskId(String taskId);

    ReportDO getTaskReportByTaskId(String taskId);

    IPage<HistoryTaskVO> getHistoryTask(Integer current,String jmeterCase, String taskId, String startTime, String endTime, Integer result);

    boolean deleteTasks(List<Integer> ids);

    List<TaskDO> getTasksByCaseId(Integer caseId);

    void aggregateReportAdd(JmeterAggregateReportDTO jmeterAggregateReportDTO);
}
