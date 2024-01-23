package io.github.guojiaxing1995.easyJmeter.common.enumeration;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum DebugTypeEnum implements IEnum<Integer> {
    ERROR(0, "ERROR"),
    CONFIG(1, "CONFIG"),
    SAMPLE(2,"SAMPLE"),
    LOG(3,"LOG");


    @EnumValue
    private final Integer value;

    @JsonValue
    private  final String desc;

    DebugTypeEnum(Integer value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    @Override
    public Integer getValue() {
        return null;
    }
}
