package com.github.neu.mqtt.core;

/**
 * 解码mqtt消息
 */
public interface MessageDecoderEncoder {

    <T> byte[] convertEncoder(T data);

    <T> T convertDecoder(byte[] data, Class<T> clazz);
}
