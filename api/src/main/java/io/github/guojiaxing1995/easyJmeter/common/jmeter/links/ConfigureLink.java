package io.github.guojiaxing1995.easyJmeter.common.jmeter.links;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.guojiaxing1995.easyJmeter.common.configuration.InfluxDBProperties;
import io.github.guojiaxing1995.easyJmeter.common.enumeration.JmeterStatusEnum;
import io.github.guojiaxing1995.easyJmeter.common.jmeter.JmeterExternal;
import io.github.guojiaxing1995.easyJmeter.common.jmeter.LinkStrategy;
import io.github.guojiaxing1995.easyJmeter.dto.task.TaskMachineDTO;
import io.github.guojiaxing1995.easyJmeter.model.TaskDO;
import io.github.guojiaxing1995.easyJmeter.service.JFileService;
import io.github.guojiaxing1995.easyJmeter.vo.MachineCutFileVO;
import io.socket.client.Socket;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ConfigureLink extends Thread implements LinkStrategy {

    private final JFileService jFileService;

    private final Socket socket;

    private TaskDO taskDO;

    private MachineCutFileVO machineCutFileVO;

    private final InfluxDBProperties influxDBProperties;

    public void setMachineCutFileVO(MachineCutFileVO machineCutFileVO) {
        this.machineCutFileVO = machineCutFileVO;
    }

    public ConfigureLink(Socket socket, JFileService jFileService, InfluxDBProperties influxDBProperties) {
        this.socket = socket;
        this.jFileService = jFileService;
        this.influxDBProperties = influxDBProperties;
    }

    @Override
    public void setTask(TaskDO taskDO) {
        this.taskDO = taskDO;
    }

    @Override
    public void run() {
        try {
            log.info("===" + this.taskDO.getTaskId() + "_" + JmeterStatusEnum.CONFIGURE.getDesc() + "===");
            // 判断给到的数据是否还需要server端去切分，没有切分文件和已经切分都为false，需要切分且未切分为true
            if (!this.machineCutFileVO.getNeedCut()){
                JmeterExternal jmeterExternal = new JmeterExternal(socket);
                // 下载运行依赖文件
                jmeterExternal.downloadConfigFile(this.taskDO,jFileService,this.machineCutFileVO);
                //jmeter jmx文件修改 添加properties
                jmeterExternal.initJMeterUtils();
                jmeterExternal.editJmxConfig(taskDO, influxDBProperties);
                jmeterExternal.addProperties();

                this.reportSuccess();
            } else {
                String message = new ObjectMapper().writeValueAsString(taskDO);
                socket.emit("cutCsv", message);
            }

        } catch (Exception e) {
            log.error("配置环节发生" + e.getClass().getName() + "异常：" +e.getMessage() + "，任务ID：" + this.taskDO.getTaskId());
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
        socket.emit("configureFinish", message);
        return true;
    }

    @Override
    public Boolean reportFail() {
        // 发送失败消息
        TaskMachineDTO taskMachineDTO = new TaskMachineDTO();
        taskMachineDTO.setTaskDO(taskDO);
        taskMachineDTO.setMachineIp(new JmeterExternal(socket).getAddress());
        taskMachineDTO.setResult(false);
        taskMachineDTO.setStatus(JmeterStatusEnum.CONFIGURE.getValue());
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
            if (lstThreads[i].getName().equals(taskDO.getTaskId()+"_"+ JmeterStatusEnum.CONFIGURE.getDesc())) {
                lstThreads[i].interrupt();
            }
        }
        return true;
    }
}
