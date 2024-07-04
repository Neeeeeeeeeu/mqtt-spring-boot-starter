package com.github.neu.mqtt.core;

import java.lang.reflect.Method;

/**
 * @author: neeeeeeeeeu
 * @date: Created in 2024/6/28 17:02
 * @description: 提供订阅者方法的映射端点
 * @modified By:
 * @version:
 */
public class MqttTopicEndpoint {

    private String clientId;
    
    private String topicName;
    
    private int qos;
    
    private Class clazz;
    
    private Class<?> convertType;
    
    private Object bean;
    
    private Method method;

    public MqttTopicEndpoint(String clientId, String topicName,
                             int qos, Class clazz, Object bean, 
                             Method method, Class<?> convertType) {
        
        this.clientId = clientId;
        
        this.topicName = topicName;
        
        this.qos = qos;
        
        this.clazz = clazz;
        
        this.bean = bean;
        
        this.method = method;
        
        this.convertType = convertType;
        
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getTopicName() {
        return topicName;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }

    public int getQos() {
        return qos;
    }

    public void setQos(int qos) {
        this.qos = qos;
    }

    public Class getClazz() {
        return clazz;
    }

    public void setClazz(Class clazz) {
        this.clazz = clazz;
    }

    public Object getBean() {
        return bean;
    }

    public void setBean(Object bean) {
        this.bean = bean;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public Class<?> getConvertType() {
        return convertType;
    }

    public void setConvertType(Class<?> convertType) {
        this.convertType = convertType;
    }
}
