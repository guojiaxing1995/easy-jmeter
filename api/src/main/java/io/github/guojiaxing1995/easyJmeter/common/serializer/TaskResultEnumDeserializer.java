package io.github.guojiaxing1995.easyJmeter.common.serializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import io.github.guojiaxing1995.easyJmeter.common.enumeration.TaskResultEnum;

import java.io.IOException;

public class TaskResultEnumDeserializer extends StdDeserializer<TaskResultEnum> {

    public TaskResultEnumDeserializer() {
        this(null);
    }

    public TaskResultEnumDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public TaskResultEnum deserialize(JsonParser parser, DeserializationContext ctxt) throws IOException {
        JsonNode node = parser.getCodec().readTree(parser);

        // 从 JSON 中读取 "value" 和 "desc" 字段的值
        int value = node.get("value").asInt();
        String desc = node.get("desc").asText();

        // 根据获取的值创建 TaskResultEnum 枚举实例
        return TaskResultEnum.fromValueAndDesc(value, desc);
    }
}