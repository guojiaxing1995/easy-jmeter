package io.github.guojiaxing1995.easyJmeter.common.task;

import io.github.guojiaxing1995.easyJmeter.common.jmeter.ReportDataProcess;
import io.github.guojiaxing1995.easyJmeter.model.TaskDO;
import io.github.guojiaxing1995.easyJmeter.repository.ReportRepository;
import io.github.guojiaxing1995.easyJmeter.repository.StatisticsRepository;
import io.github.guojiaxing1995.easyJmeter.service.CaseService;
import io.github.guojiaxing1995.easyJmeter.service.TaskService;
import io.github.guojiaxing1995.easyJmeter.vo.CaseInfoVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

@Component
@ConditionalOnProperty(name = "socket.server.enable", havingValue = "true")
@Slf4j
public class ServerScheduleTask {

    @Autowired
    private CaseService caseService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private StatisticsRepository statisticsRepository;

    @Scheduled(cron = "${cron.recordStatistics:0 0/30 * * * ? }")
    public void recordStatistics() {
        List<CaseInfoVO> caseInfoVOS = caseService.selectCase(null);
        ReportDataProcess reportDataProcess = new ReportDataProcess(reportRepository, statisticsRepository);
        for (CaseInfoVO caseInfoVO : caseInfoVOS) {
            List<TaskDO> tasks = taskService.getTasksByCaseId(caseInfoVO.getId());
            // 进行一个case的统计
            reportDataProcess.statistics(tasks, caseInfoVO, null);
        }
    }

    @Scheduled(cron = "${cron.jmeterLogClear:0 0 2 ? * * }")
    public void jmeterLogClear() throws IOException {
        String logsPath = Paths.get(System.getProperty("user.dir"), "logs", "jmeter.log").toString();
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(logsPath), StandardOpenOption.TRUNCATE_EXISTING)) {
            log.info("清空jmeter-log文件");
        }
    }
}
