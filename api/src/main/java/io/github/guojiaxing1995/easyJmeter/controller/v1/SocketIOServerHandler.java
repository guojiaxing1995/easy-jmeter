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
import io.github.guojiaxing1995.easyJmeter.common.enumeration.DebugTypeEnum;
import io.github.guojiaxing1995.easyJmeter.common.enumeration.JmeterStatusEnum;
import io.github.guojiaxing1995.easyJmeter.common.enumeration.TaskResultEnum;
import io.github.guojiaxing1995.easyJmeter.common.jmeter.JmeterExternal;
import io.github.guojiaxing1995.easyJmeter.common.serializer.DeserializerObjectMapper;
import io.github.guojiaxing1995.easyJmeter.dto.jcase.CaseDebugDTO;
import io.github.guojiaxing1995.easyJmeter.dto.machine.HeartBeatMachineDTO;
import io.github.guojiaxing1995.easyJmeter.dto.task.TaskMachineDTO;
import io.github.guojiaxing1995.easyJmeter.dto.task.TaskProgressMachineDTO;
import io.github.guojiaxing1995.easyJmeter.model.CaseDO;
import io.github.guojiaxing1995.easyJmeter.model.MachineDO;
import io.github.guojiaxing1995.easyJmeter.model.TaskDO;
import io.github.guojiaxing1995.easyJmeter.model.TaskLogDO;
import io.github.guojiaxing1995.easyJmeter.repository.ReportRepository;
import io.github.guojiaxing1995.easyJmeter.service.*;
import io.github.guojiaxing1995.easyJmeter.vo.CaseDebugVO;
import io.github.guojiaxing1995.easyJmeter.vo.CutFileVO;
import io.github.guojiaxing1995.easyJmeter.vo.MachineCutFileVO;
import io.github.guojiaxing1995.easyJmeter.vo.TaskProgressVO;
import io.socket.client.Socket;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
public class SocketIOServerHandler {

    private final Socket socket;

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

    @Autowired
    private JFileService jFileService;

    @Autowired
    private ReportRepository reportRepository;

    public SocketIOServerHandler(Socket socket) {
        this.socket = socket;
    }

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
            HeartBeatMachineDTO heartBeatMachineDTO = new HeartBeatMachineDTO();
            heartBeatMachineDTO.setClientId(client.getSessionId().toString());
            machineService.setMachineStatus(heartBeatMachineDTO, false);
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
        machineService.setMachineStatus(heartBeatMachineDTO, true);
    }

    // 接收配置完成通知
    @OnEvent("configureFinish")
    public synchronized void configureFinish(SocketIOClient client, String message) {
        log.info("收到完成配置消息" + message);
        TaskMachineDTO taskMachineDTO = DeserializerObjectMapper.deserialize(message, TaskMachineDTO.class);
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
        // 向web端发送task日志
        socketServer.getRoomOperations("web").sendEvent("taskLogs", taskService.getTaskLogByTaskId(taskDO.getTaskId()));
        // 获取task当前环节已完成的日志记录
        List<TaskLogDO> logs = taskLogService.getTaskLog(taskDO.getTaskId(), taskDO.getJmeterCase(), JmeterStatusEnum.CONFIGURE, null, true);
        // 修改机器节点jmeter状态
        machineService.updateMachineStatus(machineDO, JmeterStatusEnum.RUN);
        // 如果当前环节所有节点全部完成，修改用例状态，发送下一环节指令
        if (logs.size()==taskDO.getMachineNum()) {
            caseService.updateCaseStatus(caseDO, JmeterStatusEnum.RUN);
            // 给agent发消息进入压测运行环节
            socketServer.getRoomOperations(taskDO.getTaskId()).sendEvent("taskRun", taskDO);
            // 插入task运行日志,初始化运行进度
            String[] machines = taskDO.getMachine().split(",");
            HashMap<String, Integer> map = new HashMap<>();
            for (String m : machines) {
                MachineDO machine = machineService.getById(Integer.valueOf(m));
                // 插入task日志
                taskLogService.createTaskLog(new TaskLogDO(taskDO.getTaskId(),taskDO.getJmeterCase(),JmeterStatusEnum.RUN,null,machine.getAddress(),machine.getId()));
                map.put(machine.getAddress(), 0);
            }
            //将初始化进度写入缓存
            caffeineCache.put(taskDO.getTaskId() + "_PROGRESS", map);

            // 向web端报告进度
            TaskProgressVO taskProgressVO = new TaskProgressVO(taskDO.getTaskId(), JmeterStatusEnum.RUN, map, TaskResultEnum.IN_PROGRESS);
            socketServer.getRoomOperations("web").sendEvent("taskProgress", taskProgressVO);
            // 向web端发送task日志
            socketServer.getRoomOperations("web").sendEvent("taskLogs", taskService.getTaskLogByTaskId(taskDO.getTaskId()));
        }

    }

    // 接收压测运行完成消息
    @OnEvent("runFinish")
    public synchronized void runFinish(SocketIOClient client, String message) {
        log.info("收到压测运行完成消息" + message);
        TaskMachineDTO taskMachineDTO = DeserializerObjectMapper.deserialize(message, TaskMachineDTO.class);
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
        // 向web端发送task日志
        socketServer.getRoomOperations("web").sendEvent("taskLogs", taskService.getTaskLogByTaskId(taskDO.getTaskId()));
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

            // 向web端报告进度
            TaskProgressVO taskProgressVO = new TaskProgressVO(taskDO.getTaskId(), JmeterStatusEnum.COLLECT, null, TaskResultEnum.IN_PROGRESS);
            socketServer.getRoomOperations("web").sendEvent("taskProgress", taskProgressVO);
            // 向web端发送task日志
            socketServer.getRoomOperations("web").sendEvent("taskLogs", taskService.getTaskLogByTaskId(taskDO.getTaskId()));
        }

    }

    // 接收结果收集完成消息
    @OnEvent("collectFinish")
    public synchronized void collectFinish(SocketIOClient client, String message) {
        log.info("收到结果收集完成消息" + message);
        TaskMachineDTO taskMachineDTO = DeserializerObjectMapper.deserialize(message, TaskMachineDTO.class);
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
        // 向web端发送task日志
        socketServer.getRoomOperations("web").sendEvent("taskLogs", taskService.getTaskLogByTaskId(taskDO.getTaskId()));
        // 获取task当前环节已完成的日志记录
        List<TaskLogDO> logs = taskLogService.getTaskLog(taskDO.getTaskId(), taskDO.getJmeterCase(), JmeterStatusEnum.COLLECT, null, true);
        // 修改机器节点jmeter状态
        machineService.updateMachineStatus(machineDO, JmeterStatusEnum.CLEAN);
        // 如果当前环节所有节点全部完成，修改机器、用例状态，发送下一环节指令
        if (logs.size()==taskDO.getMachineNum()) {
            // 服务端对收集结果处理
            new JmeterExternal().serverCollect(taskDO, jFileService, reportRepository);

            caseService.updateCaseStatus(caseDO, JmeterStatusEnum.CLEAN);
            // 给agent发消息进入清理环节
            socketServer.getRoomOperations(taskDO.getTaskId()).sendEvent("taskClean", taskDO);
            // 插入task清理日志
            String[] machines = taskDO.getMachine().split(",");
            for (String m : machines) {
                MachineDO machine = machineService.getById(Integer.valueOf(m));
                taskLogService.createTaskLog(new TaskLogDO(taskDO.getTaskId(),taskDO.getJmeterCase(),JmeterStatusEnum.CLEAN,null,machine.getAddress(),machine.getId()));
            }

            // 向web端报告进度
            TaskProgressVO taskProgressVO = new TaskProgressVO(taskDO.getTaskId(), JmeterStatusEnum.CLEAN, null, TaskResultEnum.IN_PROGRESS);
            socketServer.getRoomOperations("web").sendEvent("taskProgress", taskProgressVO);
            // 向web端发送task日志
            socketServer.getRoomOperations("web").sendEvent("taskLogs", taskService.getTaskLogByTaskId(taskDO.getTaskId()));
        }
    }

    // 接收环境清理完成消息
    @OnEvent("cleanFinish")
    public synchronized void cleanFinish(SocketIOClient client, String message) {
        log.info("收到环境清理完成消息" + message);
        TaskMachineDTO taskMachineDTO = DeserializerObjectMapper.deserialize(message, TaskMachineDTO.class);
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
        // 向web端发送task日志
        socketServer.getRoomOperations("web").sendEvent("taskLogs", taskService.getTaskLogByTaskId(taskDO.getTaskId()));
        // 获取task当前环节已完成的日志记录
        List<TaskLogDO> logs = taskLogService.getTaskLog(taskDO.getTaskId(), taskDO.getJmeterCase(), JmeterStatusEnum.CLEAN, null, true);
        // 修改机器节点jmeter状态
        machineService.updateMachineStatus(machineDO, JmeterStatusEnum.IDLE);
        // 如果当前环节所有节点全部完成，修改用例状态
        if (logs.size()==taskDO.getMachineNum()) {
            caseService.updateCaseStatus(caseDO, JmeterStatusEnum.IDLE);
            // 标记task状态为成功
            TaskDO task = taskService.getTaskById(taskDO.getId());
            if (task.getResult() == TaskResultEnum.IN_PROGRESS) {
                taskService.updateTaskResult(task, TaskResultEnum.SUCCESS);
            }

            // 向web端报告进度
            TaskProgressVO taskProgressVO = new TaskProgressVO(taskDO.getTaskId(), JmeterStatusEnum.IDLE, null, TaskResultEnum.SUCCESS);
            socketServer.getRoomOperations("web").sendEvent("taskProgress", taskProgressVO);
        }

    }

    // 接收环节失败消息
    @OnEvent("linkFail")
    public synchronized void linkFail(SocketIOClient client, String message) {
        TaskMachineDTO taskMachineDTO = DeserializerObjectMapper.deserialize(message, TaskMachineDTO.class);
        log.info("收到linkFail:" + taskMachineDTO.toString());
        TaskDO taskDO = taskMachineDTO.getTaskDO();
        // 更新task日志为失败
        TaskLogDO taskLog = taskLogService.getTaskLog(taskDO.getTaskId(), taskDO.getJmeterCase(), JmeterStatusEnum.getEnumByCode(taskMachineDTO.getStatus()), taskMachineDTO.getMachineIp(), null).get(0);
        taskLogService.updateTaskLog(taskLog, taskMachineDTO.getResult());
        // 向web端发送task日志
        socketServer.getRoomOperations("web").sendEvent("taskLogs", taskService.getTaskLogByTaskId(taskDO.getTaskId()));
        // 标记task状态为失败
        TaskDO task = taskService.getTaskById(taskDO.getId());
        taskService.updateTaskResult(task, TaskResultEnum.EXCEPTION);
        // 如果没有发送过终止消息，向所有agent发送消息进行终止和进入下一环节
        if (caffeineCache.getIfPresent(taskDO.getTaskId() + "_" + JmeterStatusEnum.getEnumByCode(taskMachineDTO.getStatus())) == null) {
            caffeineCache.put(taskDO.getTaskId() + "_" + JmeterStatusEnum.getEnumByCode(taskMachineDTO.getStatus()), "taskInterrupt");
            socketServer.getRoomOperations(taskDO.getTaskId()).sendEvent("taskInterrupt", taskMachineDTO);
        }
        // 向web端报告进度
        TaskProgressVO taskProgressVO = new TaskProgressVO(taskDO.getTaskId(), JmeterStatusEnum.INTERRUPT, null, TaskResultEnum.EXCEPTION);
        socketServer.getRoomOperations("web").sendEvent("taskProgress", taskProgressVO);
    }

    @OnEvent("cutCsv")
    public synchronized void cutCsv(SocketIOClient client, String message) {
        TaskDO taskDO = DeserializerObjectMapper.deserialize(message, TaskDO.class);
        // 如果是第一次收到指定task的切分，则进行文件切分
        if (caffeineCache.getIfPresent(taskDO.getTaskId() + "_CUT") == null) {
            caffeineCache.put(taskDO.getTaskId() + "_CUT", true);
            log.info("收到cutCsv:" + taskDO.getTaskId());
            Map<String, List<CutFileVO>> machineDOCutFileVOListMap = taskService.cutCsv(taskDO);
            MachineCutFileVO machineCutFileVO = new MachineCutFileVO(machineDOCutFileVOListMap, taskDO, false);
            socketServer.getRoomOperations(taskDO.getTaskId()).sendEvent("taskConfigure", machineCutFileVO);
        }
    }

    @OnEvent("machineTaskProgress")
    public void machineTaskProgress(SocketIOClient client, String message) throws JsonProcessingException {
        TaskProgressMachineDTO taskProgressMachineDTO = new ObjectMapper().readValue(message, TaskProgressMachineDTO.class);
        HashMap<String, Integer> map = (HashMap<String, Integer>) caffeineCache.get(taskProgressMachineDTO.getTaskId() + "_PROGRESS", key -> new HashMap<>());
        map.put(taskProgressMachineDTO.getMachineIp(), taskProgressMachineDTO.getProcess());
        caffeineCache.put(taskProgressMachineDTO.getTaskId() + "_PROGRESS", map);

        // 向web端报告进度
        TaskProgressVO taskProgressVO = new TaskProgressVO(taskProgressMachineDTO.getTaskId(), JmeterStatusEnum.RUN, map, TaskResultEnum.IN_PROGRESS);
        socketServer.getRoomOperations("web").sendEvent("taskProgress", taskProgressVO);
    }

    @OnEvent("caseDebug")
    public void caseDebug(SocketIOClient client, String message){
        CaseDebugDTO caseDebugDTO = DeserializerObjectMapper.deserialize(message, CaseDebugDTO.class);
        try {
            caseService.debugCase(caseDebugDTO);
        } catch (Exception e) {
            log.error("debugCase error", e);
            CaseDebugVO caseDebugVO = CaseDebugVO.builder().type(DebugTypeEnum.ERROR).caseId(caseDebugDTO.getCaseId()).
                    debugId(caseDebugDTO.getDebugId()).log(e.getMessage()).build();
            // 通知web端发生异常
            socketServer.getRoomOperations("web").sendEvent("caseDebugResult", caseDebugVO);
        }

    }

}
