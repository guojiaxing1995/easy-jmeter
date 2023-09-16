package io.github.guojiaxing1995.easyJmeter.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.github.guojiaxing1995.easyJmeter.common.mybatis.Page;
import io.github.guojiaxing1995.easyJmeter.model.MachineDO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;

@Repository
public interface MachineMapper  extends BaseMapper<MachineDO> {

    IPage<MachineDO> selectByName(Page page, @Param("name") String name);

    MachineDO selectByAddress(String address);

    MachineDO selectByClientId(String clientId);

    ArrayList<MachineDO> selectAll();

    ArrayList<MachineDO> executable();
}
