package io.github.guojiaxing1995.easyJmeter.common.enumeration;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum MachineOnlineEnum implements IEnum<Integer> {

    ONLINE(1, "在线"),

    OFFLINE(2, "离线");

    @EnumValue
    private final Integer value;

    @JsonValue
    private final String desc;

    MachineOnlineEnum(Integer value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    @Override
    public Integer getValue() {
        return this.value;
    }
}
