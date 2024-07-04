package com.github.neu.mqtt.config;

import com.github.neu.mqtt.core.MessageDecoderEncoder;
import com.github.neu.mqtt.core.MqttTemplate;
import com.github.neu.mqtt.core.DefaultMessageDecoderEncoder;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * mqtt客户端 包含连接参数
 */

public class MqttClientConnection implements MqttCallback, MqttTemplate {

    private final Logger logger = LoggerFactory.getLogger(MqttClientConnection.class);

    /**
     * MQTT 重连失败尝试间隔
     */
    private static final int RECONNECT_TIME = 10000;

    private MemoryPersistence persistence = new MemoryPersistence();

    private MqttConnectOptions connOpts = null;

    private MqttClient client;

    private String clientId;

    private MqttProperties.ClientConfig clientConfig;

    private MessageDecoderEncoder messageDecoderEncoder;

    private Map<String, MqttClientConnection.Topic> topics = new HashMap<>();


    public MqttClientConnection(String clientId, MqttProperties.ClientConfig clientConfig) {
        this.clientId = clientId;
        this.clientConfig = clientConfig;
    }

    public void setMessageDecoderEncoder(MessageDecoderEncoder messageDecoderEncoder) {
        this.messageDecoderEncoder = messageDecoderEncoder;
    }

    public void init() {
        try {
            initMqttClient();
        } catch (MqttException e) {
            logger.error("MQTT Client创建失败:{}", clientConfig.getBroker(), e);
            throw new RuntimeException(e);
        }
    }

    private void initMqttClient() throws MqttException {
        client = new MqttClient(
                clientConfig.getBroker()
                , clientId
                , persistence);
        client.setTimeToWait(1000);
        // MQTT 连接选项
        if (clientConfig.getUsername() != null) {
            connOpts = new MqttConnectOptions();
            //没配置并发量的默认大小10
            connOpts.setMaxInflight(clientConfig.getMaxInflight() <= 0 ? 10 : clientConfig.getMaxInflight());
            connOpts.setUserName(clientConfig.getUsername());
            connOpts.setPassword(clientConfig.getPassword().toCharArray());
        }
        connOpts.setCleanSession(true);
        connect();
    }

    private synchronized void connect() {
        while (!client.isConnected()) {
            try {
                // 建立连接
                if (connOpts == null) {
                    client.connect();
                } else {
                    client.connect(connOpts);
                }
                logger.info("MQTT服务器连接成功{}", nodeInfo());
                break;
            } catch (MqttException e) {
                logger.error("MQTT连接失败:{} 尝试重新连接", nodeInfo(), e);
                sleep();
            }
        }
        // 设置客户端异常回调
        client.setCallback(this);
        doReSubscribe();
    }

    private void doReSubscribe() {
        topics.values().stream()
                .forEach(topic -> {
                    // TODO: 2024/7/1  订阅主题
                    //subscribe(mqttHandler.getTopicFilter(), mqttHandler.getQos(), mqttHandler.getHandlerClass());
                    try {
                        client.subscribe(topic.topic, topic.qos, topic.messageListener);
                    } catch (MqttException e) {
                        throw new RuntimeException(e);
                    }
                });
    }


    @Override
    public void connectionLost(Throwable cause) {
        logger.warn("mqtt:{} 断开链接, 尝试重新链接", client.getClientId());
        connect();
    }

    private void sleep() {
        try {
            Thread.sleep(RECONNECT_TIME);
        } catch (InterruptedException ignore) {
        }
    }

    int i = 0;

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        if (logger.isDebugEnabled()) {
            logger.debug("mqtt:{} 收到消息:{}", client.getClientId(), new String(message.getPayload()));
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        if (logger.isDebugEnabled()) {
            logger.debug("mqtt:{} 消息发送完成", token);
        }
    }

    public String getClientId() {
        return this.clientId;
    }

    @Override
    public String toString() {
        return super.toString();
    }

    public String nodeInfo() {
        String broker = clientConfig.getBroker();
        String username = clientConfig.getUsername();
        return " <" + clientId + "> " + " uers:" + username + " broker:" + broker;
    }

    private void doSubscribe(String topic, int qos, IMqttMessageListener messageListener) {
        try {
            client.subscribe(topic, qos, messageListener);
        } catch (MqttException e) {
            logger.error(clientId + "主题:{}订阅失败", topic, e);
        }
        logger.info(clientId + " 主题:{}订阅成功", topic);
    }

    private <T> void doPublish(String topic, byte[] data, int qos, boolean retained) {
        try {
            client.publish(topic, data, qos, retained);
        } catch (MqttException e) {
            logger.error(clientId + "主题:{}发布失败", topic, e);
        }
    }

    private void doPublish(String topic, MqttMessage message) {
        try {
            client.publish(topic, message);
        } catch (MqttException e) {
            logger.error(clientId + "主题:{}发布失败", topic, e);
        }
    }


    @Override
    public void subscribe(String topic, int qos, IMqttMessageListener messageListener) {
        if (topics.containsKey(topic)){
            logger.info("mqtt {}已经订阅{}", clientId, topic);
            return;
        }
        doSubscribe(topic, qos, messageListener);
        topics.put(topic, new Topic(topic, qos, messageListener));
    }

    @Override
    public <T> void publish(String topic, T data, int qos, boolean retained) {
        doPublish(topic, messageDecoderEncoder.convertEncoder(data), qos, retained);
    }

    @Override
    public void publish(String topic, MqttMessage message) {
        doPublish(topic, message);
    }

    record Topic(String topic, int qos, IMqttMessageListener messageListener) {
    }
}
