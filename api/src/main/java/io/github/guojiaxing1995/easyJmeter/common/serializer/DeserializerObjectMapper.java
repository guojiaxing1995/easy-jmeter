package io.github.guojiaxing1995.easyJmeter.common.serializer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.github.guojiaxing1995.easyJmeter.common.enumeration.JmeterStatusEnum;
import io.github.guojiaxing1995.easyJmeter.common.enumeration.TaskResultEnum;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DeserializerObjectMapper {

    public static ObjectMapper getObjectMapper(){
        ObjectMapper objectMapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(TaskResultEnum.class, new TaskResultEnumDeserializer());
        module.addDeserializer(JmeterStatusEnum.class, new JmeterStatusEnumDeserializer());
        objectMapper.registerModule(module);

        return objectMapper;
    }

    public static <T> T deserialize(String jsonString, Class<T> clazz) {
        try {
            return getObjectMapper().readValue(jsonString, clazz);
        } catch (JsonProcessingException e) {
            log.error("反序列化失败:" + jsonString, e);
            throw new RuntimeException(e);
        }
    }
}
