package io.github.guojiaxing1995.easyJmeter.controller.v1;

import com.alibaba.fastjson2.JSON;
import io.github.guojiaxing1995.easyJmeter.common.enumeration.JmeterStatusEnum;
import io.github.guojiaxing1995.easyJmeter.common.jmeter.links.CleanLink;
import io.github.guojiaxing1995.easyJmeter.common.jmeter.links.CollectLink;
import io.github.guojiaxing1995.easyJmeter.common.jmeter.links.ConfigureLink;
import io.github.guojiaxing1995.easyJmeter.common.jmeter.links.RunLink;
import io.github.guojiaxing1995.easyJmeter.dto.task.TaskMachineDTO;
import io.github.guojiaxing1995.easyJmeter.model.TaskDO;
import io.socket.client.Socket;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@Slf4j
@RestController
public class SocketIOClientHandler {

    private final Socket socket;

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
        test1();
        test2();
        test3();
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
            TaskDO taskDO = JSON.parseObject(args[0].toString(), TaskDO.class);
            log.info(taskDO.toString());
            log.info("收到启动命令，任务进入配置状态，任务编号：" + taskDO.getTaskId());
            // 配置逻辑
            ConfigureLink configureLink = new ConfigureLink(socket);
            configureLink.setTask(taskDO);
            configureLink.setName(taskDO.getTaskId() + "_" + JmeterStatusEnum.CONFIGURE.getDesc());
            configureLink.start();
        });
    }

    // 监听任务运行
    private void taskRun() {
        socket.on("taskRun", args -> {
            TaskDO taskDO = JSON.parseObject(args[0].toString(), TaskDO.class);
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
            TaskDO taskDO = JSON.parseObject(args[0].toString(), TaskDO.class);
            log.info("收到收集命令，任务进入压测结果收集状态，任务编号：" + taskDO.getTaskId());
            CollectLink collectLink = new CollectLink(socket);
            collectLink.setName(taskDO.getTaskId() + "_" + JmeterStatusEnum.COLLECT.getDesc());
            collectLink.setTask(taskDO);
            collectLink.start();
        });
    }

    // 监听任务清理
    private void taskClean() {
        socket.on("taskClean", args -> {
            TaskDO taskDO = JSON.parseObject(args[0].toString(), TaskDO.class);
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
            TaskMachineDTO taskMachineDTO = JSON.parseObject(args[0].toString(), TaskMachineDTO.class);
            log.info(taskMachineDTO.getTaskDO().getTaskId() + "的" + JmeterStatusEnum.getEnumByCode(taskMachineDTO.getStatus()).getDesc() + "环节异常");
            if (taskMachineDTO.getStatus().equals(JmeterStatusEnum.CONFIGURE.getValue())){
                // 配置异常终止配置环节进入清理环节
                ConfigureLink configureLink = new ConfigureLink(socket);
                configureLink.setTask(taskMachineDTO.getTaskDO());
                configureLink.interrupt();
                CleanLink cleanLink = new CleanLink(socket);
                cleanLink.setName(taskMachineDTO.getTaskDO().getTaskId() + "_" + JmeterStatusEnum.CLEAN.getDesc());
                cleanLink.setTask(taskMachineDTO.getTaskDO());
                cleanLink.start();
            } else if (taskMachineDTO.getStatus().equals(JmeterStatusEnum.RUN.getValue())) {
                // 运行异常终止运行环节进入收集环节
                RunLink runLink = new RunLink(socket);
                runLink.setTask(taskMachineDTO.getTaskDO());
                runLink.interrupt();
                CollectLink collectLink = new CollectLink(socket);
                collectLink.setName(taskMachineDTO.getTaskDO().getTaskId() + "_" + JmeterStatusEnum.COLLECT.getDesc());
                collectLink.setTask(taskMachineDTO.getTaskDO());
                collectLink.start();
            } else if (taskMachineDTO.getStatus().equals(JmeterStatusEnum.COLLECT.getValue())) {
                // 收集异常终止收集环节进入清理环节
                CollectLink collectLink = new CollectLink(socket);
                collectLink.setTask(taskMachineDTO.getTaskDO());
                collectLink.interrupt();
                CleanLink cleanLink = new CleanLink(socket);
                cleanLink.setName(taskMachineDTO.getTaskDO().getTaskId() + "_" + JmeterStatusEnum.CLEAN.getDesc());
                cleanLink.setTask(taskMachineDTO.getTaskDO());
                cleanLink.start();
            }
        });

    }

    private void test1() {
        socket.on("test1", args -> {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    // 线程需要执行的任务代码
                    System.out.println("子线程开始启动....");
//                    while (true) {
//                        log.info("66666666666666666666666666666666");
//                    }
//                    for (int i=0; i< 30; i++) {
//                        try {
//                            Thread.sleep(3000);
//                            log.info(String.valueOf(i));
//                        } catch (InterruptedException e) {
//                            throw new RuntimeException(e);
//                        }
//
//                    }
                    ProcessBuilder processBuilder = new ProcessBuilder("sh","/opt/dg/run.sh");
                    processBuilder.environment().putAll(System.getenv());
                    StringBuilder outputString = new StringBuilder();
                    try {
                        Process process = processBuilder.start();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                        String line;
                        while ((line = reader.readLine()) != null) {
                            outputString.append(line).append("\n");
                            log.info(String.valueOf(Thread.currentThread().isInterrupted()));
                            if (Thread.currentThread().isInterrupted()) {
                                System.out.println("青秧线程被中断，程序退出。");
                                process.destroy();
                            }
                            log.info(String.valueOf(outputString));
                        }
                        int exitCode = process.waitFor();

                    } catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
            thread.setName("my_test");
            thread.start();
        });
    }

    private void test2() {
        socket.on("test2", args -> {
            ThreadGroup currentGroup = Thread.currentThread().getThreadGroup();
            int noThreads = currentGroup.activeCount();
            Thread[] lstThreads = new Thread[noThreads];
            currentGroup.enumerate(lstThreads);
            for (int i = 0; i < noThreads; i++) {
                log.info(lstThreads[i].getName());
            }
        });
    }

    private void test3() {
        socket.on("test3", args -> {
            ThreadGroup currentGroup = Thread.currentThread().getThreadGroup();
            int noThreads = currentGroup.activeCount();
            Thread[] lstThreads = new Thread[noThreads];
            currentGroup.enumerate(lstThreads);
            log.info("---------------------------------");
            for (int i = 0; i < noThreads; i++) {
                log.info(lstThreads[i].getName());
                if (lstThreads[i].getName().equals("my_test")) {
                    log.info(String.valueOf(lstThreads[i].getState()));
                    log.info(String.valueOf(lstThreads[i].isInterrupted()));
                    log.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++");
                    lstThreads[i].interrupt();
                    log.info(String.valueOf(lstThreads[i].getState()));
                    log.info(String.valueOf(lstThreads[i].isInterrupted()));
                }
            }
        });
    }

}
