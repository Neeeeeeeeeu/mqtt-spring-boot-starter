package com.github.neu.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author: neeeeeeeeeu
 * @date: Created in 2024/6/26 18:40
 * @description:
 * @modified By:
 * @version:
 */
@SpringBootApplication()
@EnableScheduling
@ConfigurationPropertiesScan
public class MqttTestMainAppliaction extends SpringApplication {

    public static void main(String[] args) {
        run(MqttTestMainAppliaction.class, args);
    }
}
