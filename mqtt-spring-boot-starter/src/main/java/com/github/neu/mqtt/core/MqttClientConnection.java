package com.github.neu.mqtt.config;

import com.github.neu.mqtt.core.MessageDecoderEncoder;
import com.github.neu.mqtt.core.MqttTemplate;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * mqtt客户端 包含连接参数
 */

public class MqttClientConnection implements MqttCallback, MqttTemplate {

    private final Logger logger = LoggerFactory.getLogger(MqttClientConnection.class);

    private MemoryPersistence persistence = new MemoryPersistence();

    private MqttConnectOptions connOpts = null;

    private MqttClient client;

    private String clientName;

    private String clientId;

    private MqttProperties.ClientConfig clientConfig;

    private MessageDecoderEncoder messageDecoderEncoder;

    private Map<String, MqttClientConnection.Topic> topics = new HashMap<>();

    // 非阻塞连接控制：单线程执行器与原子状态
    private final ExecutorService connectExecutor = Executors.newSingleThreadExecutor();
    private final AtomicBoolean isConnecting = new AtomicBoolean(false);
    private final AtomicBoolean isConnected = new AtomicBoolean(false);

    public MqttClientConnection(String clientName, MqttProperties.ClientConfig clientConfig) {
        this.clientName = clientName;
        this.clientConfig = clientConfig;
        this.clientId = getClientId(clientName, clientConfig.getClientId());
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
//C/C++ 库 jni 接口 *char[] readFile();
//private native char[] readFile = "mqtt-jni";

// roback - context map<String,T> 包含任务的全部信息。
// runable(context ctx);
//
    private void initMqttClient() throws MqttException {
        client = new MqttClient(
                clientConfig.getBroker()
                , clientId
                , persistence);
        client.setTimeToWait(clientConfig.getTimeToWait() * 1000L);
        // MQTT 连接选项
        connOpts = new MqttConnectOptions();
        //没配置并发量的默认大小10
        connOpts.setMaxInflight(clientConfig.getMaxInflight() <= 0 ? 10 : clientConfig.getMaxInflight());
        connOpts.setUserName(clientConfig.getUsername());
        connOpts.setPassword(clientConfig.getPassword().toCharArray());
        // 链接超时默认30秒
        connOpts.setConnectionTimeout(clientConfig.getConnectTimeout());
        connOpts.setCleanSession(true);
        // 非阻塞触发连接
        connectAsync();
    }

    // 非阻塞连接入口：快速返回，不阻塞调用线程，避免重复提交
    private void connectAsync() {
        if (isConnected.get()) {
            return;
        }
        if (!isConnecting.compareAndSet(false, true)) {
            return;
        }
        connectExecutor.submit(this::connectLoop);
    }

    // 连接循环在单线程中运行
    private void connectLoop() {
        try {
            while (!client.isConnected()) {
                try {
                    // 建立连接
                    if (connOpts == null) {
                        client.connect();
                    } else {
                        client.connect(connOpts);
                    }
                    logger.info("MQTT服务器连接成功{}", nodeInfo() +" Config:"+clientConfig.toString());
                    isConnected.set(true);
                    break;
                } catch (MqttException e) {
                    logger.error("MQTT连接失败:{} 尝试重新连接", nodeInfo() +" Config:"+clientConfig.toString(), e);
                    sleep();
                }
            }
            // 设置客户端异常回调
            client.setCallback(this);
            doReSubscribe();
        } finally {
            // 允许后续触发重连
            isConnecting.set(false);
        }
    }

    private void doReSubscribe() {
        topics.values().stream()
                .forEach(topic -> {
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
        isConnected.set(false);
        connectAsync();
    }

    private void sleep() {
        try {
            Thread.sleep(clientConfig.getReConnectDelay() * 1000L);
        } catch (InterruptedException ignore) {
            logger.error("mqtt:{} 重新连接等待被中断", client.getClientId(), ignore);
            Thread.currentThread().interrupt();
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

    public String getClientName() {
        return this.clientName;
    }

    @Override
    public String toString() {
        return super.toString();
    }

    public String nodeInfo() {
        String broker = clientConfig.getBroker();
        String username = clientConfig.getUsername();
                return " <" + clientName + "> " + "clientId：" + clientId + " user:" + username + " broker:" + broker;
    }

    private void doSubscribe(String topic, int qos, IMqttMessageListener messageListener) {
        try {
            client.subscribe(topic, qos, messageListener);
            logger.info("client:{} topic:{} subscribe success", client.getClientId(), topic);
        } catch (MqttException e) {
            logger.error("client:{} topic:{} subscribe fail", client.getClientId(), topic, e);
        }
    }

    private void doPublish(String topic, byte[] data, int qos, boolean retained) throws MqttException {
        try {
            client.publish(topic, data, qos, retained);
            if (logger.isDebugEnabled()) {
                String dataStr = new String(data, StandardCharsets.UTF_8);
                logger.debug("client:{} topic:{} content:{}", clientName, topic, dataStr);
            }
        } catch (Exception e) {
            logger.error("client:{} topic:{} publish fail", clientName, topic, e);
        }
    }

    private void doPublish(String topic, MqttMessage message) throws MqttException {
        try {
            client.publish(topic, message);
            if (logger.isDebugEnabled()) {
                String dataStr = new String(message.getPayload(), StandardCharsets.UTF_8);
                logger.debug("client:{} topic:{} content:{}", clientName, topic, dataStr);
            }
        } catch (Exception e) {
            logger.error("client:{} topic:{} publish fail", client, topic, e);
        }
    }


    @Override
    public void subscribe(String topic, int qos, IMqttMessageListener messageListener) {
        // 触发异步连接，避免阻塞
        connectAsync();
        if (topics.containsKey(topic)) {
                        logger.warn("MQTT client:{} duplicate subscribed{}", clientName, topic);
            return;
        }
        doSubscribe(topic, qos, messageListener);
        topics.put(topic, new Topic(topic, qos, messageListener));
    }

    @Override
    public <T> void publish(String topic, T data, int qos, boolean retained) {
        // 触发异步连接，避免阻塞
        connectAsync();
        try {
            doPublish(topic, messageDecoderEncoder.convertEncoder(data), qos, retained);
        } catch (MqttException e) {
            logger.error("client:{} topic:{} publish fail", clientName, topic, e);
        }
    }

    @Override
    public void publish(String topic, MqttMessage message) {
        // 触发异步连接，避免阻塞
        connectAsync();
        try {
            doPublish(topic, message);
        } catch (MqttException e) {
            logger.error("client:{} topic:{} publish fail", clientName, topic, e);
        }
    }

    private String getClientId(String clientName, String clientId) {
        if (clientId == null || clientId.isEmpty()) {
            int length = 16; // 指定生成的16进制字符串长度
            SecureRandom secureRandom = new SecureRandom();
            byte[] randomBytes = new byte[length / 2]; // 每个字节对应两个16进制字符
            secureRandom.nextBytes(randomBytes);

            StringBuilder hexString = new StringBuilder();
            for (byte b : randomBytes) {
                hexString.append(String.format("%02x", b)); // 将字节转换为两位16进制
            }
            return clientName + "_" + hexString;
        } else {
            return clientId;
        }
    }

    record Topic(String topic, int qos, IMqttMessageListener messageListener) {
    }
}
