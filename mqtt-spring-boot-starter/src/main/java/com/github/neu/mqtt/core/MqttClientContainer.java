package com.github.neu.mqtt.core;

import com.github.neu.mqtt.config.MqttClientConnection;
import com.github.neu.mqtt.config.MqttProperties;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * MQTT 客户端容器，持有 Spring 管理的连接和运行时动态创建的连接。
 */
public class MqttClientContainer {

    private final Map<String, MqttClientConnection> managedClients;
    private final Map<String, MqttClientConnection> dynamicClients = new ConcurrentHashMap<>();

    @Autowired
    private MessageDecoderEncoder messageDecoderEncoder;

    public MqttClientContainer(Map<String, MqttClientConnection> managedClients) {
        this.managedClients = new HashMap<>(managedClients);
    }

    public MqttTemplate getMqttTemplate(String brokerName) {
        MqttClientConnection conn = managedClients.get(brokerName);
        if (conn != null) {
            return conn;
        }
        return dynamicClients.get(brokerName);
    }

    /**
     * 动态创建 MQTT 客户端（非 Spring Bean），用于运行时临时 MQTT 操作。
     * 创建的客户端会存入内部映射，可通过 {@link #getMqttTemplate(String)} 获取。
     */
    public MqttClientConnection createClient(String name, MqttProperties.ClientConfig config) {
        if (managedClients.containsKey(name) || dynamicClients.containsKey(name)) {
            throw new IllegalArgumentException("MQTT client already exists: " + name);
        }
        MqttClientConnection conn = new MqttClientConnection(name, config);
        conn.setMessageDecoderEncoder(messageDecoderEncoder);
        conn.init();
        dynamicClients.put(name, conn);
        return conn;
    }

    /**
     * 移除动态创建的 MQTT 客户端并释放资源。
     * 不会影响 Spring 管理的客户端。
     */
    public void removeClient(String name) {
        MqttClientConnection removed = dynamicClients.remove(name);
        if (removed != null) {
            removed.destroy();
        }
    }
}
