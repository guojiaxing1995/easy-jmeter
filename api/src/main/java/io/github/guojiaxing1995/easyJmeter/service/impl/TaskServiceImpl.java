package io.github.guojiaxing1995.easyJmeter.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.github.benmanes.caffeine.cache.Cache;
import io.github.guojiaxing1995.easyJmeter.common.LocalUser;
import io.github.guojiaxing1995.easyJmeter.common.enumeration.JmeterStatusEnum;
import io.github.guojiaxing1995.easyJmeter.common.enumeration.LogLevelEnum;
import io.github.guojiaxing1995.easyJmeter.common.enumeration.TaskResultEnum;
import io.github.guojiaxing1995.easyJmeter.common.mybatis.Page;
import io.github.guojiaxing1995.easyJmeter.common.util.CSVUtil;
import io.github.guojiaxing1995.easyJmeter.dto.task.CreateOrUpdateTaskDTO;
import io.github.guojiaxing1995.easyJmeter.dto.task.ModifyTaskDTO;
import io.github.guojiaxing1995.easyJmeter.dto.task.TaskMachineDTO;
import io.github.guojiaxing1995.easyJmeter.mapper.*;
import io.github.guojiaxing1995.easyJmeter.model.*;
import io.github.guojiaxing1995.easyJmeter.repository.ReportRepository;
import io.github.guojiaxing1995.easyJmeter.service.JFileService;
import io.github.guojiaxing1995.easyJmeter.service.TaskLogService;
import io.github.guojiaxing1995.easyJmeter.service.TaskService;
import io.github.guojiaxing1995.easyJmeter.vo.*;
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
    private UserMapper userMapper;

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

    @Autowired
    private ReportRepository reportRepository;

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
        taskDO.setGranularity(taskDTO.getGranularity());
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
                // 向web端发送task日志
                socketServer.getRoomOperations("web").sendEvent("taskLogs", this.getTaskLogByTaskId(taskDO.getTaskId()));
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
        // 更新mongo中task结果
        ReportDO reportDO = reportRepository.findById(taskDO.getTaskId()).orElse(null);
        if (reportDO != null) {
            reportDO.setResult(result);
            reportRepository.save(reportDO);
        }
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
                List<JFileDO> cutFiles = jFileService.createCsvCutFiles(fileMap);
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
        TaskDO taskDO = taskMapper.selectOne(queryWrapper);
        if (taskDO == null) {
            throw new ParameterException(12307);
        }

        return taskDO;
    }


    @Override
    public boolean stopTask(String taskId) {
        TaskDO taskDO = getTaskByTaskId(taskId);
        if (taskDO == null) {
            throw new ParameterException(12305);
        }
        if (taskDO.getResult() != TaskResultEnum.IN_PROGRESS) {
            throw new ParameterException(12308);
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
        updateTaskResult(taskDO, TaskResultEnum.MANUAL);

        // 如果没有发送过终止消息，向所有agent发送消息进行终止和进入下一环节
        synchronized (this) {
            if (caffeineCache.getIfPresent(taskDO.getTaskId() + "_" + caseDO.getStatus()) == null) {
                caffeineCache.put(taskDO.getTaskId() + "_" + caseDO.getStatus(), "taskInterrupt");
                socketServer.getRoomOperations(taskDO.getTaskId()).sendEvent("taskInterrupt", new TaskMachineDTO(taskDO, null, false, caseDO.getStatus().getValue()));
            }
        }

        // 向web端报告进度
        TaskProgressVO taskProgressVO = new TaskProgressVO(taskDO.getTaskId(), JmeterStatusEnum.INTERRUPT, null, TaskResultEnum.MANUAL);
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

    @Override
    public TaskInfoVO getTaskInfo(String taskId) {
        TaskDO taskDO = this.getTaskByTaskId(taskId);
        String[] csvFileIds = (taskDO.getCsv() != null && !taskDO.getCsv().isEmpty()) ? taskDO.getCsv().split(",") : new String[]{};
        List<JFileDO> csvFileList = new ArrayList<>();
        for (String csvFileId : csvFileIds){
            JFileDO jFileCsvDO = jFileService.searchById(Integer.valueOf(csvFileId));
            if (jFileCsvDO!= null) {
                csvFileList.add(jFileCsvDO);
            }
        }
        String[] jmxFileIds = (taskDO.getJmx() != null && !taskDO.getJmx().isEmpty()) ? taskDO.getJmx().split(",") : new String[]{};
        List<JFileDO> jmxFileList = new ArrayList<>();
        for (String jmxFileId : jmxFileIds){
            JFileDO jFileJmxDO = jFileService.searchById(Integer.valueOf(jmxFileId));
            if (jFileJmxDO!= null) {
                jmxFileList.add(jFileJmxDO);
            }
        }
        String[] jarFileIds = (taskDO.getJar() != null && !taskDO.getJar().isEmpty()) ? taskDO.getJar().split(",") : new String[]{};
        List<JFileDO> jarFileList = new ArrayList<>();
        for (String jarFileId : jarFileIds){
            JFileDO jFileJarDO = jFileService.searchById(Integer.valueOf(jarFileId));
            if (jFileJarDO != null) {
                jarFileList.add(jFileJarDO);
            }
        }
        String[] machineIds = taskDO.getMachine().split(",");
        List<MachineDO> machineList = new ArrayList<>();
        for (String machineId : machineIds){
            MachineDO machineDO = machineMapper.selectById(Integer.valueOf(machineId));
            if (machineDO != null) {
                machineList.add(machineDO);
            }
        }
        CaseDO caseDO = caseMapper.selectById(taskDO.getJmeterCase());
        UserDO userDO = userMapper.selectById(taskDO.getCreator());

        return new TaskInfoVO(taskDO, caseDO, userDO, jmxFileList, csvFileList, jarFileList, machineList);
    }

    @Override
    public List<Map<String, Object>> getTaskLogByTaskId(String taskId) {
        QueryWrapper<TaskLogDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("status as status, min(DATE_FORMAT(create_time,'%Y-%m-%d %H:%i:%s')) as create_time").eq("task_id", taskId).groupBy("status");
        List<Map<String, Object>> levelList = taskLogMapper.selectMaps(queryWrapper);
        for (Map<String, Object> level : levelList) {
            QueryWrapper<TaskLogDO> query = new QueryWrapper<>();
            query.select("*").eq("task_id", taskId).eq("status", level.get("status"));
            List<TaskLogDO> taskLogDOS = taskLogMapper.selectList(query);
            level.put("logs", taskLogDOS);
            JmeterStatusEnum status = JmeterStatusEnum.getEnumByCode((Integer) level.get("status"));
            level.put("status", status);
        }
        log.info(levelList.toString());


        return levelList;
    }

    @Override
    public ReportDO getTaskReportByTaskId(String taskId) {
        ReportDO reportDO = reportRepository.findNonDeletedById(taskId).orElse(null);
        if (reportDO != null) {
            return reportDO;
        } else {
            ReportDO report = new ReportDO();
            report.setTaskId(taskId);
            Map<String, List<JSONObject>> stringListMap = new HashMap<>();
            stringListMap.put("statisticsTable", new ArrayList<>());
            report.setDashBoardData(stringListMap);
            return report;
        }
    }

    @Override
    public IPage<HistoryTaskVO> getHistoryTask(Integer current, String jmeterCase, String taskId, String startTime, String endTime, Integer result) {
        Page page = new Page(current, 10);
        return taskMapper.selectHistory(page, taskId, startTime, endTime, result, jmeterCase);
    }

    @Override
    public boolean deleteTasks(List<Integer> ids) {
        int deleted = taskMapper.deleteBatchIds(ids);
        for (Integer id : ids) {
            TaskDO taskDO = taskMapper.selectByIdIncludeDelete(id);
            ReportDO reportDO = reportRepository.findNonDeletedById(taskDO.getTaskId()).orElse(null);
            if (reportDO != null) {
                reportDO.setDeleteTime(taskDO.getDeleteTime());
                reportRepository.save(reportDO);
            }
        }
        return  deleted > 0;
    }
}
