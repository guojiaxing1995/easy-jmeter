package io.github.guojiaxing1995.easyJmeter.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.guojiaxing1995.easyJmeter.model.XssDO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface XssMapper extends BaseMapper<XssDO> {

    XssDO selectByKey(@Param("k") String k);

}
