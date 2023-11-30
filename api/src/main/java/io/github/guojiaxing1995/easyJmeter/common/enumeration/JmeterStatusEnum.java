package io.github.guojiaxing1995.easyJmeter.common.enumeration;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.github.guojiaxing1995.easyJmeter.common.serializer.JmeterStatusEnumSerializer;
import io.github.guojiaxing1995.easyJmeter.common.util.EnumUtil;
import lombok.Getter;

import java.util.*;

@Getter
@JsonSerialize(using = JmeterStatusEnumSerializer.class)
public enum JmeterStatusEnum implements IEnum<Integer> {

    IDLE(0,"空闲"),

    CONFIGURE(1,"配置"),

    RUN(2,"运行"),

    COLLECT(3,"收集"),

    CLEAN(4,"清理"),

    INTERRUPT(5,"中断");

    @EnumValue
    private final Integer value;

    @JsonValue
    private  final String desc;

    JmeterStatusEnum(Integer value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    @Override
    public Integer getValue() {
        return this.value;
    }

    public static JmeterStatusEnum getEnumByCode(Integer code){
        Optional<JmeterStatusEnum> optional = EnumUtil.getEnumObject(JmeterStatusEnum.class, e -> e.getValue().equals(code));
        return optional.isPresent() ? optional.get() : null;
    }

    public static List<Map<String, Object>> toMapList() {
        List<Map<String, Object>> list = new ArrayList<>();
        for (JmeterStatusEnum e : JmeterStatusEnum.values()) { // 使用 values() 方法得到所有的枚举常量
            Map<String, Object> map = new HashMap<>();
            map.put("value", e.getValue());
            map.put("desc", e.getDesc());
            list.add(map);
        }
        return list;
    }

    // 根据 value 和 desc 创建枚举实例的静态方法
    public static JmeterStatusEnum fromValueAndDesc(int value, String desc) {
        for (JmeterStatusEnum enumValue : JmeterStatusEnum.values()) {
            if (enumValue.value == value && enumValue.desc.equals(desc)) {
                return enumValue;
            }
        }
        // 如果没有匹配的枚举值，你可以根据情况返回默认值或者抛出异常
        throw new IllegalArgumentException("No matching enum value for value: " + value + " and desc: " + desc);
    }
}
