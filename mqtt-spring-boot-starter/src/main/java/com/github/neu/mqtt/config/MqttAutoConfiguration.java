package com.github.neu.mqtt.config;

import com.github.neu.mqtt.core.*;
import com.github.neu.mqtt.config.MqttClientConnection;
import com.github.neu.mqtt.threadpool.MqttAsyncThreadPool;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@AutoConfiguration
@EnableConfigurationProperties(MqttProperties.class)
public class MqttAutoConfiguration {

    @Configuration
    public class MqttCore {

        @Bean
        public MqttAsyncThreadPool mQttAsyncThreadPool() {
            return new MqttAsyncThreadPool();
        }

        @Bean
        @ConditionalOnMissingBean(JacksonConverter.class)
        public JacksonConverter defaultMessageConvert() {
            return new DefaultJacksonConverter();
        }

        @Bean
        @ConditionalOnMissingBean(MessageDecoderEncoder.class)
        public MessageDecoderEncoder messageDecoderEncoder(JacksonConverter jacksonConverter) {
            return new DefaultMessageDecoderEncoder(jacksonConverter.createObjectMapper());
        }

        @Bean
        public static MqttClientRegistrar mqttClientRegistrar() {
            return new MqttClientRegistrar();
        }

        @Bean
        public MqttClientContainer mqttClientContainer(Map<String, MqttClientConnection> connections) {
            return new MqttClientContainer(connections);
        }

        @Bean
        @ConditionalOnBean(MqttClientContainer.class)
        public static MqttAnnotationBeanPostProcessor mQttAnnotationBeanPostProcessor() {
            return new MqttAnnotationBeanPostProcessor();
        }
    }
}
