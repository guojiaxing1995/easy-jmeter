package io.github.guojiaxing1995.easyJmeter.common.jmeter.links;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.guojiaxing1995.easyJmeter.common.enumeration.JmeterStatusEnum;
import io.github.guojiaxing1995.easyJmeter.common.jmeter.JmeterExternal;
import io.github.guojiaxing1995.easyJmeter.common.jmeter.LinkStrategy;
import io.github.guojiaxing1995.easyJmeter.common.util.ThreadUtil;
import io.github.guojiaxing1995.easyJmeter.dto.task.TaskMachineDTO;
import io.github.guojiaxing1995.easyJmeter.model.TaskDO;
import io.socket.client.Socket;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Slf4j
@Service
public class CleanLink extends Thread implements LinkStrategy {

    private final Socket socket;
    private TaskDO taskDO;

    public CleanLink(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void setTask(TaskDO taskDO) {
        this.taskDO = taskDO;
    }

    @Override
    public void run() {
        try {
            log.info("===" + this.taskDO.getTaskId() + "_" + JmeterStatusEnum.CLEAN.getDesc() + "===");
            // 如果测试进程仍然存在，则杀掉进程
            if (ThreadUtil.isProcessContainingName(taskDO.getTaskId()+".jmx")) {
                ThreadUtil.killProcessContainingName(taskDO.getTaskId()+".jmx");
            }
            // 将tmp目录重命名为任务目录
            Path tmpPath = Paths.get(System.getenv("JMETER_HOME"), "tmp");
            Path taskPath = Paths.get(System.getenv("JMETER_HOME"), this.taskDO.getTaskId());
            Files.move(tmpPath, taskPath, StandardCopyOption.REPLACE_EXISTING);

            this.reportSuccess();
        } catch (Exception e) {
            log.error("清理环节发生" + e.getClass().getName() + "异常：" +e.getMessage() + "，任务ID：" + this.taskDO.getTaskId());
            log.error("error", e);
            this.reportFail();
        }
    }

    @Override
    public Boolean reportSuccess() throws JsonProcessingException {
        TaskMachineDTO taskMachineDTO = new TaskMachineDTO();
        taskMachineDTO.setTaskDO(taskDO);
        taskMachineDTO.setMachineIp(new JmeterExternal(socket).getAddress());
        taskMachineDTO.setResult(true);
        String message = new ObjectMapper().writeValueAsString(taskMachineDTO);
        socket.emit("cleanFinish", message);
        return true;
    }

    @Override
    public Boolean reportFail() {
        // 发送失败消息
        TaskMachineDTO taskMachineDTO = new TaskMachineDTO();
        taskMachineDTO.setTaskDO(taskDO);
        taskMachineDTO.setMachineIp(new JmeterExternal(socket).getAddress());
        taskMachineDTO.setResult(false);
        taskMachineDTO.setStatus(JmeterStatusEnum.CLEAN.getValue());
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
            if (lstThreads[i].getName().equals(taskDO.getTaskId()+"_"+ JmeterStatusEnum.CLEAN.getDesc())) {
                lstThreads[i].interrupt();
            }
        }
        return true;
    }
}
