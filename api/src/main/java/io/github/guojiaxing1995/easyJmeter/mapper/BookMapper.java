package io.github.guojiaxing1995.easyJmeter.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.guojiaxing1995.easyJmeter.model.BookDO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author pedro@TaleLin
 */
@Repository
public interface BookMapper extends BaseMapper<BookDO> {

    List<BookDO> selectByTitleLikeKeyword(@Param("q") String q);

    List<BookDO> selectByTitle(@Param("title") String title);

//    @Select("SELECT * FROM book WHERE title=${q}")
    List<BookDO> selectByTitleKey(String q);
}
