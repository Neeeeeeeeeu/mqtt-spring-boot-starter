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

    private Map<String, MqttClientConnection> mqttClientRs;

    public MqttClientContainer() {
    }

    public MqttTemplate getMqttTemplate(String clientId) {
        return mqttClientRs.get(clientId);
    }

    public MqttClientContainer(Map<String, MqttClientConnection> mqttClientRs) {
        this.mqttClientRs = mqttClientRs;
    }

    public void initialize() {
        this.mqttClientRs.values().stream().forEach(MqttClientConnection::init);
    }

    public void addClinet(MqttClientConnection r) {
        MqttClientConnection mQttClientR = mqttClientRs.get(r.getClientId());
        if (mQttClientR != null) {
            throw new IllegalArgumentException("MQTT客户端已存在:" + r.toString());
        }
        mqttClientRs.put(r.getClientId(), r);
    }
}
