package io.github.guojiaxing1995.easyJmeter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.github.benmanes.caffeine.cache.Cache;
import io.github.guojiaxing1995.easyJmeter.common.LocalUser;
import io.github.guojiaxing1995.easyJmeter.common.enumeration.JmeterStatusEnum;
import io.github.guojiaxing1995.easyJmeter.common.enumeration.LogLevelEnum;
import io.github.guojiaxing1995.easyJmeter.common.enumeration.TaskResultEnum;
import io.github.guojiaxing1995.easyJmeter.common.util.CSVUtil;
import io.github.guojiaxing1995.easyJmeter.dto.task.CreateOrUpdateTaskDTO;
import io.github.guojiaxing1995.easyJmeter.dto.task.ModifyTaskDTO;
import io.github.guojiaxing1995.easyJmeter.dto.task.TaskMachineDTO;
import io.github.guojiaxing1995.easyJmeter.mapper.*;
import io.github.guojiaxing1995.easyJmeter.model.*;
import io.github.guojiaxing1995.easyJmeter.service.JFileService;
import io.github.guojiaxing1995.easyJmeter.service.TaskLogService;
import io.github.guojiaxing1995.easyJmeter.service.TaskService;
import io.github.guojiaxing1995.easyJmeter.vo.CutFileVO;
import io.github.guojiaxing1995.easyJmeter.vo.MachineCutFileVO;
import io.github.guojiaxing1995.easyJmeter.vo.TaskProgressVO;
import io.github.talelin.autoconfigure.exception.NotFoundException;
import io.github.talelin.autoconfigure.exception.ParameterException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
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
    private TaskLogMapper taskLogMapper;

    @Autowired
    private SocketIOServer socketServer;

    @Autowired
    private JFileMapper jFileMapper;
    
    @Autowired
    private JFileService jFileService;

    @Autowired
    private TaskLogService taskLogService;

    @Autowired
    Cache<String, Object> caffeineCache;

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
        taskDO.setJmeterCase(taskDTO.getJCase());
        taskDO.setJmx(caseDO.getJmx());
        taskDO.setCsv(caseDO.getCsv());
        taskDO.setJar(caseDO.getJar());
        taskDO.setNumThreads(taskDTO.getNumThreads());
        taskDO.setDuration(taskDTO.getDuration());
        taskDO.setRampTime(taskDTO.getRampTime());
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
            if (machineDO.getIsOnline().equals(false) || !machineDO.getJmeterStatus().equals(JmeterStatusEnum.IDLE)) {
                throw new ParameterException(12301);
            }
            machinesStr.append(machine).append(",");
        }
        taskDO.setMachine(machinesStr.substring(0, machinesStr.length() - 1));
        taskDO.setMonitor(taskDTO.getMonitor());
        taskDO.setRealtime(taskDTO.getRealtime());
        taskDO.setLogLevel(LogLevelEnum.getEnumByCode(taskDTO.getLogLevel()));
        taskDO.setRemark(taskDTO.getRemark());
        taskDO.setResult(TaskResultEnum.IN_PROGRESS);

        if (taskMapper.insert(taskDO) > 0) {
            // 将备选压力机加入room,发送启动命令
            List<String> clientIds = machines.stream().map(mid -> machineMapper.selectById(mid).getClientId()).collect(Collectors.toList());
            for (String clientId : clientIds) {
                SocketIOClient client = socketServer.getClient(UUID.fromString(clientId));
                client.joinRoom(taskDO.getTaskId());
            }
            // 锁定机器和case的jmeter状态,记录task机器日志
            caseDO.setStatus(JmeterStatusEnum.CONFIGURE);
            caseMapper.updateById(caseDO);
            for (Integer machine : machines) {
                MachineDO machineDO = machineMapper.selectById(machine);
                machineDO.setJmeterStatus(JmeterStatusEnum.CONFIGURE);
                machineMapper.updateById(machineDO);
                // 记录task日志
                taskLogMapper.insert(new TaskLogDO(taskDO.getTaskId(),caseDO.getId(),JmeterStatusEnum.CONFIGURE,null,machineDO.getAddress(),machineDO.getId()));
            }
            // 判断设置是否需要切分并设置切分状态
            Boolean needCut = (taskDO.getCsv() != null && !taskDO.getCsv().isEmpty()) ? jFileService.needCut(taskDO.getCsv().split(",")) : false;
            MachineCutFileVO machineCutFileVO = new MachineCutFileVO(null, taskDO, needCut);
            // 向room中的client发送启动命令
            log.info("========TaskDO=======: {}", taskDO);
            socketServer.getRoomOperations(taskDO.getTaskId()).sendEvent("taskConfigure", machineCutFileVO);
            // 向web端报告进度
            TaskProgressVO taskProgressVO = new TaskProgressVO(taskDO.getTaskId(), JmeterStatusEnum.CONFIGURE, null, TaskResultEnum.IN_PROGRESS);
            socketServer.getRoomOperations("web").sendEvent("taskProgress", taskProgressVO);
        }

        return true;
    }

    @Override
    public boolean updateTaskResult(TaskDO taskDO, TaskResultEnum result) {
        taskDO.setResult(result);
        return taskMapper.updateById(taskDO) > 0;
    }

    @Override
    public TaskDO getTaskById(Integer id) {
        return taskMapper.selectById(id);
    }

    @Override
    public Map<String, List<CutFileVO>> cutCsv(TaskDO taskDO) {
        String[] machinesArray = taskDO.getMachine().split(",");
        String[] csvFileArray = (taskDO.getCsv() != null && !taskDO.getCsv().isEmpty()) ? taskDO.getCsv().split(",") : new String[]{};
        List<MachineDO> machines = Arrays.stream(machinesArray).map(Integer::parseInt).map(machineMapper::selectById).collect(Collectors.toList());
        List<JFileDO> jFiles = Arrays.stream(csvFileArray).map(Integer::parseInt).map(jFileMapper::selectById).collect(Collectors.toList());

        Map<String, List<CutFileVO>> map = new HashMap<>();
        for (MachineDO machineDO: machines) {
            map.put(machineDO.getAddress(), new ArrayList<>());
        }
        for (JFileDO jFileDO: jFiles) {
            if (jFileDO.getCut()){
                List<CutFileVO> cutFileVOList = new ArrayList<>();
                String csvPath = jFileService.downloadFile(jFileDO.getId(), null);
                CSVUtil csvUtil = new CSVUtil(csvPath, taskDO.getMachineNum(), jFileDO.getId());
                Map<Integer, List<String>> fileMap = csvUtil.splitCSVFile();
                List<JFileDO> cutFiles = jFileService.createFiles(fileMap);
                for (JFileDO cutFileDO: cutFiles) {
                    cutFileDO.setTaskId(taskDO.getTaskId());
                    jFileMapper.updateById(cutFileDO);
                    JFileDO originFile = jFileMapper.selectById(cutFileDO.getOriginId());
                    CutFileVO cutFileVO = new CutFileVO(cutFileDO, originFile.getName());
                    cutFileVOList.add(cutFileVO);
                }
                for (int i=0;i<machines.size();i++) {
                    List<CutFileVO> cutFileVO = map.get(machines.get(i).getAddress());
                    cutFileVO.add(cutFileVOList.get(i));
                    map.put(machines.get(i).getAddress(), cutFileVO);
                }

            }
        }
        return map;
    }

    @Override
    public TaskDO getTaskByTaskId(String taskId) {
        QueryWrapper<TaskDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("task_id", taskId);

        return taskMapper.selectOne(queryWrapper);
    }


    @Override
    public boolean stopTask(String taskId) {
        TaskDO taskDO = getTaskByTaskId(taskId);
        if (taskDO == null) {
            throw new ParameterException(12305);
        }
        CaseDO caseDO = caseMapper.selectById(taskDO.getJmeterCase());
        if (caseDO.getStatus() == JmeterStatusEnum.IDLE) {
            throw new ParameterException(12305);
        }

        // 更新task日志为失败
        List<TaskLogDO> taskLogs = taskLogService.getTaskLog(taskDO.getTaskId(), taskDO.getJmeterCase(), caseDO.getStatus(), null, null);
        for (TaskLogDO taskLog: taskLogs) {
            taskLogService.updateTaskLog(taskLog, false);
        }
        // 标记task状态为失败
        updateTaskResult(taskDO, TaskResultEnum.EXCEPTION);

        // 如果没有发送过终止消息，向所有agent发送消息进行终止和进入下一环节
        synchronized (this) {
            if (caffeineCache.getIfPresent(taskDO.getTaskId() + "_" + caseDO.getStatus()) == null) {
                caffeineCache.put(taskDO.getTaskId() + "_" + caseDO.getStatus(), "taskInterrupt");
                socketServer.getRoomOperations(taskDO.getTaskId()).sendEvent("taskInterrupt", new TaskMachineDTO(taskDO, null, false, caseDO.getStatus().getValue()));
            }
        }

        // 向web端报告进度
        TaskProgressVO taskProgressVO = new TaskProgressVO(taskDO.getTaskId(), JmeterStatusEnum.INTERRUPT, null, TaskResultEnum.EXCEPTION);
        socketServer.getRoomOperations("web").sendEvent("taskProgress", taskProgressVO);

        return true;
    }

    @Override
    public boolean modifyQPSLimit(ModifyTaskDTO validator) {
        TaskDO taskDO = getTaskByTaskId(validator.getTaskId());
        if (taskDO == null) {
            throw new ParameterException(12305);
        }
        CaseDO caseDO = caseMapper.selectById(taskDO.getJmeterCase());
        if (caseDO.getStatus() != JmeterStatusEnum.RUN) {
            throw new ParameterException(12306);
        }
        taskDO.setQpsLimit(validator.getQpsLimit());
        taskMapper.updateById(taskDO);

        socketServer.getRoomOperations(taskDO.getTaskId()).sendEvent("modifyQPSLimit", taskDO);

        return true;
    }
}
