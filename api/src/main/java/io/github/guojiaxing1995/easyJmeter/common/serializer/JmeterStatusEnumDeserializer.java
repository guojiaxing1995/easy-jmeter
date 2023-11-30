package io.github.guojiaxing1995.easyJmeter.common.serializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import io.github.guojiaxing1995.easyJmeter.common.enumeration.JmeterStatusEnum;

import java.io.IOException;

public class JmeterStatusEnumDeserializer extends StdDeserializer<JmeterStatusEnum> {
    protected JmeterStatusEnumDeserializer(Class<?> vc) {
        super(vc);
    }

    protected JmeterStatusEnumDeserializer() {
        this(null);
    }

    @Override
    public JmeterStatusEnum deserialize(JsonParser parser, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        JsonNode node = parser.getCodec().readTree(parser);

        // 从 JSON 中读取 "value" 和 "desc" 字段的值
        int value = node.get("value").asInt();
        String desc = node.get("desc").asText();

        // 根据获取的值创建 TaskResultEnum 枚举实例
        return JmeterStatusEnum.fromValueAndDesc(value, desc);
    }
}
