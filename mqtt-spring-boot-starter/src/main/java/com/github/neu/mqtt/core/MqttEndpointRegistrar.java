package com.github.neu.mqtt.core;

import com.github.neu.mqtt.threadpool.MqttAsyncThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author: neeeeeeeeeu
 * @date: Created in 2024/7/2 14:20
 * @description: 端点注册类
 * @modified By:
 * @version:
 */
public class MqttEndpointRegistrar implements InitializingBean {

    private final Logger logger = LoggerFactory.getLogger(MqttEndpointRegistrar.class);

    private List<MqttTopicEndpoint> endpoints = new ArrayList<>();

    private MqttClientContainer containerMap;

    private JacksonConverter jacksonConverter;

    private MessageDecoderEncoder messageDecoderEncoder;

    private MqttAsyncThreadPool mQttAsyncThreadPool;

    private ApplicationContext applicationContext;

    private BeanFactory beanFactory;

    public void setBeanFactory(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public void add(MqttTopicEndpoint endpoint) {
        endpoints.add(endpoint);
    }

    private void registerAllEndpoints() {
        synchronized (this.endpoints) {
            endpoints.forEach(endpoint -> {
                String brokerName = endpoint.getBrokerName();
                MqttTemplate mQttTemplate = containerMap.getMqttTemplate(brokerName);
                if (mQttTemplate != null) {
                    BridgeListener bridgeListener = new BridgeListener(endpoint.getTopicName(),
                            new InvocableHandlerMethod(endpoint.getBean(), endpoint.getMethod(), endpoint.getConvertType()),
                            this.messageDecoderEncoder,
                            this.mQttAsyncThreadPool.getListenerPool());
                    try {
                        mQttTemplate.subscribe(endpoint.getTopicName(), endpoint.getQos(), bridgeListener);
                    } catch (Exception e) {
                        logger.error("brokerName:{} 订阅失败，请检查YAML文件。 订阅位于:{}", brokerName, endpoint.getClazz().getName() + "." + endpoint.getMethod().getName());
                    }
                } else {
                    logger.error("brokerName:{} 未找到对应MQTT配置，请检查YAML文件。 订阅位于:{}", brokerName, endpoint.getClazz().getName() + "." + endpoint.getMethod().getName());
                    throw new RuntimeException("brokerName:" + brokerName + " 未找到对应MQTT配置，请检查YAML文件 订阅位于:" + endpoint.getClazz().getName() + "." + endpoint.getMethod().getName());
                }
            });
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        createThreadPool();
        createEncoderDecoder();
        createJacksonConverter();
        registerAllEndpoints();
    }

    public void resloveMqttTemplate(String clientId, Consumer<MqttTemplate> t) {
        if (containerMap == null) {
            return;
        }
        MqttTemplate mqttTemplate = containerMap.getMqttTemplate(clientId);
        if (mqttTemplate != null) {
            t.accept(mqttTemplate);
        }
    }

    public void setContainer(MqttClientContainer container) {
        this.containerMap = container;

    }

    public void createThreadPool() {
        this.mQttAsyncThreadPool = beanFactory.getBean(MqttAsyncThreadPool.class);
    }

    public void createEncoderDecoder() {
        this.messageDecoderEncoder = beanFactory.getBean(MessageDecoderEncoder.class);
    }

    public void createJacksonConverter() {
        this.jacksonConverter = beanFactory.getBean(JacksonConverter.class);
    }
}
