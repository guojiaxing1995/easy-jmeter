package io.github.guojiaxing1995.easyJmeter.service.impl;

import io.github.guojiaxing1995.easyJmeter.service.TaskInfluxdbService;
import org.influxdb.InfluxDB;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

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
}
