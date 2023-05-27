package io.github.guojiaxing1995.easyJmeter.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.github.guojiaxing1995.easyJmeter.model.ProjectDO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProjectInfoVO {

    private Integer id;

    private String name;

    private String creator;

    private String description;

    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    public ProjectInfoVO(ProjectDO projectDO, String username){
        BeanUtils.copyProperties(projectDO, this);
        this.creator = username;
    }
}
