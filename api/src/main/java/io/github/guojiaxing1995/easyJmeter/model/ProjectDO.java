package io.github.guojiaxing1995.easyJmeter.model;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;


@Data
@TableName("project")
@EqualsAndHashCode(callSuper = true)
public class ProjectDO extends BaseModel implements Serializable {

    private static final long serialVersionUID = -483068736478817178L;

    private String name;

    private Integer creator;

    private String description;

}
