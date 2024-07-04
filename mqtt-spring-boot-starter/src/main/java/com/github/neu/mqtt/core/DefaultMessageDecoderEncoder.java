package com.github.neu.mqtt.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * @author: neeeeeeeeeu
 * @date: Created in 2024/7/3 15:52
 * @description:
 * @modified By:
 * @version:
 */
public class DefaultMessageDecoderEncoder implements MessageDecoderEncoder {

    private final ObjectMapper objectMapper;

    public DefaultMessageDecoderEncoder(ObjectMapper  objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public <T extends Object> byte[] convertEncoder(T data) {
        if (data instanceof byte[]) {
            return (byte[]) data;
        } else if (data instanceof String) {
            return ((String) data).getBytes(StandardCharsets.UTF_8);
        } else {
            try {
                return objectMapper.writeValueAsString(data).getBytes(StandardCharsets.UTF_8);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public <T extends Object> T convertDecoder(byte[] data, Class<T> clazz) {
        if (clazz.equals(String.class)) {
            return (T) new String(data, StandardCharsets.UTF_8);
        } else if (clazz.equals(byte[].class)) {
            return (T) data;
        } else {
            String content = null;
            try {
                content = new String(data, StandardCharsets.UTF_8);
                return objectMapper.readValue(content, clazz);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("mqtt订阅消息反序列化失败,请使用其他类型接收该消息,message:" + content, e);
            }
        }
    }
}
