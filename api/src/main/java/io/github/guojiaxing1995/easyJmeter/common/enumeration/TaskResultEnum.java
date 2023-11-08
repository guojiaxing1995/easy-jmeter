package io.github.guojiaxing1995.easyJmeter.common.enumeration;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum TaskResultEnum implements IEnum<Integer> {
    IN_PROGRESS(0, "进行中"),
    SUCCESS(1, "成功"),
    EXCEPTION(2,"异常终止"),
    MANUAL(3, "手动终止");

    @EnumValue
    private final Integer value;

    @JsonValue
    private  final String desc;

    TaskResultEnum(Integer value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    @Override
    public Integer getValue() {
        return this.value;
    }
}
