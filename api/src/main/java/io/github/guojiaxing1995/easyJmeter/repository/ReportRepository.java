package io.github.guojiaxing1995.easyJmeter.repository;

import io.github.guojiaxing1995.easyJmeter.model.ReportDO;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ReportRepository  extends MongoRepository<ReportDO, String> {
}
