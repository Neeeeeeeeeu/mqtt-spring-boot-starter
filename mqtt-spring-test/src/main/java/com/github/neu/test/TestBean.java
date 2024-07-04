package com.github.neu.test;


import com.github.neu.mqtt.core.MqttTemplate;
import com.github.neu.mqtt.core.annotation.MqttClient;
import com.github.neu.mqtt.core.annotation.MqttListener;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * @author: neeeeeeeeeu
 * @date: Created in 2024/6/26 18:17
 * @description:
 * @modified By:
 * @version:
 */
@Component
public class TestBean {

    @MqttClient("node1")
    private MqttTemplate bsMqttTemplate01;

    @MqttClient("client1")
    private MqttTemplate bsMqttTemplate02;

    @MqttListener(clientId = "node1", topic = "test/topic001")
    public void topicTest1(String data) {
        System.out.println("topicTest1: " + data);
        bsMqttTemplate01.publish("test/receive", data, 0, false);
        bsMqttTemplate02.publish("test/receive", data, 0, false);
    }

    @MqttListener(clientId = "client1", topic = "test/topic002")
    public void topicTest2(MqttMessage data) {
        System.out.println("topicTest2: " + data);
    }

    @MqttListener(clientId = "node1", topic = "test/topic003")
    public void topicTest3(TestEntry data) {
        System.out.println("topicTest3: " + data.getText());
    }

    @MqttListener(clientId = "client1", topic = "test/topic003")
    public void topicTest4(TestEntry data) {
        System.out.println("topicTest4: " + data.getText());
    }

    @MqttListener(clientId = "client_fail", topic = "test/topic003")
    public void topicTestfail(TestEntry data) {
        System.out.println("topicTestfail: " + data.getText());
    }

    @MqttListener(clientId = "node1", topic = "test/topic003")
    public void topicTestfail2(TestEntry data) {
        System.out.println("topicTestfail2: " + data.getText());
    }

    @MqttListener(clientId = "node1", topic = "test/start")
    public void send(String str) {
        for (int i = 0; i < 999; i++) {
            bsMqttTemplate01.publish("test/topic001", "test", 0, false);
        }
    }
}
