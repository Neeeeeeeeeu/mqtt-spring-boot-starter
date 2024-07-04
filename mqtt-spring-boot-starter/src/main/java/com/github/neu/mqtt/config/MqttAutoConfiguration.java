package com.github.neu.mqtt.config;

import com.github.neu.mqtt.core.*;
import com.github.neu.mqtt.threadpool.MqttAsyncThreadPool;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@EnableConfigurationProperties(MqttProperties.class)
public class MqttAutoConfiguration {

    private final MqttProperties mqttProperties;

    public MqttAutoConfiguration(MqttProperties mqttProperties) {
        this.mqttProperties = mqttProperties;
    }

    @Configuration
    public class MqttCore {

        @Bean
        public MqttAsyncThreadPool mQttAsyncThreadPool() {
            return new MqttAsyncThreadPool();
        }

        @Bean
        @ConditionalOnMissingBean(JacksonConverter.class)
        public DefaultJacksonConverter defaultMessageConvert() {
            return new DefaultJacksonConverter();
        }

        @Bean
        public MqttClinetFactory mQttClinetFactory() {
            return new MqttClinetFactory(mqttProperties);
        }

        @Bean
        @ConditionalOnBean(MqttClientContainer.class)
        public MqttAnnotationBeanPostProcessor mQttAnnotationBeanPostProcessor() {
            return new MqttAnnotationBeanPostProcessor();
        }
    }

}
