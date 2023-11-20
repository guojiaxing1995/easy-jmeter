package io.github.guojiaxing1995.easyJmeter.controller.v1;

import com.corundumstudio.socketio.HandshakeData;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.annotation.OnConnect;
import com.corundumstudio.socketio.annotation.OnDisconnect;
import com.corundumstudio.socketio.annotation.OnEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import io.github.guojiaxing1995.easyJmeter.common.enumeration.JmeterStatusEnum;
import io.github.guojiaxing1995.easyJmeter.common.enumeration.MachineOnlineEnum;
import io.github.guojiaxing1995.easyJmeter.common.enumeration.TaskResultEnum;
import io.github.guojiaxing1995.easyJmeter.dto.machine.HeartBeatMachineDTO;
import io.github.guojiaxing1995.easyJmeter.dto.task.TaskMachineDTO;
import io.github.guojiaxing1995.easyJmeter.model.CaseDO;
import io.github.guojiaxing1995.easyJmeter.model.MachineDO;
import io.github.guojiaxing1995.easyJmeter.model.TaskDO;
import io.github.guojiaxing1995.easyJmeter.model.TaskLogDO;
import io.github.guojiaxing1995.easyJmeter.service.CaseService;
import io.github.guojiaxing1995.easyJmeter.service.MachineService;
import io.github.guojiaxing1995.easyJmeter.service.TaskLogService;
import io.github.guojiaxing1995.easyJmeter.service.TaskService;
import io.github.guojiaxing1995.easyJmeter.vo.CutFileVO;
import io.github.guojiaxing1995.easyJmeter.vo.MachineCutFileVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
public class SocketIOServerHandler {

    @Autowired
    private SocketIOServer socketServer;

    @Autowired
    private MachineService machineService;

    @Autowired
    private TaskLogService taskLogService;

    @Autowired
    private CaseService caseService;

    @Autowired
    private TaskService taskService;

    @Autowired
    Cache<String, Object> caffeineCache;

    @OnConnect
    public void onConnect(SocketIOClient client) {
        log.info("Client connected: " + client.getSessionId());
        // 处理连接事件
        // 判断客户端类型 加入room
        HandshakeData handshakeData = client.getHandshakeData();
        String type = handshakeData.getUrlParams().get("client-type").get(0);
        if (type.equals("machine")){
            client.joinRoom("machine");
        } else if (type.equals("web")) {
            client.joinRoom("web");
        }
    }

    @OnDisconnect
    public void onDisconnect(SocketIOClient client) {
        log.info("Client disconnected: " + client.getSessionId());
        // 处理断开连接事件
        // 压力机客户端离线后处理
        if (machineService.getByClientId(client.getSessionId().toString()) != null){
            // 设置为已下线状态
            HeartBeatMachineDTO heartBeatMachineDTO = new HeartBeatMachineDTO(client.getSessionId().toString());
            machineService.setMachineStatus(heartBeatMachineDTO, MachineOnlineEnum.OFFLINE);
            log.info("压力机已经离线:" + client.getSessionId());
        }

    }

    @OnEvent("msgServer")
    public void handleMsgEvent(SocketIOClient client, String message) {
        // 处理 chat 事件的逻辑
        // ...
        log.info("msg: " + message);
        client.sendEvent("msgClient", "已经收到" + message);

    }

    @OnEvent("heartBeat")
    @Transactional
    public void  handleHeartBeatEvent(SocketIOClient client, String heartBeat) throws JsonProcessingException {
        HeartBeatMachineDTO heartBeatMachineDTO = new ObjectMapper().readValue(heartBeat, HeartBeatMachineDTO.class);
        heartBeatMachineDTO.setClientId(client.getSessionId().toString());
        machineService.setMachineStatus(heartBeatMachineDTO, MachineOnlineEnum.ONLINE);
    }

    // 接收配置完成通知
    @OnEvent("configureFinish")
    public void configureFinish(SocketIOClient client, String message) throws JsonProcessingException {
        log.info("收到完成配置消息" + message);
        TaskMachineDTO taskMachineDTO = new ObjectMapper().readValue(message, TaskMachineDTO.class);
        TaskDO taskDO = taskMachineDTO.getTaskDO();
        CaseDO caseDO = caseService.getById(taskDO.getJmeterCase());
        MachineDO machineDO = machineService.getByAddress(taskMachineDTO.getMachineIp());
        // 更新task日志
        List<TaskLogDO> taskLogs = taskLogService.getTaskLog(taskDO.getTaskId(), taskDO.getJmeterCase(), JmeterStatusEnum.CONFIGURE, taskMachineDTO.getMachineIp(), null);
        if (taskLogs.isEmpty()){
            // 如果是发生异常后进入此环节，插入当前环节运行成功日志
            taskLogService.createTaskLog(new TaskLogDO(taskDO.getTaskId(),taskDO.getJmeterCase(),JmeterStatusEnum.CONFIGURE,true,machineDO.getAddress(),machineDO.getId()));
        } else {
            taskLogService.updateTaskLog(taskLogs.get(0), taskMachineDTO.getResult());
        }
        // 获取task当前环节已完成的日志记录
        List<TaskLogDO> logs = taskLogService.getTaskLog(taskDO.getTaskId(), taskDO.getJmeterCase(), JmeterStatusEnum.CONFIGURE, null, true);
        // 修改机器节点jmeter状态
        machineService.updateMachineStatus(machineDO, JmeterStatusEnum.RUN);
        // 如果当前环节所有节点全部完成，修改用例状态，发送下一环节指令
        if (logs.size()==taskDO.getMachineNum()) {
            caseService.updateCaseStatus(caseDO, JmeterStatusEnum.RUN);
            // 给agent发消息进入压测运行环节
            socketServer.getRoomOperations(taskDO.getTaskId()).sendEvent("taskRun", taskDO);
            // 插入task运行日志
            String[] machines = taskDO.getMachine().split(",");
            for (String m : machines) {
                MachineDO machine = machineService.getById(Integer.valueOf(m));
                taskLogService.createTaskLog(new TaskLogDO(taskDO.getTaskId(),taskDO.getJmeterCase(),JmeterStatusEnum.RUN,null,machine.getAddress(),machine.getId()));
            }
        }

    }

    // 接收压测运行完成消息
    @OnEvent("runFinish")
    public void runFinish(SocketIOClient client, String message) throws JsonProcessingException {
        log.info("收到压测运行完成消息" + message);
        TaskMachineDTO taskMachineDTO = new ObjectMapper().readValue(message, TaskMachineDTO.class);
        TaskDO taskDO = taskMachineDTO.getTaskDO();
        CaseDO caseDO = caseService.getById(taskDO.getJmeterCase());
        MachineDO machineDO = machineService.getByAddress(taskMachineDTO.getMachineIp());
        // 更新task日志
        List<TaskLogDO> taskLogs = taskLogService.getTaskLog(taskDO.getTaskId(), taskDO.getJmeterCase(), JmeterStatusEnum.RUN, taskMachineDTO.getMachineIp(), null);
        if (taskLogs.isEmpty()){
            // 如果是发生异常后进入此环节，插入当前环节运行成功日志
            taskLogService.createTaskLog(new TaskLogDO(taskDO.getTaskId(),taskDO.getJmeterCase(),JmeterStatusEnum.RUN,true,machineDO.getAddress(),machineDO.getId()));
        } else {
            taskLogService.updateTaskLog(taskLogs.get(0), taskMachineDTO.getResult());
        }
        // 获取task当前环节已完成的日志记录
        List<TaskLogDO> logs = taskLogService.getTaskLog(taskDO.getTaskId(), taskDO.getJmeterCase(), JmeterStatusEnum.RUN, null, true);
        // 修改机器节点jmeter状态
        machineService.updateMachineStatus(machineDO, JmeterStatusEnum.COLLECT);
        // 如果当前环节所有节点全部完成，修改机器、用例状态，发送下一环节指令
        if (logs.size()==taskDO.getMachineNum()) {
            caseService.updateCaseStatus(caseDO, JmeterStatusEnum.COLLECT);
            // 给agent发消息进入结果收集环节
            socketServer.getRoomOperations(taskDO.getTaskId()).sendEvent("taskCollect", taskDO);
            // 插入task收集日志
            String[] machines = taskDO.getMachine().split(",");
            for (String m : machines) {
                MachineDO machine = machineService.getById(Integer.valueOf(m));
                taskLogService.createTaskLog(new TaskLogDO(taskDO.getTaskId(),taskDO.getJmeterCase(),JmeterStatusEnum.COLLECT,null,machine.getAddress(),machine.getId()));
            }
        }

    }

    // 接收结果收集完成消息
    @OnEvent("collectFinish")
    public void collectFinish(SocketIOClient client, String message) throws JsonProcessingException {
        log.info("收到结果收集完成消息" + message);
        TaskMachineDTO taskMachineDTO = new ObjectMapper().readValue(message, TaskMachineDTO.class);
        TaskDO taskDO = taskMachineDTO.getTaskDO();
        CaseDO caseDO = caseService.getById(taskDO.getJmeterCase());
        MachineDO machineDO = machineService.getByAddress(taskMachineDTO.getMachineIp());
        // 更新task日志
        List<TaskLogDO> taskLogs = taskLogService.getTaskLog(taskDO.getTaskId(), taskDO.getJmeterCase(), JmeterStatusEnum.COLLECT, taskMachineDTO.getMachineIp(), null);
        if (taskLogs.isEmpty()){
            // 如果是发生异常后进入此环节，插入当前环节运行成功日志
            taskLogService.createTaskLog(new TaskLogDO(taskDO.getTaskId(),taskDO.getJmeterCase(),JmeterStatusEnum.COLLECT,true,machineDO.getAddress(),machineDO.getId()));
        } else {
            taskLogService.updateTaskLog(taskLogs.get(0), taskMachineDTO.getResult());
        }
        // 获取task当前环节已完成的日志记录
        List<TaskLogDO> logs = taskLogService.getTaskLog(taskDO.getTaskId(), taskDO.getJmeterCase(), JmeterStatusEnum.COLLECT, null, true);
        // 修改机器节点jmeter状态
        machineService.updateMachineStatus(machineDO, JmeterStatusEnum.CLEAN);
        // 如果当前环节所有节点全部完成，修改机器、用例状态，发送下一环节指令
        if (logs.size()==taskDO.getMachineNum()) {
            caseService.updateCaseStatus(caseDO, JmeterStatusEnum.CLEAN);
            // 给agent发消息进入结果收集环节
            socketServer.getRoomOperations(taskDO.getTaskId()).sendEvent("taskClean", taskDO);
            // 插入task清理日志
            String[] machines = taskDO.getMachine().split(",");
            for (String m : machines) {
                MachineDO machine = machineService.getById(Integer.valueOf(m));
                taskLogService.createTaskLog(new TaskLogDO(taskDO.getTaskId(),taskDO.getJmeterCase(),JmeterStatusEnum.CLEAN,null,machine.getAddress(),machine.getId()));
            }
        }
    }

    // 接收环境清理完成消息
    @OnEvent("cleanFinish")
    public void cleanFinish(SocketIOClient client, String message) throws JsonProcessingException {
        log.info("收到环境清理完成消息" + message);
        TaskMachineDTO taskMachineDTO = new ObjectMapper().readValue(message, TaskMachineDTO.class);
        TaskDO taskDO = taskMachineDTO.getTaskDO();
        CaseDO caseDO = caseService.getById(taskDO.getJmeterCase());
        MachineDO machineDO = machineService.getByAddress(taskMachineDTO.getMachineIp());
        // 更新task日志
        List<TaskLogDO> taskLogs = taskLogService.getTaskLog(taskDO.getTaskId(), taskDO.getJmeterCase(), JmeterStatusEnum.CLEAN, taskMachineDTO.getMachineIp(), null);
        if (taskLogs.isEmpty()){
            // 如果是发生异常后进入此环节，插入当前环节运行成功日志
            taskLogService.createTaskLog(new TaskLogDO(taskDO.getTaskId(),taskDO.getJmeterCase(),JmeterStatusEnum.CLEAN,true,machineDO.getAddress(),machineDO.getId()));
        } else {
            taskLogService.updateTaskLog(taskLogs.get(0), taskMachineDTO.getResult());
        }
        // 获取task当前环节已完成的日志记录
        List<TaskLogDO> logs = taskLogService.getTaskLog(taskDO.getTaskId(), taskDO.getJmeterCase(), JmeterStatusEnum.CLEAN, null, true);
        // 修改机器节点jmeter状态
        machineService.updateMachineStatus(machineDO, JmeterStatusEnum.IDLE);
        // 如果当前环节所有节点全部完成，修改用例状态
        if (logs.size()==taskDO.getMachineNum()) {
            caseService.updateCaseStatus(caseDO, JmeterStatusEnum.IDLE);
        }
        // 标记task状态为成功
        TaskDO task = taskService.getTaskById(taskDO.getId());
        if (task.getResult()==TaskResultEnum.IN_PROGRESS) {
            taskService.updateTaskResult(task, TaskResultEnum.SUCCESS);
        }

    }

    // 接收环节失败消息
    @OnEvent("linkFail")
    public void linkFail(SocketIOClient client, String message) throws JsonProcessingException {
        TaskMachineDTO taskMachineDTO = new ObjectMapper().readValue(message, TaskMachineDTO.class);
        log.info("收到linkFail:" + taskMachineDTO.toString());
        TaskDO taskDO = taskMachineDTO.getTaskDO();
        // 更新task日志为失败
        TaskLogDO taskLog = taskLogService.getTaskLog(taskDO.getTaskId(), taskDO.getJmeterCase(), JmeterStatusEnum.getEnumByCode(taskMachineDTO.getStatus()), taskMachineDTO.getMachineIp(), null).get(0);
        taskLogService.updateTaskLog(taskLog, taskMachineDTO.getResult());
        // 标记task状态为失败
        TaskDO task = taskService.getTaskById(taskDO.getId());
        taskService.updateTaskResult(task, TaskResultEnum.EXCEPTION);
        // 如果没有发送过终止消息，向所有agent发送消息进行终止和进入下一环节
        if (caffeineCache.getIfPresent(taskDO.getTaskId() + "_" + JmeterStatusEnum.getEnumByCode(taskMachineDTO.getStatus())) == null){
            caffeineCache.put(taskDO.getTaskId() + "_" + JmeterStatusEnum.getEnumByCode(taskMachineDTO.getStatus()), "taskInterrupt");
            socketServer.getRoomOperations(taskDO.getTaskId()).sendEvent("taskInterrupt", taskMachineDTO);

        }
    }

    @OnEvent("cutCsv")
    public void cutCsv(SocketIOClient client, String message) throws JsonProcessingException {
        TaskDO taskDO = new ObjectMapper().readValue(message, TaskDO.class);
        // 如果是第一次收到指定task的切分，则进行文件切分
        if (caffeineCache.getIfPresent(taskDO.getTaskId() + "_CUT" ) == null){
            caffeineCache.put(taskDO.getTaskId() + "_CUT", true);
            log.info("收到cutCsv:" + taskDO.getTaskId());
            Map<String, List<CutFileVO>> machineDOCutFileVOListMap = taskService.cutCsv(taskDO);
            MachineCutFileVO machineCutFileVO = new MachineCutFileVO(machineDOCutFileVOListMap, taskDO, false);
            socketServer.getRoomOperations(taskDO.getTaskId()).sendEvent("taskConfigure", machineCutFileVO);
        }
    }

}
