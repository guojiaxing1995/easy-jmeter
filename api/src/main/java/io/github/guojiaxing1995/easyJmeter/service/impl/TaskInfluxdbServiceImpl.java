package io.github.guojiaxing1995.easyJmeter.service.impl;

import io.github.guojiaxing1995.easyJmeter.dto.task.JmeterParamDTO;
import io.github.guojiaxing1995.easyJmeter.service.TaskInfluxdbService;
import lombok.extern.slf4j.Slf4j;
import org.influxdb.InfluxDB;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TaskInfluxdbServiceImpl implements TaskInfluxdbService {

    @Autowired
    private final InfluxDB influxDB;

    public TaskInfluxdbServiceImpl(InfluxDB influxDB) {
        this.influxDB = influxDB;
    }

    @Override
    public Map<String, Object> getTimes(String taskId) {
        String query = String.format("select text from events where application='%s' tz('Asia/Shanghai')", taskId);
        QueryResult.Result result = influxDB.query(new Query(query)).getResults().get(0);
        if (result.getSeries()==null){
            return Map.of("startTime", "", "endTime", "", "start", "", "end", "");
        }
        List<List<Object>> values = result.getSeries().get(0).getValues();
        List<String> startTimes = new ArrayList<>();
        List<String> endTimes = new ArrayList<>();
        for (List<Object> value : values) {
            if (value.get(1).toString().contains("started")){
                startTimes.add(value.get(0).toString());
            }
            if (value.get(1).toString().contains("ended")){
                endTimes.add(value.get(0).toString());
            }
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        DateTimeFormatter newFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        // 开始时间升序排列
        List<OffsetDateTime> sortedStartTimes = startTimes.stream()
                .map(timeString -> OffsetDateTime.parse(timeString, formatter))
                .sorted()
                .collect(Collectors.toList());
        String startTime = sortedStartTimes.get(0).format(formatter);
        // 结束时间降序排列
        List<OffsetDateTime> sortedEndTimes = endTimes.stream()
                .map(timeString -> OffsetDateTime.parse(timeString, formatter))
                .sorted(Comparator.reverseOrder())
                .collect(Collectors.toList());
        String endTime = "";
        if (!sortedEndTimes.isEmpty()){
            endTime = sortedEndTimes.get(0).format(formatter);
        } else {
            ZonedDateTime zonedDateTime = LocalDateTime.now(ZoneId.systemDefault()).atZone(ZoneId.of("Asia/Shanghai"));
            endTime =  zonedDateTime.format(formatter);
        }

        List<OffsetDateTime> points = new ArrayList<>();
        OffsetDateTime currentPoint = OffsetDateTime.parse(startTime);
        while (currentPoint.isBefore(OffsetDateTime.parse(endTime))) {
            points.add(currentPoint);
            currentPoint = currentPoint.plusSeconds(5 - currentPoint.getSecond() % 5);
        }
        points.add(OffsetDateTime.parse(endTime));
        points.remove(0);

        String start = OffsetDateTime.parse(startTime, formatter).format(newFormatter);
        String end = OffsetDateTime.parse(endTime, formatter).format(newFormatter);

        return Map.of("startTime", startTime, "endTime", endTime, "points", points, "start", start, "end", end);
    }

    @Override
    public Map<String, Object> sampleCounts(String taskId, String startTime, String endTime) {
        if (startTime.isEmpty() || endTime.isEmpty()){
            return Map.of("count", 0, "countError", 0);
        }
        String errorCountQuery = String.format("SELECT count FROM jmeter WHERE statut='ko' and time >= '%s' AND time <= '%s' and application = '%s' and transaction!='all' group by transaction tz('Asia/Shanghai')",
                startTime, endTime, taskId);
        String countQuery = String.format("SELECT count FROM jmeter WHERE statut='all' and time >= '%s' AND time <= '%s' and application = '%s' and transaction!='all' group by transaction tz('Asia/Shanghai')",
                startTime, endTime, taskId);
        QueryResult.Result errorCountResult = influxDB.query(new Query(errorCountQuery)).getResults().get(0);
        QueryResult.Result countResult = influxDB.query(new Query(countQuery)).getResults().get(0);

        Map<String, Double> errorCountMap = new HashMap<>();
        if (errorCountResult.getSeries()!=null){
            List<QueryResult.Series> errorSeries = errorCountResult.getSeries();
            for (QueryResult.Series series : errorSeries) {
                String transaction = series.getTags().get("transaction");
                List<List<Object>> values = series.getValues();
                double errorCount = 0;
                for (List<Object> value : values) {
                    errorCount += (double) value.get(1);
                }
                errorCountMap.put(transaction, errorCount);
            }
        }

        Map<String, Double> countMap = new HashMap<>();
        if (countResult.getSeries()!=null){
            List<QueryResult.Series> series = countResult.getSeries();
            for (QueryResult.Series serie : series) {
                String transaction = serie.getTags().get("transaction");
                List<List<Object>> values = serie.getValues();
                double count = 0;
                for (List<Object> value : values) {
                    count += (double) value.get(1);
                }
                countMap.put(transaction, count);
            }
        }

        String query = String.format("SELECT count,countError FROM jmeter WHERE statut='all' and time >= '%s' AND time <= '%s' and application = '%s' and transaction='all'",
                startTime, endTime, taskId);
        QueryResult.Result result = influxDB.query(new Query(query)).getResults().get(0);
        if (result.getSeries()!=null){
            List<List<Object>> values = result.getSeries().get(0).getValues();
            double count = 0;
            double countError = 0;
            for (List<Object> value : values) {
                count += (double) value.get(1);
                countError += (double) value.get(2);
            }
            countMap.put("all", count);
            errorCountMap.put("all", countError);
        }

        return Map.of("count", countMap, "countError", errorCountMap);
    }

    @Override
    public Map<String, Object> throughputGraph(String taskId, String startTime, String endTime, List<OffsetDateTime> points) {
        if (startTime.isEmpty() || endTime.isEmpty()){
            return Map.of();
        }
        String query = String.format("SELECT count FROM jmeter WHERE statut='all' and time >= '%s' AND time <= '%s' and application = '%s' group by transaction tz('Asia/Shanghai')",
                startTime, endTime, taskId);
        QueryResult.Result result = influxDB.query(new Query(query)).getResults().get(0);
        Map<String, Object> map = new HashMap<>();
        if (result.getSeries()!=null){
            List<QueryResult.Series> series = result.getSeries();
            for (QueryResult.Series seriesItem : series) {
                List<Object> data = new ArrayList<>();
                String transaction = seriesItem.getTags().get("transaction");
                List<List<Object>> values = seriesItem.getValues();
                for (OffsetDateTime point : points){
                    OffsetDateTime lastPoint = point.minusSeconds(5);
                    double count = 0;
                    for (List<Object> value : values) {
                        OffsetDateTime valuePoint = OffsetDateTime.parse(value.get(0).toString());
                        if (valuePoint.isAfter(lastPoint) && valuePoint.isBefore(point)) {
                            count += (double)value.get(1);
                        }
                    }
                    Duration duration = Duration.between(lastPoint.toInstant(), point.toInstant());
                    double tps = count / duration.getSeconds();
                    data.add(List.of(point,tps));
                }
                map.put(transaction, data);
            }
        }
        return map;
    }

    @Override
    public Map<String, Object> errorGraph(String taskId, String startTime, String endTime, List<OffsetDateTime> points) {
        if (startTime.isEmpty() || endTime.isEmpty()){
            return Map.of();
        }
        String query = String.format("SELECT count FROM jmeter WHERE statut='ko' and time >= '%s' AND time <= '%s' and application = '%s' and transaction!='all' group by transaction tz('Asia/Shanghai')",
                startTime, endTime, taskId);
        QueryResult.Result result = influxDB.query(new Query(query)).getResults().get(0);
        Map<String, Object> map = new HashMap<>();
        if (result.getSeries()!=null){
            List<QueryResult.Series> series = result.getSeries();
            for (QueryResult.Series seriesItem : series) {
                List<Object> data = new ArrayList<>();
                String transaction = seriesItem.getTags().get("transaction");
                List<List<Object>> values = seriesItem.getValues();
                for (OffsetDateTime point : points){
                    OffsetDateTime lastPoint = point.minusSeconds(5);
                    double count = 0;
                    for (List<Object> value : values) {
                        OffsetDateTime valuePoint = OffsetDateTime.parse(value.get(0).toString());
                        if (valuePoint.isAfter(lastPoint) && valuePoint.isBefore(point)) {
                            count += (double)value.get(1);
                        }
                    }
                    data.add(List.of(point,count));
                }
                map.put(transaction, data);
            }
        }

        String queryAll = String.format("SELECT countError FROM jmeter WHERE statut='all' and time >= '%s' AND time <= '%s' and application = '%s' and transaction='all'",
                startTime, endTime, taskId);
        QueryResult.Result resultAll = influxDB.query(new Query(queryAll)).getResults().get(0);
        if (resultAll.getSeries()!=null){
            List<QueryResult.Series> series = resultAll.getSeries();
            for (QueryResult.Series seriesItem : series) {
                List<Object> data = new ArrayList<>();
                List<List<Object>> values = seriesItem.getValues();
                for (OffsetDateTime point : points){
                    OffsetDateTime lastPoint = point.minusSeconds(5);
                    double count = 0;
                    for (List<Object> value : values) {
                        OffsetDateTime valuePoint = OffsetDateTime.parse(value.get(0).toString());
                        if (valuePoint.isAfter(lastPoint) && valuePoint.isBefore(point)) {
                            count += (double)value.get(1);
                        }
                    }
                    data.add(List.of(point,count));
                }
                map.put("all", data);
            }
        }
        return map;
    }

    @Override
    public Map<String, Object> errorInfo(String taskId, String startTime, String endTime) {
        if (startTime.isEmpty() || endTime.isEmpty()){
            return Map.of();
        }
        Map<String, Object> map = new HashMap<>();
        String queryCount = String.format("SELECT sum(count) FROM jmeter WHERE statut='' and time >= '%s' AND time <= '%s' and application = '%s' and transaction!='internal' group by transaction tz('Asia/Shanghai')",
                startTime, endTime, taskId);
        QueryResult.Result resultCount = influxDB.query(new Query(queryCount)).getResults().get(0);
        if (resultCount.getSeries()!=null){
            List<QueryResult.Series> series = resultCount.getSeries();
            List<Object> count = new ArrayList<>();
            for (QueryResult.Series seriesItem : series) {
                String transaction = seriesItem.getTags().get("transaction");
                List<List<Object>> values = seriesItem.getValues();
                count.add(Map.of("transaction",transaction,"sum",(long)(double)(values.get(0).get(1))));
            }
            map.put("count", count);
        }
        String query = String.format("SELECT count,responseCode,responseMessage FROM jmeter WHERE statut='' and time >= '%s' AND time <= '%s' and application = '%s' and transaction!='internal' group by transaction tz('Asia/Shanghai')",
                startTime, endTime, taskId);
        QueryResult.Result result = influxDB.query(new Query(query)).getResults().get(0);
        if (result.getSeries()!=null){
            List<QueryResult.Series> series = result.getSeries();
            List<Object> transactionList = new ArrayList<>();
            for (QueryResult.Series seriesItem : series) {
                String t = seriesItem.getTags().get("transaction");
                List<List<Object>> values = seriesItem.getValues();
                Map<String, Long> groupedCounts = values.stream()
                        .collect(Collectors.groupingBy(
                                transaction -> transaction.get(2) + " - " + transaction.get(3), // 分组键由第3（responseCode）和第4（responseMessage）个元素组成
                                Collectors.reducing(0L, transaction -> ((Number) transaction.get(1)).longValue(), Long::sum) // 求count字段的和，它位于第2个位置
                        ));
                List<Object> infoResult = new ArrayList<>();
                for (Map.Entry<String, Long> entry : groupedCounts.entrySet()) {
                    infoResult.add(Map.of("responseCode",entry.getKey().split(" - ")[0],"responseMessage",entry.getKey().split(" - ")[1],"count",entry.getValue()));
                }
                transactionList.add(Map.of("transaction",t,"count",infoResult));
            }
            map.put("transaction",transactionList);
        }

        return map;
    }
    @Override
    public List<JmeterParamDTO> getEvents(JmeterParamDTO jmeterParamDTO) {
        String startTime = jmeterParamDTO.getStartTime();
        String endTime = jmeterParamDTO.getEndTime();
        String application = jmeterParamDTO.getApplication();
        String tags = jmeterParamDTO.getTags();
        String text = jmeterParamDTO.getText();
        if (startTime==null || endTime==null || startTime.isEmpty() || endTime.isEmpty()){
            return List.of();
        }
        String queryEvents = "SELECT time,application,tags,text FROM events WHERE time >= '%s' AND time <= '%s'";
        List<String> params = new ArrayList<>();
        params.add(startTime);
        params.add(endTime);

        if (application != null && !application.isEmpty()) {
            queryEvents += " AND application =~ /%s/";
            params.add(application);
        }

        if (tags != null && !tags.isEmpty()) {
            queryEvents += " AND tags =~ /%s/";
            params.add(tags);
        }

        if (text != null && !text.isEmpty()) {
            queryEvents += " AND text =~ /%s/";
            params.add(text);
        }

        queryEvents += " tz('Asia/Shanghai')";
        queryEvents = String.format(queryEvents, params.toArray());
        log.info(queryEvents);
        List<QueryResult.Result> results = influxDB.query(new Query(queryEvents)).getResults();
        List<Map<String, Object>> eventsList = new ArrayList<>();
        for (QueryResult.Result result : results) {
            if (result.getSeries() != null) {
                for (QueryResult.Series series : result.getSeries()) {
                    List<List<Object>> values = series.getValues();
                    List<String> columns = series.getColumns();

                    for (List<Object> value : values) {
                        Map<String, Object> row = new HashMap<>();
                        for (int i = 0; i < columns.size(); i++) {
                            row.put(columns.get(i), value.get(i));
                        }
                        eventsList.add(row);
                    }
                }
            }
        }

        // 进行数据处理
        List<JmeterParamDTO> resultsList = new ArrayList<>();
        List<Map<String, Object>> toRemove = new ArrayList<>();

        for (int i = 0; i < eventsList.size(); i++) {
            Map<String, Object> map1 = eventsList.get(i);
            if (map1.get("text").toString().endsWith("started") &&!toRemove.contains(map1)) {
                String applicationEvent = map1.get("application").toString();
                String tagsEvent = map1.get("tags").toString();
                long minTimeDiff = Long.MAX_VALUE;
                Map<String, Object> closestEndedMap = null;
                for (int j = 0; j < eventsList.size(); j++) {
                    Map<String, Object> map2 = eventsList.get(j);
                    if (j == i) continue;
                    if (map2.get("text").toString().endsWith("ended") &&
                            map2.get("application").toString().equals(applicationEvent) &&
                            map2.get("tags").toString().equals(tagsEvent) &&
                            !toRemove.contains(map2)) {
                        Instant time1 = OffsetDateTime.parse(map1.get("time").toString()).toInstant();
                        Instant time2 = OffsetDateTime.parse(map2.get("time").toString()).toInstant();
                        long timeDiff = Duration.between(time1, time2).toMillis();
                        if (timeDiff < minTimeDiff) {
                            minTimeDiff = timeDiff;
                            closestEndedMap = map2;
                        }
                    }
                }
                if (closestEndedMap!= null) {
                    JmeterParamDTO jmeterParam = new JmeterParamDTO();
                    jmeterParam.setApplication(applicationEvent);
                    jmeterParam.setStartTime(map1.get("time").toString());
                    jmeterParam.setEndTime(closestEndedMap.get("time").toString());
                    jmeterParam.setTags(tagsEvent);
                    jmeterParam.setText(map1.get("text").toString().split(" ")[0]);
                    resultsList.add(0, jmeterParam);
                    toRemove.add(map1);
                    toRemove.add(closestEndedMap);
                }
            }
        }


        return resultsList;
    }

    @Override
    public List<Map<String, Object>> getAggregateReport(List<JmeterParamDTO> jmeterParamDTOList) {
        List<Map<String,Object>> aggregateReportList = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        for (JmeterParamDTO jmeterParamDTO : jmeterParamDTOList) {
            List<Map<String,Object>> query1List = new ArrayList<>();
            String application = jmeterParamDTO.getApplication();
            String tags = jmeterParamDTO.getTags();
            String text = jmeterParamDTO.getText();
            String startTime = jmeterParamDTO.getStartTime();
            String endTime = ZonedDateTime.parse(jmeterParamDTO.getEndTime(), formatter).plus(Duration.ofMillis(200)).format(formatter);
            Duration duration = Duration.between(ZonedDateTime.parse(startTime, formatter), ZonedDateTime.parse(endTime, formatter));
            long time = duration.getSeconds();

            String query1 = String.format("SELECT sum(count) as sample,count(count),mean(avg) as avg,MEDIAN(avg),sum(countError) as error ,max(max),min(min),sum(rb)/"
                            +time+ " as rb,sum(sb)/"+time+" as sb ,sum(hit)/"+time+" as tps FROM jmeter WHERE statut='all' and time >= '%s' AND time <= '%s' and application = '%s' and transaction!='internal' group by transaction ORDER BY time DESC tz('Asia/Shanghai')",
                    startTime, endTime, application);
            List<QueryResult.Result> results1 = influxDB.query(new Query(query1)).getResults();
            for (QueryResult.Result result : results1) {
                if (result.getSeries() != null) {
                    for (QueryResult.Series series : result.getSeries()) {
                        List<List<Object>> values = series.getValues();
                        List<String> columns = series.getColumns();
                        String transaction = series.getTags().get("transaction");

                        for (List<Object> value : values) {
                            Map<String, Object> row = new HashMap<>();
                            row.put("transaction", transaction);
                            row.put("application", application);
                            row.put("tags", tags);
                            row.put("startTime", startTime);
                            row.put("endTime", endTime);
                            row.put("text", text);
                            row.put("pct90.0", 0.0f);
                            row.put("pct95.0", 0.0f);
                            row.put("pct99.0", 0.0f);
                            for (int i = 0; i < columns.size(); i++) {
                                row.put(columns.get(i), value.get(i));
                            }
                            row.remove("time");
                            query1List.add(row);
                        }
                    }
                }
            }
            String query2 = String.format("SELECT sum(count) as error FROM jmeter WHERE statut='ko' and time >= '%s' AND time <= '%s' and application = '%s' and transaction!='internal' group by transaction tz('Asia/Shanghai')",
                    startTime, endTime, application);
            List<QueryResult.Result> results2 = influxDB.query(new Query(query2)).getResults();
            for (QueryResult.Result result : results2) {
                if (result.getSeries() != null) {
                    for (QueryResult.Series series : result.getSeries()) {
                        List<List<Object>> values = series.getValues();
                        String transaction = series.getTags().get("transaction");
                        for (List<Object> value : values) {
                            for (Map<String, Object> row: query1List) {
                                if (row.get("transaction").equals(transaction)) {
                                    row.put("error", value.get(1));
                                }
                            }
                        }
                    }
                }
            }
            String query3 = String.format("SELECT * FROM jmeter WHERE statut='all' and time >= '%s' AND time <= '%s' and application = '%s' and transaction!='internal' tz('Asia/Shanghai')",
                    startTime, endTime, application);
            List<Map<String,Object>> pctList = new ArrayList<>();
            List<QueryResult.Result> results3 = influxDB.query(new Query(query3)).getResults();
            for (QueryResult.Result result : results3) {
                if (result.getSeries() != null) {
                    for (QueryResult.Series series : result.getSeries()) {
                        List<List<Object>> values = series.getValues();
                        List<String> columns = series.getColumns();

                        for (List<Object> value : values) {
                            Map<String, Object> row = new HashMap<>();
                            for (int i = 0; i < columns.size(); i++) {
                                row.put(columns.get(i), value.get(i));
                            }
                            pctList.add(row);
                        }
                    }
                }
            }
            for (Map<String, Object> value : pctList) {
                String transaction = value.get("transaction").toString();
                for (Map<String, Object> row: query1List) {
                    float pct90 = (float) row.get("pct90.0");
                    float pct95 = (float) row.get("pct95.0");
                    float pct99 = (float) row.get("pct99.0");
                    if (row.get("transaction").equals(transaction)) {
                        row.put("pct90.0", Float.parseFloat(value.get("pct90.0").toString())+pct90);
                        row.put("pct95.0", Float.parseFloat(value.get("pct95.0").toString())+pct95);
                        row.put("pct99.0", Float.parseFloat(value.get("pct99.0").toString())+pct99);
                    }
                }
            }
            for (Map<String, Object> row : query1List) {
                if (row.get("pct90.0") != null) {
                    row.put("pct90.0", Float.parseFloat(row.get("pct90.0").toString())/Double.parseDouble(row.get("count").toString()));
                }
                if (row.get("pct95.0") != null) {
                    row.put("pct95.0", Float.parseFloat(row.get("pct95.0").toString())/Double.parseDouble(row.get("count").toString()));
                }
                if (row.get("pct99.0") != null) {
                    row.put("pct99.0", Float.parseFloat(row.get("pct99.0").toString())/Double.parseDouble(row.get("count").toString()));
                }
            }
            aggregateReportList.addAll(query1List);
        }
        return aggregateReportList;
    }
}
