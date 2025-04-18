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
    private Map<String, ClientConfig> clients;

    public static class ClientConfig {
        // MQTT服务器 ip:port
        private String broker;
        // 客户端id MQTT中不可重复
        private String clientId;
        //用户名
        private String username;
        //密码
        private String password;
        //可同时发送的数据量 MQTT 并发数量 超了就会抛出异常 看着配 默认配置10
        private int maxInflight;

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
    }

    public Map<String, ClientConfig> getClients() {
        return clients;
    }

    public void setClients(Map<String, ClientConfig> clients) {
        this.clients = clients;
    }
}
