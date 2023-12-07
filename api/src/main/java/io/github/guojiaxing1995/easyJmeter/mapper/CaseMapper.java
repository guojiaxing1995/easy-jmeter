package io.github.guojiaxing1995.easyJmeter.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.guojiaxing1995.easyJmeter.model.CaseDO;
import io.github.guojiaxing1995.easyJmeter.vo.CaseInfoVO;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CaseMapper extends BaseMapper<CaseDO> {
    List<CaseInfoVO> select(Integer id);

    List<CaseDO> selectByProject(Integer project);
}
