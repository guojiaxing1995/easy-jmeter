package io.github.guojiaxing1995.easyJmeter.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.github.guojiaxing1995.easyJmeter.common.mybatis.Page;
import io.github.guojiaxing1995.easyJmeter.model.ProjectDO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectMapper extends BaseMapper<ProjectDO> {

    IPage<ProjectDO> selectByName(Page page, @Param("name") String name);

    List<ProjectDO> getAll();
}
