package io.github.guojiaxing1995.easyJmeter.common.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import io.github.guojiaxing1995.easyJmeter.common.enumeration.JmeterStatusEnum;

import java.io.IOException;

public class JmeterStatusEnumSerializer extends JsonSerializer<JmeterStatusEnum> {
    @Override
    public void serialize(JmeterStatusEnum value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();
        gen.writeFieldName("value");
        gen.writeObject(value.getValue());
        gen.writeFieldName("desc");
        gen.writeObject(value.getDesc());
        gen.writeEndObject();
    }
}
