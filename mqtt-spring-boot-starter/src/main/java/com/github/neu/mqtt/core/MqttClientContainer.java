package com.github.neu.mqtt.core;


import com.github.neu.mqtt.config.MqttClientConnection;

import java.util.Map;

/**
 * @author: neeeeeeeeeu
 * @date: Created in 2024/6/28 15:33
 * @description: MQTT 客户端容器持有类
 * @modified By:
 * @version:
 */
public class MqttClientContainer {

    private Map<String, MqttClientConnection> mqttClientWithName;

    public MqttClientContainer() {
    }

    public MqttTemplate getMqttTemplate(String brokerName) {
        return mqttClientWithName.get(brokerName);
    }

    public MqttClientContainer(Map<String, MqttClientConnection> mqttClientWithName) {
        this.mqttClientWithName = mqttClientWithName;
    }

    public void initialize() {
        this.mqttClientWithName.values().stream().forEach(MqttClientConnection::init);
    }

    public void addClinetWithName(MqttClientConnection r) {
        MqttClientConnection mQttClientR = mqttClientWithName.get(r.getBrokerName());
        if (mQttClientR != null) {
            throw new IllegalArgumentException("MQTT客户端Id已存在:" + r.toString());
        }
        mqttClientWithName.put(r.getBrokerName(), r);
    }
}
