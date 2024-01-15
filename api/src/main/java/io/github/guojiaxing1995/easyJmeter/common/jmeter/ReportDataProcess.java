package io.github.guojiaxing1995.easyJmeter.common.jmeter;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import io.github.guojiaxing1995.easyJmeter.model.JFileDO;
import io.github.guojiaxing1995.easyJmeter.model.ReportDO;
import io.github.guojiaxing1995.easyJmeter.model.StatisticsDO;
import io.github.guojiaxing1995.easyJmeter.model.TaskDO;
import io.github.guojiaxing1995.easyJmeter.repository.ReportRepository;
import io.github.guojiaxing1995.easyJmeter.repository.StatisticsRepository;
import io.github.guojiaxing1995.easyJmeter.vo.CaseInfoVO;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
public class ReportDataProcess {

    private  ReportRepository reportRepository;

    private StatisticsRepository statisticsRepository;

    public ReportDataProcess() {}
    
    public ReportDataProcess(ReportRepository reportRepository, StatisticsRepository statisticsRepository) {
        this.statisticsRepository = statisticsRepository;
        this.reportRepository = reportRepository;
    }

    public Map<String, List<JSONObject>> getDashBoardData(String jsPath) {
        StringBuilder contentBuilder = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(jsPath))) {
            String line;
            while ((line = br.readLine()) != null) {
                contentBuilder.append(line).append(System.lineSeparator());
            }
        } catch (IOException e) {
            log.error("读取js文件失败", e);
        }
        String jsStr = contentBuilder.toString();

        Map<String, List<JSONObject>> map = new HashMap<>();
        Pattern statisticsPattern = Pattern.compile("createTable\\(\\$\\(\"#statisticsTable\"\\),([\\s\\S]*?), function\\(index, item\\)\\{");
        Matcher statisticsPatternMatcher = statisticsPattern.matcher(jsStr);
        if (statisticsPatternMatcher.find()) {
            JSONObject statistics = JSONObject.parseObject(statisticsPatternMatcher.group(1));
            List<String> titlesList = List.of("label", "samples", "fail", "error", "average", "min", "max", "median", "90th", "95th", "99th", "transactions", "received", "Sent");
            statistics.put("titles", titlesList);
            List<JSONObject> combinedList = new ArrayList<>(statistics.getJSONArray("overall").toJavaList(JSONObject.class));
            combinedList.addAll(statistics.getJSONArray("items").toJavaList(JSONObject.class));
            statistics.put("items", combinedList);
            List<JSONObject> statisticsTable = matchList(statistics);
            map.put("statisticsTable", statisticsTable);

        }

        Pattern errorsPattern = Pattern.compile("createTable\\(\\$\\(\"#errorsTable\"\\),([\\s\\S]*?), function\\(index, item\\)\\{");
        Matcher errorsPatternMatcher = errorsPattern.matcher(jsStr);
        if (errorsPatternMatcher.find()) {
            JSONObject errors = JSONObject.parseObject(errorsPatternMatcher.group(1));
            List<String> titlesList = List.of("type", "number", "currentPercent", "allPercent");
            errors.put("titles", titlesList);
            List<JSONObject> errorsTable = matchList(errors);
            map.put("errorsTable", errorsTable);
        }

        Pattern top5ErrorsPattern = Pattern.compile("createTable\\(\\$\\(\"#top5ErrorsBySamplerTable\"\\),([\\s\\S]*?), function\\(index, item\\)\\{");
        Matcher top5ErrorsPatternMatcher = top5ErrorsPattern.matcher(jsStr);
        if (top5ErrorsPatternMatcher.find()) {
            JSONObject top5Errors = JSONObject.parseObject(top5ErrorsPatternMatcher.group(1));
            List<String> titlesList = List.of("sample", "samples", "errors", "errorA", "errorsA", "errorB", "errorsB", "errorC", "errorsC", "errorD", "errorsD", "errorE", "errorsE");
            top5Errors.put("titles", titlesList);
            List<JSONObject> combinedList = new ArrayList<>(top5Errors.getJSONArray("overall").toJavaList(JSONObject.class));
            combinedList.addAll(top5Errors.getJSONArray("items").toJavaList(JSONObject.class));
            top5Errors.put("items", combinedList);
            List<JSONObject> top5ErrorsBySamplerTable = this.matchList(top5Errors);
            map.put("top5ErrorsBySamplerTable", top5ErrorsBySamplerTable);
        }
        log.info("获取DashBoardData完成");
        return map;
    }
    public List<JSONObject> matchList(JSONObject jsonObject) {
        List<String> titles = jsonObject.getJSONArray("titles").toJavaList(String.class);
        List<JSONObject> items = jsonObject.getJSONArray("items").toJavaList(JSONObject.class);
        List<Map<String, Object>> resultList = new ArrayList<>();

        for (JSONObject item : items) {
            Map<String, Object> resultMap = new LinkedHashMap<>();
            JSONArray data = item.getJSONArray("data");
            for (int i = 0; i < titles.size(); i++) {
                if (i < data.size()) {
                    resultMap.put(titles.get(i), data.get(i));
                }
            }
            resultList.add(resultMap);
        }

        String jsonString = JSON.toJSONString(resultList);
        List<JSONObject> list = JSON.parseArray(jsonString, JSONObject.class);
        return list;
    }

    public Map<String, JSONObject> getGraphData(String jsPath) {
        Map<String, JSONObject> map = new HashMap<>();
        Pattern pattern = Pattern.compile("var\\s+([a-zA-Z_$][a-zA-Z\\d_$]*)\\s*=\\s*\\{");
        StringBuilder contentBuilder = new StringBuilder();

        try (BufferedReader br = new BufferedReader(new FileReader(jsPath))) {
            String line;
            while ((line = br.readLine()) != null) {
                contentBuilder.append(line).append(System.lineSeparator());
                Matcher matcher = pattern.matcher(line);
                if (matcher.find()) {
                    String key = matcher.group(1);
                    map.put(key, null);
                }
            }
        } catch (IOException e) {
            log.error("读取js文件失败", e);
        }
        String jsStr = contentBuilder.toString();

        for (String key : map.keySet()) {
            String patternString = key + "\\s*=\\s*\\{[\\s\\S]*?data\\s*:\\s*(\\{[\\s\\S]*?\\})[^}]*getOptions[^}]*\\}";
            Pattern p = Pattern.compile(patternString);
            Matcher m = p.matcher(jsStr);
            if (m.find()) {
                JSONObject data = JSONObject.parseObject(m.group(1));
                map.put(key, data.getJSONObject("result"));
            }
        }
        log.info("获取GraphData完成");
        return this.dealGraphData(map);
    }

    public ReportDO getData(TaskDO taskDO, String reportPath, JFileDO jFileDO) {
        Map<String, List<JSONObject>> dashBoardData = this.getDashBoardData(new File(reportPath, "content/js/dashboard.js").toString());
        Map<String, JSONObject> graphData = this.getGraphData(new File(reportPath, "content/js/graph.js").toString());
        ReportDO reportDO = new ReportDO();
        reportDO.setTaskId(taskDO.getTaskId());
        reportDO.setGraphData(graphData);
        reportDO.setDashBoardData(dashBoardData);
        reportDO.setFile(jFileDO);
        reportDO.setCaseId(taskDO.getJmeterCase());
        reportDO.setCreateTime(taskDO.getCreateTime());
        reportDO.setResult(taskDO.getResult());

        return reportDO;
    }

    // 数据处理，适配echarts
    public Map<String, JSONObject> dealGraphData(Map<String, JSONObject> graphData) {
        // responseTimesOverTimeInfos
        JSONObject responseTimesOverTimeInfos = graphData.get("responseTimesOverTimeInfos");
        responseTimesOverTimeInfos.put("titleCN", "平均响应时间");
        responseTimesOverTimeInfos.put("yName", "平均响应时间(ms)");
        JSONObject responseTimesOverTime = this.dealSeries(responseTimesOverTimeInfos, false);
        graphData.put("responseTimesOverTimeInfos", responseTimesOverTime);
        // transactionsPerSecondInfos
        JSONObject transactionsPerSecondInfos = graphData.get("transactionsPerSecondInfos");
        transactionsPerSecondInfos.put("titleCN", "TPS");
        transactionsPerSecondInfos.put("yName", "事务数/秒");
        JSONObject transactionsPerSecond = this.dealSeries(transactionsPerSecondInfos, false);
        graphData.put("transactionsPerSecondInfos", transactionsPerSecond);
        // activeThreadsOverTimeInfos
        JSONObject activeThreadsOverTimeInfos = graphData.get("activeThreadsOverTimeInfos");
        activeThreadsOverTimeInfos.put("titleCN", "活动线程数");
        activeThreadsOverTimeInfos.put("yName", "线程数");
        JSONObject activeThreadsOverTime = this.dealSeries(activeThreadsOverTimeInfos, true);
        graphData.put("activeThreadsOverTimeInfos", activeThreadsOverTime);
        // totalTPSInfos
        JSONObject totalTPSInfos = graphData.get("totalTPSInfos");
        totalTPSInfos.put("titleCN", "总TPS");
        totalTPSInfos.put("yName", "事务数/秒");
        JSONObject totalTPS = this.dealSeries(totalTPSInfos, false);
        graphData.put("totalTPSInfos", totalTPS);
        // responseTimePercentilesOverTimeInfos
        JSONObject responseTimePercentilesOverTimeInfos = graphData.get("responseTimePercentilesOverTimeInfos");
        responseTimePercentilesOverTimeInfos.put("titleCN", "随时间变化的响应时间百分位数（成功响应）");
        responseTimePercentilesOverTimeInfos.put("yName", "响应时间(ms)");
        JSONObject responseTimePercentilesOverTime = this.dealSeries(responseTimePercentilesOverTimeInfos, true);
        graphData.put("responseTimePercentilesOverTimeInfos", responseTimePercentilesOverTime);

        log.info("dealGraphData完成");
        return graphData;
    }

    public JSONObject dealSeries(JSONObject infos, boolean areaStyle) {
        List<String> labels = new ArrayList<>();
        List<JSONObject> series = infos.getJSONArray("series").toJavaList(JSONObject.class);
        for (JSONObject seriesItem : series) {
            seriesItem.put("name", seriesItem.get("label"));
            seriesItem.put("type", "line");
            if (areaStyle) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("opacity", 0.6);
                seriesItem.put("areaStyle", jsonObject);
            }
            labels.add(seriesItem.get("label").toString());
            JSONArray seriesItemData = seriesItem.getJSONArray("data");
            List<List<Object>> sortedData = seriesItemData.stream()
                    .map(item -> (JSONArray) item)
                    .map(array -> {
                        Object a = ((Number)array.get(0)).longValue();
                        BigDecimal b = ((BigDecimal) array.get(1)).setScale(1, RoundingMode.HALF_UP);
                        return List.of(a, b);
                    })
                    .collect(Collectors.toList());
            Collections.sort(sortedData, Comparator.comparingLong(list -> (long) list.get(0)));
            seriesItem.put("data", sortedData);
        }
        infos.put("series", series);
        infos.put("labels", labels);

        return infos;
    }

    public void statistics(List<TaskDO> taskDOS, CaseInfoVO caseInfoVO, String manualName) {
        StatisticsDO statisticsDO = new StatisticsDO();
        if (caseInfoVO==null) {
            statisticsDO.setCaseId("manual" + System.currentTimeMillis());
            statisticsDO.setName(manualName);
        } else {
            statisticsDO.setCaseId(caseInfoVO.getId().toString());
            statisticsDO.setName(caseInfoVO.getName());
        }
        List<String> taskIdList = taskDOS.stream().map(TaskDO::getTaskId).collect(Collectors.toList());
        statisticsDO.setTaskIdList(taskIdList);
        statisticsDO.setTaskNum(taskDOS.size());

        long samplesSum = 0L;
        long durationSum = 0L;
        long errorsSum = 0L;
        double throughputSum = 0.0;
        double errorRateSum = 0.0;
        double averageResponseTimeSum = 0.0;
        double averageMinResponseTimeSum = 0.0;
        double averageMaxResponseTimeSum = 0.0;
        double average90thResponseTimeSum = 0.0;
        double average95thResponseTimeSum = 0.0;
        double average99thResponseTimeSum = 0.0;
        double averageMedianResponseTimeSum = 0.0;
        double receivedSum = 0.0;
        double sentSum = 0.0;
        List<List<Object>> throughputData = new ArrayList<>();
        List<List<Object>> responseTime99thData = new ArrayList<>();
        List<List<Object>> responseTime90thData = new ArrayList<>();
        List<List<Object>> responseTime95thData = new ArrayList<>();
        List<List<Object>> responseTimeMedianData = new ArrayList<>();
        List<List<Object>> responseTimeAverageData = new ArrayList<>();
        List<List<Object>> errorRateData = new ArrayList<>();
        for (TaskDO taskDO : taskDOS) {
            // 测试总时长
            durationSum += taskDO.getDuration();
            // task 创建时间
            long createTime = taskDO.getCreateTime().getTime();
            List<JSONObject> taskStatisticsTable = reportRepository.getTaskStatisticsTable(taskDO.getTaskId());
            if (!taskStatisticsTable.isEmpty()) {
                JSONObject taskStatistics = taskStatisticsTable.get(0).getJSONObject("totalJson");
                // 请求数总数
                samplesSum += Long.parseLong(taskStatistics.get("samples").toString());
                // 错误总数
                errorsSum += Long.parseLong(taskStatistics.get("fail").toString());
                // 吞吐量总数
                throughputSum += Double.parseDouble(taskStatistics.get("transactions").toString());
                // 错误率和
                errorRateSum += Double.parseDouble(taskStatistics.get("error").toString());
                // 平均响应时间和
                averageResponseTimeSum += Double.parseDouble(taskStatistics.get("average").toString());
                // 最小响应时间和
                averageMinResponseTimeSum += Double.parseDouble(taskStatistics.get("min").toString());
                // 最大响应时间和
                averageMaxResponseTimeSum += Double.parseDouble(taskStatistics.get("max").toString());
                // 90分位响应时间和
                average90thResponseTimeSum += Double.parseDouble(taskStatistics.get("90th").toString());
                // 95分位响应时间和
                average95thResponseTimeSum += Double.parseDouble(taskStatistics.get("95th").toString());
                // 99分位响应时间和
                average99thResponseTimeSum += Double.parseDouble(taskStatistics.get("99th").toString());
                // 中位数响应时间和
                averageMedianResponseTimeSum += Double.parseDouble(taskStatistics.get("median").toString());
                // 收到的总数据量和
                receivedSum += Double.parseDouble(taskStatistics.get("received").toString());
                // 发送的总数据量和
                sentSum += Double.parseDouble(taskStatistics.get("Sent").toString());
                // 吞吐量 时间 list
                throughputData.add(List.of(createTime, throughputSum/taskDOS.size()));
                Collections.sort(throughputData, Comparator.comparingLong(list -> (long) list.get(0)));
                // 99分位响应时间 list
                responseTime99thData.add(List.of(createTime, average99thResponseTimeSum/taskDOS.size()));
                Collections.sort(responseTime99thData, Comparator.comparingLong(list -> (long) list.get(0)));
                // 90分位响应时间 list
                responseTime90thData.add(List.of(createTime, average90thResponseTimeSum/taskDOS.size()));
                Collections.sort(responseTime90thData, Comparator.comparingLong(list -> (long) list.get(0)));
                // 95分位响应时间 list
                responseTime95thData.add(List.of(createTime, average95thResponseTimeSum/taskDOS.size()));
                Collections.sort(responseTime95thData, Comparator.comparingLong(list -> (long) list.get(0)));
                // 中位数响应时间 list
                responseTimeMedianData.add(List.of(createTime, averageMedianResponseTimeSum/taskDOS.size()));
                Collections.sort(responseTimeMedianData, Comparator.comparingLong(list -> (long) list.get(0)));
                // 平均响应时间 list
                responseTimeAverageData.add(List.of(createTime, averageResponseTimeSum/taskDOS.size()));
                Collections.sort(responseTimeAverageData, Comparator.comparingLong(list -> (long) list.get(0)));
                // 错误率 list
                errorRateData.add(List.of(createTime, errorRateSum/taskDOS.size()));
                Collections.sort(errorRateData, Comparator.comparingLong(list -> (long) list.get(0)));

            }
        }
        statisticsDO.setSamplesSum(samplesSum);
        statisticsDO.setDurationSum(durationSum);
        statisticsDO.setErrorsSum(errorsSum);
        statisticsDO.setAverageThroughput(throughputSum/taskDOS.size());
        statisticsDO.setAverageErrorRate(errorRateSum/taskDOS.size());
        statisticsDO.setAverageResponseTime(averageResponseTimeSum/taskDOS.size());
        statisticsDO.setAverageMinResponseTime(averageMinResponseTimeSum/taskDOS.size());
        statisticsDO.setAverageMaxResponseTime(averageMaxResponseTimeSum/taskDOS.size());
        statisticsDO.setAverage90thResponseTime(average90thResponseTimeSum/taskDOS.size());
        statisticsDO.setAverage95thResponseTime(average95thResponseTimeSum/taskDOS.size());
        statisticsDO.setAverage99thResponseTime(average99thResponseTimeSum/taskDOS.size());
        statisticsDO.setAverageMedianResponseTime(averageMedianResponseTimeSum/taskDOS.size());
        statisticsDO.setAverageReceived(receivedSum/taskDOS.size());
        statisticsDO.setAverageSent(sentSum/taskDOS.size());
        statisticsDO.setThroughputData(throughputData);
        statisticsDO.setResponseTime99thData(responseTime99thData);
        statisticsDO.setResponseTime90thData(responseTime90thData);
        statisticsDO.setResponseTime95thData(responseTime95thData);
        statisticsDO.setResponseTimeMedianData(responseTimeMedianData);
        statisticsDO.setResponseTimeAverageData(responseTimeAverageData);
        statisticsDO.setErrorRateData(errorRateData);
        Map<String, Object> stringObjectMap = this.statisticsGraphData(statisticsDO);
        statisticsDO.setGraphData(stringObjectMap);
        statisticsDO.setUpdateTime(new Date());
        statisticsRepository.save(statisticsDO);
    }

    public Map<String, Object> statisticsGraphData(StatisticsDO statisticsDO) {
        Map<String, Object> graphData = new HashMap<>();
        JSONObject responseTimeInfos = new JSONObject();
        responseTimeInfos.put("titleCN", "响应时间");
        responseTimeInfos.put("yName", "响应时间(ms)");
        responseTimeInfos.put("labels", List.of("平均", "中位数", "90分位", "95分位", "99分位"));
        List<JSONObject> series = new ArrayList<>();
        List<Object> timeList = new ArrayList<>();
        List<Object> averageList = new ArrayList<>();
        List<Object> medianList = new ArrayList<>();
        List<Object> rt90thList = new ArrayList<>();
        List<Object> rt95thList = new ArrayList<>();
        List<Object> rt99thList = new ArrayList<>();
        for (List<Object> item : statisticsDO.getResponseTimeAverageData()) {
            timeList.add(item.get(0));
            averageList.add(item.get(1));
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd HH:mm").withZone(ZoneId.systemDefault());
        timeList.replaceAll(element -> {
            Long timestamp = (Long) element;
            Instant instant = Instant.ofEpochMilli(timestamp);
            return formatter.format(instant);
        });
        responseTimeInfos.put("times", timeList);
        statisticsDO.getResponseTime90thData().forEach(item -> rt90thList.add(item.get(1)));
        statisticsDO.getResponseTime95thData().forEach(item -> rt95thList.add(item.get(1)));
        statisticsDO.getResponseTime99thData().forEach(item -> rt99thList.add(item.get(1)));
        statisticsDO.getResponseTimeMedianData().forEach(item -> medianList.add(item.get(1)));
        JSONObject average = new JSONObject();
        average.put("name", "平均");
        average.put("type", "line");
        average.put("data", averageList);
        series.add(average);
        JSONObject median = new JSONObject();
        median.put("name", "中位数");
        median.put("type", "line");
        median.put("data", medianList);
        series.add(median);
        JSONObject rt90th = new JSONObject();
        rt90th.put("name", "90分位");
        rt90th.put("type", "line");
        rt90th.put("data", rt90thList);
        series.add(rt90th);
        JSONObject rt95th = new JSONObject();
        rt95th.put("name", "95分位");
        rt95th.put("type", "line");
        rt95th.put("data", rt95thList);
        series.add(rt95th);
        JSONObject rt99th = new JSONObject();
        rt99th.put("name", "99分位");
        rt99th.put("type", "line");
        rt99th.put("data", rt99thList);
        series.add(rt99th);
        responseTimeInfos.put("series", series);
        graphData.put("responseTimeInfos", responseTimeInfos);

        JSONObject throughputAndErrorInfos = new JSONObject();
        throughputAndErrorInfos.put("titleCN", "吞吐量和错误率");
        throughputAndErrorInfos.put("yName", "事务数/秒");
        throughputAndErrorInfos.put("labels", List.of("吞吐量", "错误率"));
        throughputAndErrorInfos.put("times", timeList);
        List<JSONObject> series2 = new ArrayList<>();
        JSONObject throughput = new JSONObject();
        List<Object> throughputList = new ArrayList<>();
        statisticsDO.getThroughputData().forEach(item -> throughputList.add(item.get(1)));
        throughput.put("name", "吞吐量");
        throughput.put("type", "line");
        throughput.put("data", throughputList);
        series2.add(throughput);
        JSONObject errorRate = new JSONObject();
        errorRate.put("name", "错误率");
        errorRate.put("type", "line");
        List<Object> errorRateList = new ArrayList<>();
        statisticsDO.getErrorRateData().forEach(item -> errorRateList.add(item.get(1)));
        errorRate.put("data", errorRateList);
        series2.add(errorRate);
        throughputAndErrorInfos.put("series", series2);
        graphData.put("throughputAndErrorInfos", throughputAndErrorInfos);

        return graphData;
    }
}
