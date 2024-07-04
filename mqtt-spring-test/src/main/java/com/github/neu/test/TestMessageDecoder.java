package com.github.neu.test;

import com.github.neu.mqtt.core.MessageDecoderEncoder;
import org.springframework.stereotype.Component;

/**
 * @author: neeeeeeeeeu
 * @date: Created in 2024/7/4 16:37
 * @description:
 * @modified By:
 * @version:
 */
//@Component
public class TestMessageDecoder implements MessageDecoderEncoder {
    @Override
    public <T> byte[] convertEncoder(T data) {
        return new byte[0];
    }

    @Override
    public <T> T convertDecoder(byte[] data, Class<T> clazz) {
        return null;
    }
}
