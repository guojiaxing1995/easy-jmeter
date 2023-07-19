package io.github.guojiaxing1995.easyJmeter.service;

import io.github.guojiaxing1995.easyJmeter.dto.jcase.CreateOrUpdateCaseDTO;

public interface CaseService {

    boolean createCase(CreateOrUpdateCaseDTO caseDTO);
}
