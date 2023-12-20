package io.github.guojiaxing1995.easyJmeter.common.jmeter;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import io.github.guojiaxing1995.easyJmeter.model.JFileDO;
import io.github.guojiaxing1995.easyJmeter.model.ReportDO;
import io.github.guojiaxing1995.easyJmeter.model.TaskDO;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class ReportDataProcess {

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
        return map;
    }

    public ReportDO getData(TaskDO taskDO, String reportPath, JFileDO jFileDO) {
        Map<String, List<JSONObject>> dashBoardData = this.getDashBoardData(new File(reportPath, "content/js/dashboard.js").toString());
        Map<String, JSONObject> graphData = this.getGraphData(new File(reportPath, "content/js/graph.js").toString());
        ReportDO reportDO = new ReportDO();
        reportDO.setTaskId(taskDO.getTaskId());
        reportDO.setGraphData(graphData);
        reportDO.setDashBoardData(dashBoardData);
        reportDO.setFile(jFileDO);

        return reportDO;
    }
}
