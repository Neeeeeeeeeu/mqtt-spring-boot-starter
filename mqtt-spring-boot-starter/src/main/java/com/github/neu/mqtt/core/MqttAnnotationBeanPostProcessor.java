package com.github.neu.mqtt.core;

import com.github.neu.mqtt.core.annotation.MqttClient;
import com.github.neu.mqtt.core.annotation.MqttListener;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.Ordered;
import org.springframework.lang.Nullable;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * @author: neeeeeeeeeu
 * @date: Created in 2024/7/1 17:02
 * @description:
 * @modified By:
 * @version:
 */
public class MqttAnnotationBeanPostProcessor implements BeanPostProcessor, Ordered, SmartInitializingSingleton, ApplicationContextAware {

    private MqttEndpointRegistrar registrar = new MqttEndpointRegistrar();

    private BeanFactory beanFactory;

    @Nullable
    private ApplicationContext applicationContext;

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        Class<?> clazz = bean.getClass();
        ReflectionUtils.doWithMethods(clazz, method -> {
            resloveMqttListener(clazz, method, bean);
        });
        return bean;
    }

    private void resloveMqttListener(Class<?> clazz, Method method, Object bean) {
        MqttListener annotation = method.getAnnotation(MqttListener.class);
        if (annotation != null) {
            String clientId = annotation.clientId();
            String topic = annotation.topic();
            int qos = annotation.qos();
            Class<?>[] parameterTypes = method.getParameterTypes();
            if (parameterTypes.length != 1) {
                throw new RuntimeException("MqttListener method must have one parameter");
            }
            MqttTopicEndpoint mQttTopic = new MqttTopicEndpoint(clientId, topic, annotation.qos(), clazz, bean, method
                    , Arrays.stream(parameterTypes).findFirst().get());
            registrar.add(mQttTopic);
        }
    }

    @Override
    public int getOrder() {
        return LOWEST_PRECEDENCE;
    }

    @Override
    public void afterSingletonsInstantiated() {
        MqttClientContainer bean = this.beanFactory.getBean(MqttClientContainer.class);
        //添加mqtt客户端
        registrar.setContainer(bean);
        registrar.setBeanFactory(this.beanFactory);
        registrar.setApplicationContext(this.applicationContext);
        //注册监听器与主题
        try {
            registrar.afterPropertiesSet();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
        setBeanFactory(applicationContext);
    }

    public synchronized void setBeanFactory(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }
}

