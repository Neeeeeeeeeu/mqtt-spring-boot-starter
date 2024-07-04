package com.github.neu.mqtt.core.annotation;

import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * @author: neeeeeeeeeu
 * @date: Created in 2024/7/3 18:41
 * @description:
 * @modified By:
 * @version:
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Autowired // 合并 @Autowired 功能
@Qualifier
@Lazy
public @interface MqttClient {

    /**
     * mqtt clientId
     * @return
     */
    String value();
}
