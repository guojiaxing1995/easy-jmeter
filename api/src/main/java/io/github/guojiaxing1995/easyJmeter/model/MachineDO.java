package io.github.guojiaxing1995.easyJmeter.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.github.guojiaxing1995.easyJmeter.common.enumeration.JmeterStatusEnum;
import lombok.*;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "machine")
@EqualsAndHashCode(callSuper = true)
public class MachineDO extends BaseModel implements Serializable {

    private static final long serialVersionUID = -6928506417680888594L;

    private String name;

    private String address;

    private String path;

    private String version;

    private Boolean isOnline;

    @TableField(value = "`jmeter_status`")
    private JmeterStatusEnum jmeterStatus;

    @JsonIgnore
    @TableField(value = "`client_id`")
    private String clientId;

}
