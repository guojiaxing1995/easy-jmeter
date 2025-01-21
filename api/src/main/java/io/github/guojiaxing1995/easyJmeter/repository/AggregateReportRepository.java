package io.github.guojiaxing1995.easyJmeter.repository;

import io.github.guojiaxing1995.easyJmeter.model.AggregateReportDO;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AggregateReportRepository extends MongoRepository<AggregateReportDO, String> {

}