package com.github.neu.mqtt.core;

import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttMessage;

/**
 * @author: neeeeeeeeeu
 * @date: Created in 2024/7/3 14:36
 * @description:
 * @modified By:
 * @version:
 */
public interface MqttTemplate {

    <T> void publish(String topic, T data, int qos, boolean retained);

    void publish(String topic, MqttMessage message);

    void subscribe(String topic, int qos, IMqttMessageListener messageListener);
    
}
