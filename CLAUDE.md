# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build / Test Commands

```bash
# 编译整个项目
mvn -f pom.xml clean compile

# 仅编译 starter 模块
mvn -f mqtt-spring-boot-starter/pom.xml clean compile

# 打包并安装到本地仓库
mvn -f mqtt-spring-boot-starter/pom.xml clean install

# 运行测试模块 (需要 MQTT broker 环境，无独立单元测试)
mvn -f mqtt-spring-test/pom.xml spring-boot:run
```

## 项目结构

- Java 17，Spring Boot 3.3.1，基于 Eclipse Paho MQTTv3 客户端。
- 父 POM (`pom.xml`) 为 `<packaging>pom</packaging>`，包含两个子模块。
- **`mqtt-spring-boot-starter`**: starter 核心库，提供自动配置、注解驱动的 MQTT 订阅/发布、消息编解码及连接管理。
- **`mqtt-spring-test`**: 一个 Spring Boot 应用，用于手动验证 starter 功能的示例模块（非自动化测试）。

## 架构概览

### 1. 自动配置入口

`MqttAutoConfiguration` 通过 `AutoConfiguration.imports` 自动装配。其内部类 `MqttCore` 注册核心 bean：`MqttAsyncThreadPool`、`JacksonConverter`（可被用户覆盖）、`MqttClinetFactory`、`MqttAnnotationBeanPostProcessor`。

### 2. 配置属性绑定

`@ConfigurationProperties(prefix = "mqtt")` → `MqttProperties`。YAML 结构：
```yaml
mqtt:
  brokers:
    <brokerName>:
      broker: tcp://host:port
      clientId: xxx       # 必填，broker 内唯一
      username: xxx
      password: xxx
      maxInflight: 1000
      connectTimeout: 30  # 秒
      timeToWait: 3       # 秒
      reConnectDelay: 10  # 秒
```

### 3. 容器与连接管理

`MqttClinetFactory`（`FactoryBean<MqttClientContainer>`）从 `MqttProperties` 中为每个 broker 配置创建一个 `MqttClientConnection`，并持有在 `MqttClientContainer` 内。

`MqttClientConnection` 封装 Paho `MqttClient`，实现了 `MqttTemplate` 接口并扩展了 `MqttCallback`。**连接采用非阻塞异步模式**：单线程 executor + `AtomicBoolean` 状态控制，支持断线自动重连，30 秒心跳检测。

### 4. 注解驱动消息处理

- **`@MqttListener`** — 标记在方法上，声明对某个 broker 下某个 topic 的订阅。方法签名：1 个参数 `(Payload)` 或 2 个参数 `(String topic, Payload)`。参数类型为 `MqttMessage` 时接收原始消息，否则走 Jackson 解码。
- **`@MqttClient`** — 标记在字段上，按 broker 名称注入 `MqttTemplate`。内部组合了 `@Autowired`、`@Qualifier`、`@Lazy`。

处理链路：
1. `MqttAnnotationBeanPostProcessor`（`BeanPostProcessor` + `SmartInitializingSingleton`）扫描所有 bean 的 `@MqttListener` 方法，构建 `MqttTopicEndpoint` 列表。
2. 在 `afterSingletonsInstantiated()` 回调中，`MqttEndpointRegistrar` 为每个端点创建 `BridgeListener` 并调用 `MqttTemplate.subscribe()`。
3. 消息到达时，`BridgeListener` 将消息派发到线程池中执行反射调用。

### 5. 消息编解码（SPI）

- `MessageDecoderEncoder` — 编解码接口。默认实现 `DefaultMessageDecoderEncoder` 使用 Jackson ObjectMapper 做 JSON 序列化/反序列化，同时透传 `byte[]` 和 `String`。
- `JacksonConverter` — 提供 `ObjectMapper` 的工厂接口，默认实现 `DefaultJacksonConverter` 配置为忽略未知属性、排除 null 字段。用户可通过注册同名 bean 覆盖。
- 若用户提供了自定义 `MessageDecoderEncoder` bean，框架会优先使用而非创建默认实例。

### 6. 线程模型

`MqttAsyncThreadPool` 提供固定线程池 `pool-mqtt-messageListener`（核心 24，最大 1000，无界队列，CallerRuns 拒绝策略）。每个到达的消息在该池中异步执行，避免阻塞 Paho 的接收线程。
