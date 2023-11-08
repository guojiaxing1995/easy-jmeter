package io.github.guojiaxing1995.easyJmeter.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.guojiaxing1995.easyJmeter.common.enumeration.JmeterStatusEnum;
import io.github.guojiaxing1995.easyJmeter.model.TaskLogDO;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskLogMapper extends BaseMapper<TaskLogDO> {

    List<TaskLogDO> search(String taskId, Integer jCase, JmeterStatusEnum status, String address, Boolean result);

}
