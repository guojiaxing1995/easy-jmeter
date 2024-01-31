package io.github.guojiaxing1995.easyJmeter.controller.v1;

import io.github.guojiaxing1995.easyJmeter.common.configuration.InfluxDBProperties;
import io.github.guojiaxing1995.easyJmeter.common.enumeration.JmeterStatusEnum;
import io.github.guojiaxing1995.easyJmeter.common.jmeter.JmeterExternal;
import io.github.guojiaxing1995.easyJmeter.common.jmeter.links.CleanLink;
import io.github.guojiaxing1995.easyJmeter.common.jmeter.links.CollectLink;
import io.github.guojiaxing1995.easyJmeter.common.jmeter.links.ConfigureLink;
import io.github.guojiaxing1995.easyJmeter.common.jmeter.links.RunLink;
import io.github.guojiaxing1995.easyJmeter.common.serializer.DeserializerObjectMapper;
import io.github.guojiaxing1995.easyJmeter.common.util.ThreadUtil;
import io.github.guojiaxing1995.easyJmeter.dto.task.TaskMachineDTO;
import io.github.guojiaxing1995.easyJmeter.model.TaskDO;
import io.github.guojiaxing1995.easyJmeter.service.JFileService;
import io.github.guojiaxing1995.easyJmeter.vo.MachineCutFileVO;
import io.socket.client.Socket;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class SocketIOClientHandler {

    private final Socket socket;

    @Autowired
    private JFileService jFileService;

    @Autowired
    private InfluxDBProperties influxDBProperties;

    @Autowired
    public SocketIOClientHandler(Socket socket){
        this.socket = socket;
        connectListener();
        disconnectListener();
        eventListeners();
        taskConfigure();
        taskRun();
        taskCollect();
        taskClean();
        taskInterrupt();
        modifyQPSLimit();
    }

    private void eventListeners() {
        socket.on("msgClient", args -> {
            log.info(args[0].toString());
            socket.emit("msgServer", "收到" + args[0].toString());
        });
    }

    //事件监听
    private void connectListener() {
        socket.on(Socket.EVENT_CONNECT, args -> {
            log.info("Connected to server");
        });
    }

    private void disconnectListener() {
        socket.on(Socket.EVENT_DISCONNECT, args -> {
            log.info("Socket disconnect");
        });
    }

    // 监听任务配置
    private void taskConfigure() {
        socket.on("taskConfigure", args -> {
            MachineCutFileVO machineCutFileVO = DeserializerObjectMapper.deserialize(args[0].toString(), MachineCutFileVO.class);
            TaskDO taskDO = machineCutFileVO.getTaskDO();
            log.info(taskDO.toString());
            log.info("收到启动命令，任务进入配置状态，任务编号：" + taskDO.getTaskId());
            // 配置逻辑
            ConfigureLink configureLink = new ConfigureLink(socket, jFileService, influxDBProperties);
            configureLink.setMachineCutFileVO(machineCutFileVO);
            configureLink.setTask(taskDO);
            configureLink.setName(taskDO.getTaskId() + "_" + JmeterStatusEnum.CONFIGURE.getDesc());
            configureLink.start();
        });
    }

    // 监听任务运行
    private void taskRun() {
        socket.on("taskRun", args -> {
            TaskDO taskDO = DeserializerObjectMapper.deserialize(args[0].toString(), TaskDO.class);
            log.info("收到运行命令，任务进入压测运行状态，任务编号：" + taskDO.getTaskId());
            RunLink runLink = new RunLink(socket);
            runLink.setName(taskDO.getTaskId() + "_" + JmeterStatusEnum.RUN.getDesc());
            runLink.setTask(taskDO);
            runLink.start();
        });
    }

    // 监听任务收集
    private void taskCollect() {
        socket.on("taskCollect", args -> {
            TaskDO taskDO = DeserializerObjectMapper.deserialize(args[0].toString(), TaskDO.class);
            log.info("收到收集命令，任务进入压测结果收集状态，任务编号：" + taskDO.getTaskId());
            CollectLink collectLink = new CollectLink(socket, jFileService);
            collectLink.setName(taskDO.getTaskId() + "_" + JmeterStatusEnum.COLLECT.getDesc());
            collectLink.setTask(taskDO);
            collectLink.start();
        });
    }

    // 监听任务清理
    private void taskClean() {
        socket.on("taskClean", args -> {
            TaskDO taskDO = DeserializerObjectMapper.deserialize(args[0].toString(), TaskDO.class);
            log.info("收到清理命令，任务进入环境清理状态，任务编号：" + taskDO.getTaskId());
            CleanLink cleanLink = new CleanLink(socket);
            cleanLink.setName(taskDO.getTaskId() + "_" + JmeterStatusEnum.CLEAN.getDesc());
            cleanLink.setTask(taskDO);
            cleanLink.start();
        });
    }

    // 监听失败后终止
    private void taskInterrupt() {
        socket.on("taskInterrupt", args -> {
            TaskMachineDTO taskMachineDTO = DeserializerObjectMapper.deserialize(args[0].toString(), TaskMachineDTO.class);
            TaskDO taskDO = taskMachineDTO.getTaskDO();
            String configureThreadName = taskDO.getTaskId() + "_" + JmeterStatusEnum.CONFIGURE.getDesc();
            String runThreadName = taskDO.getTaskId() + "_" + JmeterStatusEnum.RUN.getDesc();
            String collectThreadName = taskDO.getTaskId() + "_" + JmeterStatusEnum.COLLECT.getDesc();
            String cleanThreadName = taskDO.getTaskId() + "_" + JmeterStatusEnum.CLEAN.getDesc();
            log.info(taskDO.getTaskId() + "的" + JmeterStatusEnum.getEnumByCode(taskMachineDTO.getStatus()).getDesc() + "环节异常");
            if (taskMachineDTO.getStatus().equals(JmeterStatusEnum.CONFIGURE.getValue())){
                // 配置异常终止配置环节进入清理环节
                ThreadUtil.interruptThread(configureThreadName);
                CleanLink cleanLink = new CleanLink(socket);
                cleanLink.setName(cleanThreadName);
                cleanLink.setTask(taskDO);
                cleanLink.start();
            } else if (taskMachineDTO.getStatus().equals(JmeterStatusEnum.RUN.getValue())) {
                // 运行异常终止运行环节进入清理环节
                ThreadUtil.interruptThread(runThreadName);
                CleanLink cleanLink = new CleanLink(socket);
                cleanLink.setName(cleanThreadName);
                cleanLink.setTask(taskDO);
                cleanLink.start();
            } else if (taskMachineDTO.getStatus().equals(JmeterStatusEnum.COLLECT.getValue())) {
                // 收集异常终止收集环节进入清理环节
                ThreadUtil.interruptThread(collectThreadName);
                CleanLink cleanLink = new CleanLink(socket);
                cleanLink.setName(cleanThreadName);
                cleanLink.setTask(taskDO);
                cleanLink.start();
            }
        });

    }

    private void modifyQPSLimit() {
        socket.on("modifyQPSLimit", args -> {
            TaskDO taskDO = DeserializerObjectMapper.deserialize(args[0].toString(), TaskDO.class);
            log.info("收到修改QPS限制命令，任务编号：" + taskDO.getTaskId());
            new JmeterExternal(socket).modifyQPSLimit(taskDO);
        });
    }

}
