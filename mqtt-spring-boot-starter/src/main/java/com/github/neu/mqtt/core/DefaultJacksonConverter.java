package com.github.neu.mqtt.core;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;

/**
 * @author: neeeeeeeeeu
 * @date: Created in 2024/7/3 14:52
 * @description:
 * @modified By:
 * @version:
 */
public class DefaultJacksonConverter implements JacksonConverter {

    @Override
    public ObjectMapper createObjectMapper() {
        return new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }
}
