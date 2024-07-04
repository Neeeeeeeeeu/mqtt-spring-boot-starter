package com.github.neu.mqtt.threadpool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

public class MqttAsyncThreadPool {

    private final Logger logger = LoggerFactory.getLogger(MqttAsyncThreadPool.class);

    public static final String POOL_MQTT_LISTENER = "pool-mqtt-messageListener";

    private ThreadPoolTaskExecutor listenerPool;

    public MqttAsyncThreadPool() {
        listenerPool = poolWoekerMqttListener();
    }

    public ThreadPoolTaskExecutor getListenerPool() {
        return listenerPool;
    }

    public ThreadPoolTaskExecutor poolWoekerMqttListener() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        //配置核心线程数
        executor.setCorePoolSize(24);
        //配置最大线程数
        executor.setMaxPoolSize(1000);
        //配置队列大小
        executor.setQueueCapacity(Integer.MAX_VALUE);
        //配置线程池中的线程的名称前缀
        executor.setThreadNamePrefix("mqtt-listener");
        // rejection-policy：当pool已经达到max size的时候，如何处理新任务
        // CALLER_RUNS：不在新线程中执行任务，而是有调用者所在的线程来执行
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        //执行初始化
        executor.initialize();
        listenerPool = executor;
        return executor;
    }

    public void showPoolHealth() {
        if (listenerPool != null) {
            logger.info("===线程池状态===");
            logger.info("提交线程_活跃线程:{},队列大小:{}", listenerPool.getActiveCount(), listenerPool.getThreadPoolExecutor().getQueue().size());
        }
    }
}
