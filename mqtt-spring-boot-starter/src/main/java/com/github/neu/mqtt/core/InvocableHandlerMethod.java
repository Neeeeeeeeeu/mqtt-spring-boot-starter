package com.github.neu.mqtt.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

/**
 * @author: neeeeeeeeeu
 * @date: Created in 2024/7/2 18:16
 * @description:
 * @modified By:
 * @version:
 */
public class InvocableHandlerMethod {

    private final Logger logger = LoggerFactory.getLogger(InvocableHandlerMethod.class);

    private final Object bean;

    private final Method method;

    private final Class<?> convertType;
    
    public InvocableHandlerMethod(Object bean, Method method,Class<?> convertType) {
        this.bean = bean;
        this.method = method;
        this.convertType = convertType;
    }

    public Object invoke(Object... args) {
        try {
            return method.invoke(bean, args);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Class<?> argType() {
        return convertType;
    }
}
