package io.github.guojiaxing1995.easyJmeter.model;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@TableName("xss")
@EqualsAndHashCode(callSuper = true)
public class XssDO extends BaseModel implements Serializable {

    private static final long serialVersionUID = 3531805912578317266L;

    private String xssKey;

    private String xssValue;
}
