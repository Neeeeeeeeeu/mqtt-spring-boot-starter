package com.github.neu.mqtt.core;

import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * @author: neeeeeeeeeu
 * @date: Created in 2024/7/3 16:04
 * @description:
 * @modified By:
 * @version:
 */
public class BridgeListener implements IMqttMessageListener {

    private final Logger logger = LoggerFactory.getLogger(BridgeListener.class);

    private String topicName;

    private final InvocableHandlerMethod invocableHandlerMethod;

    private final MessageDecoderEncoder messageDecoderEncoder;

    private final ThreadPoolTaskExecutor executor;

    public BridgeListener(String topicName, InvocableHandlerMethod invocableHandlerMethod, MessageDecoderEncoder messageDecoderEncoder, ThreadPoolTaskExecutor executor) {
        this.invocableHandlerMethod = invocableHandlerMethod;
        this.messageDecoderEncoder = messageDecoderEncoder;
        this.topicName = topicName;
        this.executor = executor;
    }

    public void invoke(String topic, MqttMessage message) {
        // 1. 获取目标参数个数 (这是一个极快的元数据读取)
        int paramCount = invocableHandlerMethod.getMethod().getParameterCount();

        // 2. 准备 Payload 数据 (仅在需要时转换，Lazy load 思路)
        Object payloadTarget;
        if (invocableHandlerMethod.argType() == MqttMessage.class) {
            payloadTarget = message;
        } else {
            // 这里是单纯的数据转换，没有反射开销
            payloadTarget = messageDecoderEncoder.convertDecoder(message.getPayload(), invocableHandlerMethod.argType());
        }

        // 3. 执行调用 (Strict Order 策略)
        // 依据 MqttAnnotationBeanPostProcessor 的逻辑：
        // - 1个参数 -> (Payload)
        // - 2个参数 -> (Topic, Payload)  <-- 注意这里 Topic 在前

        if (paramCount == 2) {
            // 对应 method(String topic, MyResult data)
            invocableHandlerMethod.invoke(topic, payloadTarget);
        } else {
            // 对应 method(MyResult data)
            invocableHandlerMethod.invoke(payloadTarget);
        }
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) {
        executor.execute(() -> {
            try {
                invoke(topic, message);
            } catch (Exception e) {
                logger.error("mqtt messageArrived topic:{} error:{}", topic, e.getMessage(), e);
            }
        });
    }
}
