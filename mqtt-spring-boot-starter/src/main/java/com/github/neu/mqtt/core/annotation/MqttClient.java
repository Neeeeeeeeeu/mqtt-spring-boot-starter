package com.github.neu.mqtt.core.annotation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.lang.annotation.*;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Autowired
@Qualifier
public @interface MqttClient {

    /**
     * mqtt broker name (YAML config key)
     */
    String value();
}
