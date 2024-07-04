package com.github.neu.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.neu.mqtt.core.JacksonConverter;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

/**
 * @author: neeeeeeeeeu
 * @date: Created in 2024/7/4 16:14
 * @description:
 * @modified By:
 * @version:
 */
//@Component
public class TestJackSonConverter implements JacksonConverter {
    @Override
    public ObjectMapper createObjectMapper() {
        return null;
    }
}
