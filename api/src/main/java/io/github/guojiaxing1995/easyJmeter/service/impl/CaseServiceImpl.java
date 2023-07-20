package io.github.guojiaxing1995.easyJmeter.service.impl;

import io.github.guojiaxing1995.easyJmeter.common.LocalUser;
import io.github.guojiaxing1995.easyJmeter.common.enumeration.JmeterStatusEnum;
import io.github.guojiaxing1995.easyJmeter.dto.jcase.CreateOrUpdateCaseDTO;
import io.github.guojiaxing1995.easyJmeter.mapper.CaseMapper;
import io.github.guojiaxing1995.easyJmeter.model.CaseDO;
import io.github.guojiaxing1995.easyJmeter.service.CaseService;
import io.github.guojiaxing1995.easyJmeter.vo.CaseInfoVO;
import io.github.talelin.autoconfigure.exception.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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

    @Override
    public boolean updateCase(CaseDO caseDO, CreateOrUpdateCaseDTO caseDTO) {
        if (caseDO == null){
            throw new NotFoundException(12201);
        }
        caseDO.setName(caseDTO.getName());
        caseDO.setDescription(caseDTO.getDescription());
        caseDO.setJmx(caseDTO.getJmx());
        caseDO.setCsv(caseDTO.getCsv());
        caseDO.setJar(caseDTO.getJar());
        caseDO.setProject(caseDTO.getProject());
        return caseMapper.updateById(caseDO) > 0;
    }

    public CaseDO getById(Integer id) {
        return caseMapper.selectById(id);
    }

    @Override
    public boolean deleteCase(Integer id) {
        CaseDO caseDO = caseMapper.selectById(id);
        if (caseDO == null){
            throw new NotFoundException(12201);
        }
        return caseMapper.deleteById(id) > 0;
    }

    @Override
    public List<CaseInfoVO> getAll() {
        return caseMapper.selectAll();
    }
}
