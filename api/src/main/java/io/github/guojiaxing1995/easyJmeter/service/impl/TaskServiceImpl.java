package io.github.guojiaxing1995.easyJmeter.service.impl;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import io.github.guojiaxing1995.easyJmeter.common.LocalUser;
import io.github.guojiaxing1995.easyJmeter.common.enumeration.JmeterStatusEnum;
import io.github.guojiaxing1995.easyJmeter.common.enumeration.MachineOnlineEnum;
import io.github.guojiaxing1995.easyJmeter.dto.task.CreateOrUpdateTaskDTO;
import io.github.guojiaxing1995.easyJmeter.mapper.CaseMapper;
import io.github.guojiaxing1995.easyJmeter.mapper.MachineMapper;
import io.github.guojiaxing1995.easyJmeter.mapper.TaskMapper;
import io.github.guojiaxing1995.easyJmeter.model.BaseModel;
import io.github.guojiaxing1995.easyJmeter.model.CaseDO;
import io.github.guojiaxing1995.easyJmeter.model.MachineDO;
import io.github.guojiaxing1995.easyJmeter.model.TaskDO;
import io.github.guojiaxing1995.easyJmeter.service.TaskService;
import io.github.talelin.autoconfigure.exception.NotFoundException;
import io.github.talelin.autoconfigure.exception.ParameterException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TaskServiceImpl implements TaskService {

    @Autowired
    private TaskMapper taskMapper;

    @Autowired
    private CaseMapper caseMapper;

    @Autowired
    private MachineMapper machineMapper;

    @Autowired
    private SocketIOServer socketServer;

    @Override
    @Transactional
    public boolean createTask(CreateOrUpdateTaskDTO taskDTO) {
        CaseDO caseDO = caseMapper.selectById(taskDTO.getJCase());
        if (caseDO == null){
            throw new NotFoundException(12201);
        }
        if (caseDO.getStatus() != JmeterStatusEnum.IDLE) {
            throw new ParameterException(12304);
        }
        TaskDO taskDO = new TaskDO();
        taskDO.setTaskId("TASK" + System.currentTimeMillis());
        taskDO.setCreator(LocalUser.getLocalUser().getId());
        taskDO.setJCase(taskDTO.getJCase());
        taskDO.setJmx(caseDO.getJmx());
        taskDO.setCsv(caseDO.getCsv());
        taskDO.setJar(caseDO.getJar());
        taskDO.setThreads(taskDTO.getThreads());
        taskDO.setDuration(taskDTO.getDuration());
        taskDO.setWarmupTime(taskDTO.getWarmupTime());
        taskDO.setQpsLimit(taskDTO.getQpsLimit());
        // 可用压力机数量小于传入num
        ArrayList<MachineDO> executableMachines = machineMapper.executable();
        if (executableMachines.size() < taskDTO.getMachineNum()) {
            throw new ParameterException(12302);
        }
        ArrayList<Integer> machines = taskDTO.getMachine();
        // 传入num 小于 传入集合数量
        if (taskDTO.getMachineNum() < machines.size()) {
            taskDO.setMachineNum(machines.size());
        } else {
            taskDO.setMachineNum(taskDTO.getMachineNum());
        }
        // 传入num 大于 传入集合数量
        if (taskDTO.getMachineNum() > machines.size()) {
            int diff = taskDTO.getMachineNum() - machines.size();
            ArrayList<Integer> executableIdList = (ArrayList<Integer>) executableMachines.stream().map(BaseModel::getId).collect(Collectors.toList());
            executableIdList.removeAll(taskDTO.getMachine());
            machines.addAll(executableIdList.subList(0, diff));
        }
        StringBuilder machinesStr = new StringBuilder();
        for (Integer machine : machines) {
            MachineDO machineDO = machineMapper.selectById(machine);
            if (machineDO == null) {
                throw new ParameterException(12303);
            }
            if (machineDO.getOnline().equals(MachineOnlineEnum.OFFLINE) || !machineDO.getJmeterStatus().equals(JmeterStatusEnum.IDLE)) {
                throw new ParameterException(12301);
            }
            machinesStr.append(machine).append(",");
        }
        taskDO.setMachine(machinesStr.substring(0, machinesStr.length() - 1));
        taskDO.setMonitor(taskDTO.getMonitor());
        taskDO.setRealtime(taskDTO.getRealtime());
        taskDO.setLog(taskDTO.getLog());
        taskDO.setRemark(taskDTO.getRemark());

        if (taskMapper.insert(taskDO) > 0) {
            // 将备选压力机加入room,发送启动命令
            List<String> clientIds = machines.stream().map(mid -> machineMapper.selectById(mid).getClientId()).collect(Collectors.toList());
            for (int i=0;i<clientIds.size();i++){
                SocketIOClient client = socketServer.getClient(UUID.fromString(clientIds.get(i)));
                client.joinRoom(taskDO.getTaskId());
            }
            // 向room中的client发送启动命令
            socketServer.getRoomOperations(taskDO.getTaskId()).sendEvent("start", taskDO.getTaskId());
        }

        return true;
    }
}
