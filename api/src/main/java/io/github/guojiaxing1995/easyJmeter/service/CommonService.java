package io.github.guojiaxing1995.easyJmeter.service;

import io.github.guojiaxing1995.easyJmeter.model.StatisticsDO;

import java.util.Map;

public interface CommonService {

    Map<String, Object> getEnum();

    Map<String, Object> getTotal();

    StatisticsDO getStatisticsById(String id);
}
