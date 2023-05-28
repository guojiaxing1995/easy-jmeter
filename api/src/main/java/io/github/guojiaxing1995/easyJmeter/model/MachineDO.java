package io.github.guojiaxing1995.easyJmeter.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.github.guojiaxing1995.easyJmeter.common.enumeration.JmeterStatusEnum;
import io.github.guojiaxing1995.easyJmeter.common.enumeration.MachineOnlineEnum;
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

    @TableField(value = "`online`")
    private MachineOnlineEnum online;

    @TableField(value = "`jmeter_status`")
    private JmeterStatusEnum jmeterStatus;

}
