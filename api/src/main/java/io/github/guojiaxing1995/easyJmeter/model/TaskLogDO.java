package io.github.guojiaxing1995.easyJmeter.model;

import com.baomidou.mybatisplus.annotation.TableName;
import io.github.guojiaxing1995.easyJmeter.common.enumeration.JmeterStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("task_log")
@EqualsAndHashCode(callSuper = true)
public class TaskLogDO  extends BaseModel implements Serializable {

    private static final long serialVersionUID = 177261585529036980L;

    private String taskId;

    private Integer jCase;

    private JmeterStatusEnum status;

    private Boolean result;

    private String address;

    private Integer machine;
}
