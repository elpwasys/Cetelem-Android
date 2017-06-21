package br.com.wasys.library.utils;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import java.util.Date;

import br.com.wasys.library.jackson.JsonDateDeserializer;
import br.com.wasys.library.jackson.JsonDateSerializer;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * Created by pascke on 15/08/16.
 */
public class JacksonUtils {

    public static ObjectMapper getObjectMapper() {
        SimpleModule module = new SimpleModule();
        module.addSerializer(Date.class, new JsonDateSerializer());
        module.addDeserializer(Date.class, new JsonDateDeserializer());
        ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(module)
                .setSerializationInclusion(Include.NON_NULL)
                .setSerializationInclusion(Include.NON_EMPTY)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return objectMapper;
    }
}
