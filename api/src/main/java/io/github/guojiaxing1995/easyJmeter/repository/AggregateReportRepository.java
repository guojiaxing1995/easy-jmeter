package io.github.guojiaxing1995.easyJmeter.repository;

import io.github.guojiaxing1995.easyJmeter.model.AggregateReportDO;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface AggregateReportRepository extends MongoRepository<AggregateReportDO, String> {
    @Query("{'projectId': ?0, 'text': { $regex: ?1, $options: 'i' }, 'label': { $regex: ?2, $options: 'i' }, 'deleteTime': { $exists: false }}")
    Optional<List<AggregateReportDO>> getAggregateReportRecord(Integer projectId, String text, String label);
}