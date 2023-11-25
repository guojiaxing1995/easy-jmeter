package io.github.guojiaxing1995.easyJmeter.common.enumeration;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public static List<Map<String, Object>> toMapList() {
        List<Map<String, Object>> list = new ArrayList<>();
        for (TaskResultEnum e : TaskResultEnum.values()) { // 使用 values() 方法得到所有的枚举常量
            Map<String, Object> map = new HashMap<>();
            map.put("value", e.getValue());
            map.put("desc", e.getDesc());
            list.add(map);
        }
        return list;
    }
}
