package com.github.neu.mqtt.config;

import com.github.neu.mqtt.core.MessageDecoderEncoder;
import com.github.neu.mqtt.core.MqttTemplate;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * mqtt客户端 包含连接参数
 */

public class MqttClientConnection implements MqttCallback, MqttTemplate {

    private final Logger logger = LoggerFactory.getLogger(MqttClientConnection.class);
    private final ScheduledExecutorService heartbeatExecutor = Executors.newSingleThreadScheduledExecutor();

    private MemoryPersistence persistence = new MemoryPersistence();

    private MqttConnectOptions connOpts = null;

    private MqttClient client;

    private String brokerName;

    private String clientId;

    private MqttProperties.ClientConfig clientConfig;

    private MessageDecoderEncoder messageDecoderEncoder;

    private Map<String, Topic> topics = new ConcurrentHashMap<>();

    // 非阻塞连接控制：单线程执行器与原子状态
    private final ExecutorService connectExecutor = Executors.newSingleThreadExecutor();
    private final AtomicBoolean isConnecting = new AtomicBoolean(false);
    private final AtomicBoolean isConnected = new AtomicBoolean(false);

    public MqttClientConnection(String brokerName, MqttProperties.ClientConfig clientConfig) {
        this.brokerName = brokerName;
        this.clientConfig = clientConfig;
        this.clientId = clientConfig.getClientId();
        if (this.clientId == null) {
            throw  new IllegalArgumentException("MQTT客户端为空，请在配置中指定clientId，没有客户端ID无法有效追踪和管理节点");
        }
    }

    @Autowired
    public void setMessageDecoderEncoder(MessageDecoderEncoder messageDecoderEncoder) {
        this.messageDecoderEncoder = messageDecoderEncoder;
    }

    @PostConstruct
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
                    logger.info("MQTT服务器连接成功{}", nodeInfo() + " Config:" + clientConfig.toString());
                    isConnected.set(true);
                    break;
                } catch (MqttException e) {
                    logger.error("MQTT连接失败:{} 尝试重新连接", nodeInfo() + " Config:" + clientConfig.toString(), e);
                    sleep();
                }
            }
            // 设置客户端异常回调
            client.setCallback(this);
            doReSubscribe();
        } catch (Exception e){
          logger.error("mqtt:{} 连接过程中出现异常", client.getClientId(), e);
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
                        logger.info("client:{} topic:{} reSubscribe success", client.getClientId(), topic.topic);
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

    public String getBrokerName() {
        return this.brokerName;
    }

    @Override
    public String toString() {
        return super.toString();
    }

    public String nodeInfo() {
        String broker = clientConfig.getBroker();
        String username = clientConfig.getUsername();
        return " <" + brokerName + "> " + "clientId：" + clientId + " user:" + username + " broker:" + broker;
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
                logger.debug("client:{} topic:{} content:{}", brokerName, topic, dataStr);
            }
        } catch (Exception e) {
            logger.error("client:{} topic:{} publish fail", brokerName, topic, e);
        }
    }

    private void doPublish(String topic, MqttMessage message) throws MqttException {
        try {
            client.publish(topic, message);
            if (logger.isDebugEnabled()) {
                String dataStr = new String(message.getPayload(), StandardCharsets.UTF_8);
                logger.debug("client:{} topic:{} content:{}", brokerName, topic, dataStr);
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
            logger.warn("MQTT client:{} duplicate subscribed{}", brokerName, topic);
            return;
        }
        topics.put(topic, new Topic(topic, qos, messageListener));
    }

    @Override
    public <T> void publish(String topic, T data, int qos, boolean retained) {
        // 触发异步连接，避免阻塞
        try {
            doPublish(topic, messageDecoderEncoder.convertEncoder(data), qos, retained);
        } catch (MqttException e) {
            logger.error("client:{} topic:{} publish fail", brokerName, topic, e);
        }
    }

    @Override
    public void publish(String topic, MqttMessage message) {
        // 触发异步连接，避免阻塞
        try {
            doPublish(topic, message);
        } catch (MqttException e) {
            logger.error("client:{} topic:{} publish fail", brokerName, topic, e);
        }
    }

    private void startHeartbeat() {
        heartbeatExecutor.scheduleAtFixedRate(() -> {
            if (!client.isConnected() && isConnected.get()) {
                logger.warn("mqtt:{} 心跳检测到连接断开", clientId);
                isConnected.set(false);
                connectAsync();
            }
        }, 30, 30, TimeUnit.SECONDS);
    }

    @PreDestroy
    public void destroy() {
        try {
            if (client != null && client.isConnected()) {
                client.disconnect();
            }
            if (client != null) {
                client.close();
            }
        } catch (MqttException e) {
            logger.error("MQTT cleanup failed for {}", brokerName, e);
        }
        heartbeatExecutor.shutdownNow();
        connectExecutor.shutdownNow();
    }

    record Topic(String topic, int qos, IMqttMessageListener messageListener) {
    }
}
