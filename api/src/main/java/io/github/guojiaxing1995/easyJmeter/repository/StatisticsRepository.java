package io.github.guojiaxing1995.easyJmeter.repository;

import io.github.guojiaxing1995.easyJmeter.model.StatisticsDO;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface StatisticsRepository extends MongoRepository<StatisticsDO, String> {

    @Aggregation({
            "{ $match: { id: { $not: /manual/ } } }",
            "{ $group: { _id: null, totalSamplesSum: { $sum: '$samplesSum' } } }",
            "{ $project: { _id: 0, totalSamplesSum: 1 } }"
    })
    List<Long> getTotalSamplesSumForNonManual();
}
