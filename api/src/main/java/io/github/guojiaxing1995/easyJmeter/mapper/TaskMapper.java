package io.github.guojiaxing1995.easyJmeter.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.github.guojiaxing1995.easyJmeter.common.mybatis.Page;
import io.github.guojiaxing1995.easyJmeter.model.TaskDO;
import io.github.guojiaxing1995.easyJmeter.vo.HistoryTaskVO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskMapper extends BaseMapper<TaskDO> {

    IPage<HistoryTaskVO> selectHistory(Page page, @Param("taskId") String taskId,
                                       @Param("startTime") String startTime, @Param("endTime") String endTime,
                                       @Param("result") Integer result, @Param("jmeterCase") String jmeterCase);

    Integer selectSumDuration(@Param("taskId") String taskId);

    TaskDO selectByIdIncludeDelete(@Param("id") Integer id);
}
