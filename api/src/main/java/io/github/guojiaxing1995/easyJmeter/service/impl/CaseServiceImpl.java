package io.github.guojiaxing1995.easyJmeter.service.impl;

import io.github.guojiaxing1995.easyJmeter.common.LocalUser;
import io.github.guojiaxing1995.easyJmeter.common.enumeration.JmeterStatusEnum;
import io.github.guojiaxing1995.easyJmeter.dto.jcase.CreateOrUpdateCaseDTO;
import io.github.guojiaxing1995.easyJmeter.mapper.CaseMapper;
import io.github.guojiaxing1995.easyJmeter.model.CaseDO;
import io.github.guojiaxing1995.easyJmeter.service.CaseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CaseServiceImpl implements CaseService {

    @Autowired
    private CaseMapper caseMapper;

    @Override
    public boolean createCase(CreateOrUpdateCaseDTO caseDTO) {
        CaseDO caseDO = new CaseDO();
        caseDO.setName(caseDTO.getName());
        caseDO.setCreator(LocalUser.getLocalUser().getId());
        caseDO.setDescription(caseDTO.getDescription());
        caseDO.setJmx(caseDTO.getJmx());
        caseDO.setCsv(caseDTO.getCsv());
        caseDO.setJar(caseDTO.getJar());
        caseDO.setProject(caseDTO.getProject());
        caseDO.setStatus(JmeterStatusEnum.IDLE);
        return caseMapper.insert(caseDO) > 0;
    }
}
