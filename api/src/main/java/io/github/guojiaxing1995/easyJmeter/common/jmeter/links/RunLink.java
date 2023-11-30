package io.github.guojiaxing1995.easyJmeter.common.jmeter.links;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.guojiaxing1995.easyJmeter.common.enumeration.JmeterStatusEnum;
import io.github.guojiaxing1995.easyJmeter.common.jmeter.JmeterExternal;
import io.github.guojiaxing1995.easyJmeter.common.jmeter.LinkStrategy;
import io.github.guojiaxing1995.easyJmeter.dto.task.TaskMachineDTO;
import io.github.guojiaxing1995.easyJmeter.model.TaskDO;
import io.socket.client.Socket;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Paths;

@Slf4j
@Service
public class RunLink extends Thread implements LinkStrategy {
    private final Socket socket;

    private TaskDO taskDO;

    public RunLink(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void setTask(TaskDO taskDO) {
        this.taskDO = taskDO;
    }

    @Override
    public void run() {
        try {
            log.info("===" + this.taskDO.getTaskId() + "_" + JmeterStatusEnum.RUN.getDesc() + "===");
            JmeterExternal jmeterExternal = new JmeterExternal(socket);
            jmeterExternal.runJmeter(taskDO);
            this.reportSuccess();
        } catch (Exception e) {
            log.error("运行环节发生" + e.getClass().getName() + "异常：" +e.getMessage() + "，任务ID：" + this.taskDO.getTaskId());
            log.error("error", e);
            this.reportFail();
        }
    }

    @Override
    public Boolean reportSuccess() throws JsonProcessingException {
        String reportPath = Paths.get(System.getenv("JMETER_HOME"), "/tmp/report").toString();
        File report = new File(reportPath);
        if (report.exists()){
            TaskMachineDTO taskMachineDTO = new TaskMachineDTO();
            taskMachineDTO.setTaskDO(taskDO);
            taskMachineDTO.setMachineIp(new JmeterExternal(socket).getAddress());
            taskMachineDTO.setResult(true);
            String message = new ObjectMapper().writeValueAsString(taskMachineDTO);
            socket.emit("runFinish", message);
            return true;
        }
        return false;
    }

    @Override
    public Boolean reportFail() {
        // 发送失败消息
        TaskMachineDTO taskMachineDTO = new TaskMachineDTO();
        taskMachineDTO.setTaskDO(taskDO);
        taskMachineDTO.setMachineIp(new JmeterExternal(socket).getAddress());
        taskMachineDTO.setResult(false);
        taskMachineDTO.setStatus(JmeterStatusEnum.RUN.getValue());
        try {
            String message = new ObjectMapper().writeValueAsString(taskMachineDTO);
            socket.emit("linkFail", message);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    @Override
    public Boolean interruptThread() {
        // 终止线程
        ThreadGroup currentGroup = Thread.currentThread().getThreadGroup();
        int noThreads = currentGroup.activeCount();
        Thread[] lstThreads = new Thread[noThreads];
        currentGroup.enumerate(lstThreads);
        for (int i = 0; i < noThreads; i++) {
            if (lstThreads[i].getName().equals(taskDO.getTaskId()+"_"+ JmeterStatusEnum.RUN.getDesc())) {
                lstThreads[i].interrupt();
            }
        }
        return true;
    }
}
