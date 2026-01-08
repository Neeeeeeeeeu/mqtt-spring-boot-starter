package com.github.neu.mqtt.core;

import com.github.neu.mqtt.core.annotation.MqttListener;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EmbeddedValueResolverAware;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringValueResolver;

import java.lang.reflect.Method;

/**
 * @author: neeeeeeeeeu
 * @date: Created in 2024/7/1 17:02
 * @description:
 * @modified By:
 * @version:
 */
public class MqttAnnotationBeanPostProcessor implements
        BeanPostProcessor, Ordered, SmartInitializingSingleton,
        ApplicationContextAware, EmbeddedValueResolverAware {

    private MqttEndpointRegistrar registrar = new MqttEndpointRegistrar();

    private BeanFactory beanFactory;
    private StringValueResolver valueResolver;

    @Nullable
    private ApplicationContext applicationContext;

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        // 获取原始类（解开 CGLIB/JDK 代理）
        Class<?> targetClass = ClassUtils.getUserClass(bean.getClass());
        ReflectionUtils.doWithMethods(targetClass, method -> {
            resloveMqttListener(targetClass, method, bean);
        });
        return bean;
    }

    private void resloveMqttListener(Class<?> clazz, Method method, Object bean) {
        MqttListener annotation = AnnotatedElementUtils.findMergedAnnotation(method, MqttListener.class);
        if (annotation != null) {
            String topic = resolve(annotation.topic());
            String brokerName = resolve(annotation.brokerName());
            int qos = annotation.qos();
            Class<?>[] parameterTypes = method.getParameterTypes();
            if (parameterTypes.length == 0 || parameterTypes.length > 2) {
                throw new RuntimeException("MqttListener method must have one or two parameters");
            }
            MqttTopicEndpoint mQttTopic;
            if (parameterTypes.length == 1) {
                mQttTopic = new MqttTopicEndpoint(brokerName, topic, annotation.qos(), clazz, bean, method
                        , parameterTypes[0]);
            } else {
                mQttTopic = new MqttTopicEndpoint(brokerName, topic, annotation.qos(), clazz, bean, method
                        , parameterTypes[1]);
            }
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

    @Override
    public void setEmbeddedValueResolver(StringValueResolver resolver) {
        this.valueResolver = resolver;
    }

    //封装解析spring配置占位符
    private String resolve(String value) {
        // 如果值是以 ${ 开始并以 } 结束，或者包含占位符，解析它
        return (this.valueResolver != null) ? this.valueResolver.resolveStringValue(value) : value;
    }
}

