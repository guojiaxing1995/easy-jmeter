package io.github.guojiaxing1995.easyJmeter.model;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@TableName("file")
@EqualsAndHashCode(callSuper = true)
public class JFileDO extends BaseModel implements Serializable {

    private static final long serialVersionUID = -3402162550338356318L;

    private String name;

    private String type;

    private String path;

    private String url;

    private Long size;

    private Boolean cut;

    private Integer originId;

    private String taskId;
}
