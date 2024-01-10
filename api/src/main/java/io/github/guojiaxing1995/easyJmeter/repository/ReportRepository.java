package io.github.guojiaxing1995.easyJmeter.repository;

import io.github.guojiaxing1995.easyJmeter.model.ReportDO;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Optional;

public interface ReportRepository  extends MongoRepository<ReportDO, String> {

    @Query("{'_id': ?0, 'deleteTime': { $exists: false }}")
    Optional<ReportDO> findNonDeletedById(String id);
}
