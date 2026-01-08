package com.github.neu.mqtt.config;

import com.github.neu.mqtt.core.Constants;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

/**
 * @author: neeeeeeeeeu
 * @date: Created in 2024/6/28 14:16
 * @description:
 * @modified By:
 * @version:
 */

@ConfigurationProperties(prefix = Constants.BS_MQTT)
public class MqttProperties {

    /**
     * key 不再使用它作为clinetid 仅仅作为注解中指定的mqtt客户端配置文件中的节点的名字
     * value 客户端配置信息
     */
    private Map<String, ClientConfig> brokers;

    public static class ClientConfig {
        // MQTT服务器 ip:port
        private String broker = null;
        // 客户端id MQTT中不可重复 未指定则根据mqtt当前的配置名字+随机字符串
        // 如果主动配置则直接应用，启动相同实例会出现冲突。
        private String clientId = null;
        //用户名
        private String username = null;
        //密码
        private String password = null;
        //可同时发送的数据量 MQTT 并发数量 超了就会抛出异常 看着配 默认配置10
        private int maxInflight = 1000;
        //MQTT连接超时时间 默认30秒
        private int connectTimeout = 30;
        //同步操作等待异步完成的最大时间  默认3秒
        private int timeToWait = 3;
        //重连间隔时间 默认0秒
        private int reConnectDelay = 10;

        public ClientConfig() {
        }

        public String getBroker() {
            return broker;
        }

        public void setBroker(String broker) {
            this.broker = broker;
        }

        public String getClientId() {
            return clientId;
        }

        public void setClientId(String clientId) {
            this.clientId = clientId;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public int getMaxInflight() {
            return maxInflight;
        }

        public void setMaxInflight(int maxInflight) {
            this.maxInflight = maxInflight;
        }

        public int getConnectTimeout() {
            return connectTimeout;
        }

        public void setConnectTimeout(int connectTimeout) {
            this.connectTimeout = connectTimeout;
        }

        public int getTimeToWait() {
            return timeToWait;
        }

        public void setTimeToWait(int timeToWait) {
            this.timeToWait = timeToWait;
        }

        public int getReConnectDelay() {
            return reConnectDelay;
        }

        public void setReConnectDelay(int reConnectDelay) {
            this.reConnectDelay = reConnectDelay;
        }

        @Override
        public String toString() {
            return "ClientConfig{" +
                    "broker='" + broker + '\'' +
                    ", clientId='" + clientId + '\'' +
                    ", username='" + username + '\'' +
                    ", password='" + password + '\'' +
                    ", maxInflight=" + maxInflight +
                    ", connectTimeout=" + connectTimeout +
                    ", timeToWait=" + timeToWait +
                    ", reConnectDelay=" + reConnectDelay +
                    '}';
        }
    }

    public Map<String, ClientConfig> getBrokers() {
        return brokers;
    }

    public void setBrokers(Map<String, ClientConfig> brokers) {
        this.brokers = brokers;
    }

}

