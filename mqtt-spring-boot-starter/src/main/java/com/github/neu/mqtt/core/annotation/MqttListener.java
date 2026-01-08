package com.github.neu.mqtt.core.annotation;

import org.apache.logging.log4j.util.Strings;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface MqttListener {

    /**
     * 订阅主题名
     */
    String topic() default Strings.EMPTY;

    /**
     * yml中的自定义客户端时的节点名
     * xxx:
     *  node1: <-- 这里的node1
     * @return
     */
    String brokerName() default Strings.EMPTY;

    int qos() default 0;
}
