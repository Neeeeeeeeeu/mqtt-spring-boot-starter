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

    public void invoke(MqttMessage message) {
        if (invocableHandlerMethod.argType() == MqttMessage.class) {
            invocableHandlerMethod.invoke(message);
        } else {
            byte[] payload = message.getPayload();
            Object targetType = messageDecoderEncoder.convertDecoder(payload, invocableHandlerMethod.argType());
            invocableHandlerMethod.invoke(targetType);
        }
    }

    @Override
    public void messageArrived(String topic, MqttMessage message){
        executor.execute(() -> {
            try {
                invoke(message);
            }catch (Exception e){
                logger.error("mqtt messageArrived topic:{} error:{}",topic, e.getMessage(),e);
            }
        });
    }
}
