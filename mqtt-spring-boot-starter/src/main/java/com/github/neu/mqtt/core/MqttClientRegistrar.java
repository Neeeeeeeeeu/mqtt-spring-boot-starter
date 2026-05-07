package com.github.neu.mqtt.core;

import com.github.neu.mqtt.config.MqttClientConnection;
import com.github.neu.mqtt.config.MqttProperties;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;

import java.util.Collections;
import java.util.Map;

public class MqttClientRegistrar
        implements BeanDefinitionRegistryPostProcessor, EnvironmentAware, Ordered {

    private Environment environment;

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 2;
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) {
        Map<String, MqttProperties.ClientConfig> brokers = Binder.get(environment)
                .bind("mqtt.brokers",
                        Bindable.mapOf(String.class, MqttProperties.ClientConfig.class))
                .orElse(Collections.emptyMap());

        for (Map.Entry<String, MqttProperties.ClientConfig> entry : brokers.entrySet()) {
            BeanDefinition bd = BeanDefinitionBuilder
                    .genericBeanDefinition(MqttClientConnection.class)
                    .addConstructorArgValue(entry.getKey())
                    .addConstructorArgValue(entry.getValue())
                    .getBeanDefinition();
            registry.registerBeanDefinition(entry.getKey(), bd);
        }
    }

    @Override
    public void postProcessBeanFactory(org.springframework.beans.factory.config.ConfigurableListableBeanFactory beanFactory) {
        // no-op
    }
}
