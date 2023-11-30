package io.github.guojiaxing1995.easyJmeter.common.serializer;

import io.github.guojiaxing1995.easyJmeter.common.enumeration.TaskResultEnum;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

public class TaskResultEnumSerializer extends JsonSerializer<TaskResultEnum> {
    @Override
    public void serialize(TaskResultEnum value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();
        gen.writeFieldName("value");
        gen.writeObject(value.getValue());
        gen.writeFieldName("desc");
        gen.writeObject(value.getDesc());
        gen.writeEndObject();
    }
}
