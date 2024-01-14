package io.github.guojiaxing1995.easyJmeter.repository;

import com.alibaba.fastjson2.JSONObject;
import io.github.guojiaxing1995.easyJmeter.model.ReportDO;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ReportRepository  extends MongoRepository<ReportDO, String> {

    @Query("{'_id': ?0, 'deleteTime': { $exists: false }}")
    Optional<ReportDO> findNonDeletedById(String id);

    @Aggregation({
            "{ $match: { 'result': 'SUCCESS', '_id': ?0, 'deleteTime': { $exists: false } } }",
            "{ $unwind: '$dashBoardData.statisticsTable' }",
            "{ $match: { 'dashBoardData.statisticsTable.label': 'Total' } }",
            "{ $group: { _id: null, totalSamples: { $first: ?1 } } }",
            "{ $project: { _id: 0, totalSamples: 1 } }"
    })
    List<Object> getTaskStatisticsTableParam(String taskId, String param);

    @Aggregation({
            "{ $match: { 'result': 'SUCCESS', '_id': ?0, 'deleteTime': { $exists: false } } }",
            "{ $unwind: '$dashBoardData.statisticsTable' }",
            "{ $match: { 'dashBoardData.statisticsTable.label': 'Total' } }",
            "{ $group: { _id: null, totalJson: { $first: '$dashBoardData.statisticsTable' } } }",
            "{ $project: { _id: 0, totalJson: 1 } }"
    })
    List<JSONObject> getTaskStatisticsTable(String taskId);

}
