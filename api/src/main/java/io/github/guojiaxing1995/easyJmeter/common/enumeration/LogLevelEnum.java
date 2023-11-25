package io.github.guojiaxing1995.easyJmeter.common.enumeration;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonValue;
import io.github.guojiaxing1995.easyJmeter.common.util.EnumUtil;
import lombok.Getter;

import java.util.*;

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

    public static LogLevelEnum getEnumByCode(Integer code){
        Optional<LogLevelEnum> optional = EnumUtil.getEnumObject(LogLevelEnum.class, e -> e.getValue().equals(code));
        return optional.isPresent() ? optional.get() : null;
    }

    public static List<Map<String, Object>> toMapList() {
        List<Map<String, Object>> list = new ArrayList<>();
        for (LogLevelEnum e : LogLevelEnum.values()) { // 使用 values() 方法得到所有的枚举常量
            Map<String, Object> map = new HashMap<>();
            map.put("value", e.getValue());
            map.put("desc", e.getDesc());
            list.add(map);
        }
        return list;
    }
}
