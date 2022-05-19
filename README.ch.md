# Alpaca

​	Alpaca 是一款轻量级的, 基于 java 语言的开源 RPC 框架, 致力于提供高性能和透明化的 RPC 远程服务调用方案, 以及 SOA 服务治理方案，使得应用可通过 Alpaca 实现服务的远程调用功能, 集成于 Springboot 进行应用开发

​	Alpaca 包含远程通讯、服务集群和服务发现与注册三个核心部分, 提供透明化的远程方法调用, 实现像调用本地方法一样调用远程方法，只需简单配置, 没有任何 API 侵入, 同时具备软负载均衡机制, 支持实现服务自动注册与发现, 不再需要写死服务提供方地址, 注册中心基于接口名查询服务提供者的 IP 地址, 并且能够平滑添加或删除服务提供者



**特性**

* ‎基于透明接口的 RPC‎
* ‎智能负载均衡‎
* ‎自动服务注册和发现‎
* 灵活配置‎
* 高可扩展性‎
* 不同身份仅加载各自所需的部件, 不额外占用资源





## 架构

**总结构**

![总流程.png](https://s2.loli.net/2022/04/11/W5FNAdu9rc1qeoj.png)



**类部件**

![Alpaca.png](https://s2.loli.net/2022/04/11/HlPtYN4ywpCm9vc.png)





## 快速开始

### 依赖

```xml
<dependency>
      <groupId>io.gitee.birds-of-prey</groupId>
      <artifactId>alpaca</artifactId>
      <version>1.0.2</version>
</dependency>
```



### 步骤

#### 1 定义接口

```java
package com.lyb.api.service;
public interface HelloService {
    String hello(String name);
}
```



#### 2 Provider实现接口

```java
package com.lyb.provider.service.impl;

import com.lyb.api.service.HelloService;
import io.gitee.birdsofprey.annotation.ProviderService;

// 暴露服务
@ProviderService
public class HelloServiceImpl implements HelloService {
    @Override
    public String hello(String name) {
        return "I am Mr.Alpaca, Hello " + name;
    }
}
```



#### 3 Consumer调用服务

```java
package com.lyb.consumer.controller;

import com.lyb.api.service.HelloService;
import io.gitee.birdsofprey.annotation.ConsumerService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class IndexController {
    // 标志为远程服务类
    @ConsumerService
    private HelloService service;

    @GetMapping("/hello")
    public String testHello(String name) {
        return service.hello(name);
    }
}
```



#### 4 配置服务

```sh
# Provider
server.port=8080
server.servlet.context-path=/
alpaca.identity=provider
alpaca.registry-url=127.0.0.1:2181
alpaca.net-port=8081

# Consumer
server.port=9000
server.servlet.context-path=/
alpaca.identity=consumer
alpaca.registry-url=127.0.0.1:2181
```



#### 5 启动注册中心/服务

```shell
# zookeeper
sh bin/zkServer.sh start
# redis
redis-server
```



**Provider**

```java
package com.lyb.provider;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ProviderApplication {
    public static void main(String[] args)  {
        SpringApplication.run(ProviderApplication.class, args);
    }
}
```



**Consumer**

```java
package com.lyb.consumer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ConsumerApplication {
    public static void main(String[] args) {
        SpringApplication.run(ConsumerApplication.class, args);
    }
}
```



#### 6 访问服务

​	访问 http://ip:9000/hello?name=Tester 成功返回

> I am Mr.Alpaca, Hello Tester





## 框架详情

### 配置文件

​	Alpaca 支持可自定义配置的属性, 不同要求仅需通过配置文件定义即可实现无缝切换, 现支持如下:

* 注册中心类型: Zookeeper(默认), Redis
* 网络通信序列化方式: Hessian(默认), Kyro, FastJson, JDK 原生
* 负载均衡策略: 随机策略(默认), 轮询, 权重随机, 最快响应

```properties
# 身份: provider/consumer
alpaca.identity=provider
# 注册中心类型: zookeeper(默认)/redis
alpaca.registry-type=zookeeper
# 注册中心url: ip:port
alpaca.registry-url=127.0.0.1:2181
# 网络通信使用端口(provider)
alpaca.net-port=8099
# 序列化方式: hessian(默认)/kyro/fastjson/jdk
alpaca.serializer=hessian
# 节点权重(provider): 用于weight-random算法(默认10)
alpaca.weight=10
# 负载均衡策略(consumer): random(默认)/round/weight-random/fastest-response
alpaca.load-balance-strategy=random
```



**示例**

```properties
# 服务提供者
alpaca.identity=provider
alpaca.registry-type=zookeeper
alpaca.registry-url=127.0.0.1:2181
alpaca.serializer=hessian
alpaca.net-port=8099
alpaca.weight=10
# 简化版
alpaca.identity=provider
alpaca.registry-url=127.0.0.1:2181
alpaca.net-port=8099

# 服务消费者
alpaca.identity=consumer
alpaca.registry-type=zookeeper
alpaca.registry-url=127.0.0.1:2181
alpaca.serializer=hessian
alpaca.load-balance-strategy=random
# 简化版
alpaca.identity=consumer
alpaca.registry-url=127.0.0.1:2181
```



### 自定义扩展

​	Alpaca 支持根据自身需求自定义模块实现, 开发者仅需要直接实现规定接口并锚定应用名称即可, 现支持扩展点如下:

* 序列化器:  **AlpacaSerializer**
* 注册中心: **AlpacaRegistry**

* 负载均衡策略: **AlpacaStrategy**



#### 序列化器

​	Alpaca Serializer 用于对传输对象实体进行序列化以实现网络传输, 内置多种序列化方式并支持通过配置文件指定启动的类型, 开发者如有需求仅需通过 2 步实现



**1 实现 AlpacaSerializer 接口**

```java
/**
 * 序列化接口
 * 所有自定义的提供序列化能力的类都应实现该接口
 *
 * @author Mr.Alpaca
 * @version 1.0.0
 */
public interface AlpacaSerializer {
    /**
     * 序列化
     *
     * @param object 对象
     * @return 字节数组
     * @throws IOException 异常
     */
    byte[] serialize(Object object) throws IOException;

    /**
     * 反序列化
     *
     * @param clazz 接口
     * @param data  对象
     * @param <T>   泛型
     * @return 接口对象
     */
    <T> T deserialze(Class<T> clazz, byte[] data) throws IOException, ClassNotFoundException;
}
```



**2 添加实例到 AlpacaSerializerOptions 枚举类**

```java
/**
 * 序列化器枚举类
 *
 * @author Mr.Alpaca
 * @version 1.0.2
 */
public enum AlpacaSerializerOptions {
    // 加入自定义实例
    DEFAULT("default", new HessianSerializer()),
    HESSIAN("hessian", new HessianSerializer()),
    KYRO("kyro", new KyroSerializer()),
    FAST_JSON("fastjson", new FastJsonSerializer()),
    JDK_JSON("jdk", new JDKSerializer());

    /**
	 * 框架根据name加载对应的serializer
 	*/
    private final String name;
    private final AlpacaSerializer serializer;

    AlpacaSerializerOptions(String name, AlpacaSerializer serializer) {
        this.name = name;
        this.serializer = serializer;
    }

    public AlpacaSerializer getSerializer() {
        return serializer;
    }

    public String getName() {
        return name;
    }
}
```



#### 注册中心

​	Alpaca 现支持使用 Zookeeper 和 Redis 作为注册中心, 默认 Zookeeper, 开发者可使用其他框架作为注册中心如 MultiCast, Nacos 等, 同样仅需 2 步即可实现自定义扩展



**1 实现 AlpacaRegistry 接口**

```java
/**
 * 注册中心接口
 *
 * @author Mr.Alpaca
 * @version 1.0.2
 */
public interface AlpacaRegistry {
    /**
     * 注册服务
     *
     * @param serviceName 服务名
     */
    void registerService(String serviceName);

    /**
     * 获取服务提供者invoker列表
     *
     * @param serviceName 服务名
     * @return List<AlpacaInvoker>
     */
    List<AlpacaInvoker> getServiceInvokers(String serviceName);

    /**
     * 注册身份
     *
     * @param serviceName 服务名
     * @param identity    provider/consumer
     */
    void registerIdentity(String serviceName, String identity);
}
```



**2 添加示例到自动配置类 AutoConfig** 

```java
@Slf4j
@Configuration
public class AlpacaConfig {
    /**
     * 自定义注册中心配置逻辑
     *
     * @param alpacaProperties alpaca配置
     * @param serverProperties springboot配置
     * @return AlpacaRegistry 注册中心实例
     */
    @Bean
    public AlpacaRegistry alpacaRegistry(
		// ......
    }
}
```



#### 负载均衡

​	Alpaca 提供了 4 中内置的负载均衡算法 **Random**, **Round**, **Weight-Random**, **Fastest-Response**, 开发者可通过以下步骤扩展



**1 实现 AlpacaStrategy 接口**

```java
/**
 * 负载均衡接口
 *
 * @author Mr.Alpaca
 * @version 1.0.2
 */
public interface AlpacaStrategy {
    /**
     * 根据负载均衡策略选出特定的Invoker
     *
     * @param invokerList Invoker列表
     * @return AlpacaInvoker
     */
    AlpacaInvoker loadBalance(List<AlpacaInvoker> invokerList);
}

```



**2 添加实例到 AlpacaStrategyOptions 枚举类**

```java
/**
 * 负载均衡策略枚举器
 *
 * @author Mr.Alpaca
 * @version 1.0.2
 */
public enum AlpacaStrategyOptions {
    // 添加自定义实例
    DEFAULT("default", new RandomStrategy()),
    RANDOM("random", new RandomStrategy()),
    ROUND("round", new RoundStrategy()),
    WEIGHT_RANDOM("weight-random", new WeightRandomStrategy()),
    FASTEST_RESPONSE("fastest-response", new FastestResponseStrategy()),
    @Deprecated
    CONSISTENT_HASH("consistent-hash", new ConsistentHashStrategy());

    /**
	 * 框架根据name加载对应的strategy
 	*/
    private final String name;
    private final AlpacaStrategy strategy;

    AlpacaStrategyOptions(String name, AlpacaStrategy strategy) {
        this.name = name;
        this.strategy = strategy;
    }

    public String getName() {
        return name;
    }

    public AlpacaStrategy getStrategy() {
        return strategy;
    }
}

```



### 框架自身引用依赖

> 即使这只是一个简陋的玩具框架, 但留出该章节是为了表示对前辈们工作的感谢, 并提醒自己仍需努力

```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-configuration-processor</artifactId>
        <optional>true</optional>
        <version>2.3.7.RELEASE</version>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-autoconfigure</artifactId>
        <version>2.3.7.RELEASE</version>
    </dependency>
    <dependency>
        <groupId>io.netty</groupId>
        <artifactId>netty-all</artifactId>
        <version>4.1.39.Final</version>
    </dependency>
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <version>1.18.10</version>
    </dependency>
    <dependency>
        <groupId>com.google.guava</groupId>
        <artifactId>guava</artifactId>
        <version>19.0</version>
    </dependency>
    <dependency>
        <groupId>ch.qos.logback</groupId>
        <artifactId>logback-classic</artifactId>
        <version>1.2.6</version>
    </dependency>
    <dependency>
        <groupId>com.alibaba</groupId>
        <artifactId>fastjson</artifactId>
        <version>1.2.78</version>
    </dependency>
    <dependency>
        <groupId>com.101tec</groupId>
        <artifactId>zkclient</artifactId>
        <version>0.10</version>
    </dependency>
    <dependency>
        <groupId>redis.clients</groupId>
        <artifactId>jedis</artifactId>
        <version>2.9.0</version>
    </dependency>
    <dependency>
        <groupId>com.fasterxml.jackson.core</groupId>
        <artifactId>jackson-databind</artifactId>
        <version>2.13.0</version>
    </dependency>
    <dependency>
        <groupId>com.google.code.gson</groupId>
        <artifactId>gson</artifactId>
        <version>2.8.5</version>
    </dependency>
    <dependency>
        <groupId>com.esotericsoftware</groupId>
        <artifactId>kryo-shaded</artifactId>
        <version>4.0.0</version>
    </dependency>
    <dependency>
        <groupId>com.caucho</groupId>
        <artifactId>hessian</artifactId>
        <version>4.0.62</version>
    </dependency>
</dependencies>
```





## 基准测试

* 参照
    * 使用同为 rpc 框架的 dubbo 示例
* 测试类型
    * 测试 1: 请求 String 并原样返回
    * 测试 2: 请求 pojo 并原样返回
    * 测试 3: 混合请求 String + pojo 并原样返回
* 性能指标
    *  $TPS = n / t$
    * 请求 n 次, 耗时 t 秒
* 结果格式
    * TPS-耗时
    * 例: `41500-0.0338` 表示测试时长为 0.0338s, 平均 TPS 为 41500



**结果**

> Alpaca

| 请求次数 |     测试 1     |     测试 2     |     测试 3     |
| :------: | :------------: | :------------: | :------------: |
|   100    |  41500-0.0338  |  44999-0.0263  |  49000-0.0445  |
|   1000   |  46274-0.2242  |  46771-0.2199  |  46526-0.4353  |
|  10_000  |  44651-2.2373  |  45107-2.2373  |  46721-4.2860  |
| 100_000  | 43869-22.9185  | 40168-24.9939  | 38172-52.5001  |
| 1000_000 | 39472-253.4669 | 38104-262.4530 | 37422-535.0844 |

> dubbo

| 请求次数 |     测试 1     |     测试 2     |     测试 3     |
| :------: | :------------: | :------------: | :------------: |
|   100    |  5363-0.3919   |  5901-0.1868   |  7397-0.2816   |
|   1000   |  21284-0.5242  |  21928-0.4706  |  28797-0.7307  |
|  10_000  |  46385-2.6013  |  54777-1.8742  |  65484-3.0690  |
| 100_000  |  66198-15.328  | 63716-15.7487  | 57532-35.0569  |
| 1000_000 | 61639-162.2954 | 61393-163.1888 | 62901-317.9762 |

> 对比图

![基准测试.png](https://s2.loli.net/2022/04/14/U6sLguOVoxSc5wZ.png)





## 优劣&改进

**优点**

* 轻量级, 使用透明
* 网络传输负担小
* 多线程情况下不同线程的执行结果隔离, 且耗时长的任务不会阻塞耗时短的
* 使用配置文件即可灵活切换不同组件如序列化方式、注册中心等



**不足**

1. 没有实现鲁棒性强的通信协议
2. 没有容灾、超时重传、路由控制等机制
3. 没有类似于 Dubbo 一样的监控中心
4. 负载均衡由于设计问题无法实现一致性哈希、平滑轮询等算法
5. 没有实现 Consumer 将 Provider url 缓存到本地的功能, 每次服务调用都必须从注册中心中获取 Provider 列表
6. 无法脱离 Springboot 等容器框架进行使用



**改进**

1. 改进不足
2. 坚持 "使用配置文件即可灵活切换不同组件如序列化方式、注册中心等", 个人觉得应当允许通过配置文件对大部分配置进行调整
3. 坚持不同身份程序仅加载使用到的部分, 尽可能节省资源 ( 轻量! )
4. 应当允许不使用 Springboot 的程序也使用该框架, 实现最小的额外消耗完成 RPC 需求
5. 服务暴露的安全问题, 应当仅允许经过授权的 Consumer 使用
6. Consumer 可以指定仅使用某一个或某一子集 Provider
7. 应当更充分利用异步机制, 复杂任务进行网络通信的线程池应当跟执行服务的线程分离开 ( 分离职责 )
8. 提供控制器机制允许实现对节点的流量控制、优雅退出等





## 联系方式

* 作者：Mr.Alpaca
* QQ：2031403630
* 邮箱：2031403630@qq.com

