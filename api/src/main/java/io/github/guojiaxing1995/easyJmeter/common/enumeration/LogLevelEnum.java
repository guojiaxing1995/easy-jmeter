package io.github.guojiaxing1995.easyJmeter.common.enumeration;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum LogLevelEnum implements IEnum<Integer> {
    OFF(1, "OFF"),
    ERROR(2,"ERROR"),
    WARN(3,"WARN"),
    INFO(4, "INFO"),
    DEBUG(5, "DEBUG");

    @EnumValue
    private final Integer value;

    @JsonValue
    private  final String desc;

    LogLevelEnum(Integer value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    @Override
    public Integer getValue()  {
        return this.value;
    }
}
