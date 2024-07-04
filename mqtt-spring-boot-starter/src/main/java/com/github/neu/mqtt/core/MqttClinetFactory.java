package com.github.neu.mqtt.core;

import com.github.neu.mqtt.config.MqttClientConnection;
import com.github.neu.mqtt.config.MqttProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author: neeeeeeeeeu
 * @date: Created in 2024/6/28 14:40
 * @description: 构建MQTT 客户端对象
 * @modified By:
 * @version:
 */
public class MqttClinetFactory implements FactoryBean<MqttClientContainer>, EnvironmentAware, ApplicationContextAware {
    
    private final Logger logger = LoggerFactory.getLogger(MqttClinetFactory.class);

    private ApplicationContext applicationContext;

    private Environment environment;

    private MqttProperties mqttProperties;

    public MqttClinetFactory(MqttProperties mqttProperties) {
        this.mqttProperties = mqttProperties;
    }

    @Override
    public MqttClientContainer getObject() throws Exception {
        return crateClientContainer(mqttProperties);
    }

    private MqttClientContainer crateClientContainer(MqttProperties nodeConfig) throws Exception {
        MessageDecoderEncoder messageDecoderEncoder = createMessageDecoderEncoder();

        Map<String, MqttProperties.ClientConfig> nodes = nodeConfig.getClients();
        if (nodes != null) {
            Map<String, MqttClientConnection> mqttClientRs = nodes.entrySet()
                    .stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, entry ->
                            createConnection(entry.getKey(), entry.getValue(), messageDecoderEncoder)
                    ));
            return new MqttClientContainer(mqttClientRs);
        }
        return null;
    }

    @Override
    public Class<?> getObjectType() {
        return MqttClientContainer.class;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    private MessageDecoderEncoder createMessageDecoderEncoder() {
        MessageDecoderEncoder messageDecoderEncoder = null;
        if (this.applicationContext instanceof ConfigurableApplicationContext caf) {
            if (!applicationContext.getBeansOfType(MessageDecoderEncoder.class).isEmpty()) {
                messageDecoderEncoder = this.applicationContext.getBean(MessageDecoderEncoder.class);
            } else {
                JacksonConverter converter = this.applicationContext.getBean(JacksonConverter.class);
                messageDecoderEncoder = new DefaultMessageDecoderEncoder(converter.createObjectMapper());
                caf.getBeanFactory().registerSingleton("messageDecoderEncoder", messageDecoderEncoder);
            }
        }
        return messageDecoderEncoder;
    }

    private MqttClientConnection createConnection(String clientId, MqttProperties.ClientConfig cfg, MessageDecoderEncoder messageDecoderEncoder) {
        if (this.applicationContext instanceof ConfigurableApplicationContext caf) {
            MqttClientConnection mqttClientConnection = new MqttClientConnection(clientId, cfg);
            mqttClientConnection.setMessageDecoderEncoder(messageDecoderEncoder);
            caf.getBeanFactory().registerSingleton(clientId, mqttClientConnection);
            return mqttClientConnection;
        }
        return null;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }
}
