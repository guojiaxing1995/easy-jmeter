package io.github.guojiaxing1995.easyJmeter.service;

import io.github.guojiaxing1995.easyJmeter.common.enumeration.JmeterStatusEnum;
import io.github.guojiaxing1995.easyJmeter.dto.jcase.CaseDebugDTO;
import io.github.guojiaxing1995.easyJmeter.dto.jcase.CreateOrUpdateCaseDTO;
import io.github.guojiaxing1995.easyJmeter.model.CaseDO;
import io.github.guojiaxing1995.easyJmeter.vo.CaseInfoPlusVO;
import io.github.guojiaxing1995.easyJmeter.vo.CaseInfoVO;

import java.io.IOException;
import java.util.List;

public interface CaseService {

    boolean createCase(CreateOrUpdateCaseDTO caseDTO);

    boolean updateCase(CaseDO caseDO, CreateOrUpdateCaseDTO caseDTO);

    CaseDO getById(Integer id);

    boolean deleteCase(Integer id);

    List<CaseInfoVO> selectCase(Integer id);

    CaseInfoPlusVO getCaseInfoById(Integer id);

    boolean updateCaseStatus(CaseDO caseDO, JmeterStatusEnum status);

    void debugCase(CaseDebugDTO caseDebugDTO) throws IOException;
}
