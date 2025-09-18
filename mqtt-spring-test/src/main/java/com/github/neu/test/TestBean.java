package com.github.neu.test;


import com.github.neu.mqtt.core.MqttTemplate;
import com.github.neu.mqtt.core.annotation.MqttClient;
import com.github.neu.mqtt.core.annotation.MqttListener;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author: neeeeeeeeeu
 * @date: Created in 2024/6/26 18:17
 * @description:
 * @modified By:
 * @version:
 */
@Component
@Slf4j
public class TestBean {

    @MqttClient("node1")
    private MqttTemplate bsMqttTemplate01;

    @MqttClient("client1")
    private MqttTemplate bsMqttTemplate02;

    private AtomicLong count1 = new AtomicLong();
    private AtomicLong count2 = new AtomicLong();


    private AtomicLong send1 = new AtomicLong();
    private AtomicLong send2 = new AtomicLong();

//    @MqttListener(clientId = "node1", topic = "node1/topic001")
//    public void topicTest1(String data) {
//        bsMqttTemplate01.publish("node1/topic002", "node1publish1", 0, false);
//        bsMqttTemplate01.publish("123",new MqttMessage("node1".getBytes()));
//        if (!"node1publish1".equals(data)) {
//            log.info("node1/topic001 error: " + data);
//        }
//        log.info("node1/topic001: rec count:" + count1.getAndIncrement());
//    }

//    @MqttListener(clientId = "client1", topic = "client1/topic001")
//    public void topicTest2(String data) {
//        if (!"client1publish1".equals(data)) {
//            log.info("client1/topic001 error: " + data);
//        }
//        log.info("client1/topic001: rec count:" + count2.getAndIncrement());
//    }

    @MqttListener(clientId = "node1", topic = "node1/topic001")
    public void topicTestCount1(String data) {
        log.info("node1/topic001: " + data);
    }

    @MqttListener(clientId = "client1", topic = "bs_cloud_v2/elec_meter/761076982079/data")
    public void topicTestCount2(MeterDataCacheDTO data) {
        log.info(data.toString());

    }

    @MqttListener(clientId = "client1", topic = "client1/topic003")
    public void topicTest4(TestEntry data) {
        log.info("client1/topic002: " + data.getText());
    }

    @PostConstruct
    public void send() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                for (int i = 0; i < 100000; i++) {
                    try {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        bsMqttTemplate01.publish("node1/topic001", "node1publish1", 0, false);
                    } catch (MqttException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }).start();
//        log.info("node1/topic001: send count:" + send1.getAndIncrement());
//        bsMqttTemplate02.publish("client1/topic001", "client1publish1", 0, false);
//        log.info("client1/topic001: send count:" + send2.getAndIncrement());
    }

    private void test1() {
        for (int i = 1; i <= 100000; i++) {
            try {
                bsMqttTemplate01.publish("node1/topic001", "node1publish1", 0, false);
            } catch (MqttException e) {
                throw new RuntimeException(e);
            }
//            bsMqttTemplate01.publish("node1/topic001","node1", 0, false);
            try {
                bsMqttTemplate02.publish("client1/topic001", "client1publish1", 0, false);
            } catch (MqttException e) {
                throw new RuntimeException(e);
            }
//            bsMqttTemplate02.publish("client1/topic001","client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1client1publish1", 0, false);

            if (i % 100 == 0) {
                try {
                    bsMqttTemplate01.publish("node1/topic002", i + "", 0, false);
                } catch (MqttException e) {
                    throw new RuntimeException(e);
                }
                try {
                    bsMqttTemplate02.publish("client1/topic002", i + "", 0, false);
                } catch (MqttException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
