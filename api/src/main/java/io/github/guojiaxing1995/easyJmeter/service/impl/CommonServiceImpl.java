package io.github.guojiaxing1995.easyJmeter.service.impl;

import io.github.guojiaxing1995.easyJmeter.common.enumeration.JmeterStatusEnum;
import io.github.guojiaxing1995.easyJmeter.common.enumeration.LogLevelEnum;
import io.github.guojiaxing1995.easyJmeter.common.enumeration.TaskResultEnum;
import io.github.guojiaxing1995.easyJmeter.service.CommonService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class CommonServiceImpl implements CommonService {
    @Override
    public Map<String, Object> getEnum() {
        Map<String, Object> map = new HashMap<>();
        map.put("JmeterStatus", JmeterStatusEnum.toMapList());
        map.put("TaskResult", TaskResultEnum.toMapList());
        map.put("LogLevel", LogLevelEnum.toMapList());

        return map;
    }
}
