package io.github.guojiaxing1995.easyJmeter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.github.guojiaxing1995.easyJmeter.common.enumeration.JmeterStatusEnum;
import io.github.guojiaxing1995.easyJmeter.common.enumeration.LogLevelEnum;
import io.github.guojiaxing1995.easyJmeter.common.enumeration.TaskResultEnum;
import io.github.guojiaxing1995.easyJmeter.mapper.CaseMapper;
import io.github.guojiaxing1995.easyJmeter.mapper.MachineMapper;
import io.github.guojiaxing1995.easyJmeter.mapper.ProjectMapper;
import io.github.guojiaxing1995.easyJmeter.mapper.TaskMapper;
import io.github.guojiaxing1995.easyJmeter.model.MachineDO;
import io.github.guojiaxing1995.easyJmeter.model.TaskDO;
import io.github.guojiaxing1995.easyJmeter.repository.ReportRepository;
import io.github.guojiaxing1995.easyJmeter.service.CommonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class CommonServiceImpl implements CommonService {

    @Autowired
    private ProjectMapper projectMapper;

    @Autowired
    private CaseMapper caseMapper;

    @Autowired
    private MachineMapper machineMapper;

    @Autowired
    private TaskMapper taskMapper;

    @Autowired
    private ReportRepository reportRepository;

    @Override
    public Map<String, Object> getEnum() {
        Map<String, Object> map = new HashMap<>();
        map.put("JmeterStatus", JmeterStatusEnum.toMapList());
        map.put("TaskResult", TaskResultEnum.toMapList());
        map.put("LogLevel", LogLevelEnum.toMapList());

        return map;
    }

    @Override
    public Map<String, Object> getTotal() {
        Map<String, Object> map = new HashMap<>();
        map.put("projectNum", projectMapper.selectCount(new QueryWrapper<>()));
        map.put("caseNum", caseMapper.selectCount(new QueryWrapper<>()));
        map.put("machineNum", machineMapper.selectCount(new QueryWrapper<MachineDO>().eq("is_online", true)));
        map.put("taskNum", taskMapper.selectCount(new QueryWrapper<TaskDO>().eq("result", 1)));
        map.put("durationSum", taskMapper.selectSumDuration(null));
        return map;
    }
}
