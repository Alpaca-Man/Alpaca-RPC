# Alpaca

​	Alpaca is a lightweight, open source RPC framework based on Java language. It is committed to providing high-performance and transparent RPC remote service invocation scheme and SOA Service governance scheme, so that applications can realize the remote invocation function of services through Alpaca and integrate into springboot for application development

​	Alpaca consists of three core parts: **remote communication**, **service cluster** and **service discovery and registration**. It provides transparent remote method call, and calls remote methods like local methods. It only needs simple configuration without any API intrusion. At the same time, it has a soft load balancing mechanism to support automatic service registration and discovery. There is no need to write the address of the service provider. The registration center queries the IP address of the service provider based on the interface name, And can smoothly add or delete service providers



**Features**

* Transparent interface based RPC
* Intelligent load balancing
* Automatic service registration and discovery
* High extensibility
* Different identities only load the parts they need, and do not occupy additional resources





## Architecture

**Main**

![总流程.png](https://s2.loli.net/2022/04/11/W5FNAdu9rc1qeoj.png)

> Sorry, the picture bed service I use cannot upload the English version of this picture. I don't know why ???



**Components**

![Class components.png](https://s2.loli.net/2022/04/11/HlPtYN4ywpCm9vc.png)





## Getting started

### dependency

```xml
<dependency>
      <groupId>io.gitee.birds-of-prey</groupId>
      <artifactId>alpaca</artifactId>
      <version>1.0.2</version>
</dependency>
```



### Steps

#### 1 Define interfaces

```java
package com.lyb.api.service;
public interface HelloService {
    String hello(String name);
}
```



#### 2 Provider implement interface

```java
package com.lyb.provider.service.impl;

import com.lyb.api.service.HelloService;
import io.gitee.birdsofprey.annotation.ProviderService;

// Expose services
@ProviderService
public class HelloServiceImpl implements HelloService {
    @Override
    public String hello(String name) {
        return "I am Mr.Alpaca, Hello " + name;
    }
}
```



#### 3 Consumer invoke service

```java
package com.lyb.consumer.controller;

import com.lyb.api.service.HelloService;
import io.gitee.birdsofprey.annotation.ConsumerService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class IndexController {
    // Use remote service
    @ConsumerService
    private HelloService service;

    @GetMapping("/hello")
    public String testHello(String name) {
        return service.hello(name);
    }
}
```



#### 4 Configure

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



#### 5 Run registry/program

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



#### 6 Access services

​	Visit http://ip:9000/hello?name=Tester , Success will return

> I am Mr.Alpaca, Hello Tester





## Frame details

### Configure

​	Alpaca supports custom configurable attributes. Different requirements can be seamlessly switched only through the definition of the configuration file. Now it supports the following: 

* Registry Type: Zookeeper(default), Redis
* Network communication serialization mode: Hessian(defalut), Kyro, FastJson, JDK
* Load balancing strategy: Random(default), Round, Weight-Random, Fastest-Response

```properties
# provider/consumer
alpaca.identity=provider
# zookeeper(default)/redis
alpaca.registry-type=zookeeper
# url: ip:port
alpaca.registry-url=127.0.0.1:2181
# Port for provider network communication
alpaca.net-port=8099
# hessian(default)/kyro/fastjson/jdk
alpaca.serializer=hessian
# Provider node weight: used for weight-random algorithm (default 10)
alpaca.weight=10
# Load balancing strategy for consumer: random(default)/round/weight-random/fastest-response
alpaca.load-balance-strategy=random
```



**示例**

```properties
# Provider
alpaca.identity=provider
alpaca.registry-type=zookeeper
alpaca.registry-url=127.0.0.1:2181
alpaca.serializer=hessian
alpaca.net-port=8099
alpaca.weight=10
# Lite
alpaca.identity=provider
alpaca.registry-url=127.0.0.1:2181
alpaca.net-port=8099

# Consumer
alpaca.identity=consumer
alpaca.registry-type=zookeeper
alpaca.registry-url=127.0.0.1:2181
alpaca.serializer=hessian
alpaca.load-balance-strategy=random
# Lite
alpaca.identity=consumer
alpaca.registry-url=127.0.0.1:2181
```



### Custom extension

​	 Alpaca supports customized module implementation according to its own needs. Developers only need to directly implement the specified interface and anchor the application name. Now the supported extension points are as follows:

* Network communication serialization mode:  **AlpacaSerializer**
* Registry: **AlpacaRegistry**

* Load balancing strategy: **AlpacaStrategy**



#### Serializer

​	Alpaca Serializer is used to serialize the transport object entities to achieve network transmission, built-in various serialization methods, and support the type of startup that is specified through configuration files. Developers need only 2 steps to achieve them.



**1 Implement AlpacaSerializer interface**

```java
public interface AlpacaSerializer {
    byte[] serialize(Object object) throws IOException;
    <T> T deserialze(Class<T> clazz, byte[] data) throws IOException, ClassNotFoundException;
}
```



**2 Add an instance to the AlpacaSerializerOptions enumeration class**

```java
public enum AlpacaSerializerOptions {
    // Add instance
    DEFAULT("default", new HessianSerializer()),
    HESSIAN("hessian", new HessianSerializer()),
    KYRO("kyro", new KyroSerializer()),
    FAST_JSON("fastjson", new FastJsonSerializer()),
    JDK_JSON("jdk", new JDKSerializer());

    /**
	 * The framework loads the corresponding serializer according to name
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



#### Registry

​	Alpaca now supports zookeeper and redis as registration centers. By default, zookeeper is used. Developers can use other frameworks as registration centers, such as multicast and Nacos. Similarly, it only takes two steps to realize custom extension



**1 Implement AlpacaRegistry interface**

```java
public interface AlpacaRegistry {
    void registerService(String serviceName);
    List<AlpacaInvoker> getServiceInvokers(String serviceName);
    void registerIdentity(String serviceName, String identity);
}
```



**2 Add instance to autoconfig class AutoConfig** 

```java
@Slf4j
@Configuration
public class AlpacaConfig {
    @Bean
    public AlpacaRegistry alpacaRegistry(
		// ......
    }
}
```



#### load balancing

​	Alpaca provides the built-in load balancing algorithm in 4 by default: **Random**, **Round**, **Weight-Random**, **Fastest-Response**, Developers can extend the following steps



**1 implement AlpacaStrategy interface**

```java
public interface AlpacaStrategy {
    AlpacaInvoker loadBalance(List<AlpacaInvoker> invokerList);
}
```



**2 Add an instance to the AlpacaStrategyOptions enumeration class**

```java
public enum AlpacaStrategyOptions {
    // Add instance
    DEFAULT("default", new RandomStrategy()),
    RANDOM("random", new RandomStrategy()),
    ROUND("round", new RoundStrategy()),
    WEIGHT_RANDOM("weight-random", new WeightRandomStrategy()),
    FASTEST_RESPONSE("fastest-response", new FastestResponseStrategy()),
    @Deprecated
    CONSISTENT_HASH("consistent-hash", new ConsistentHashStrategy());

    /**
	 * The framework loads the corresponding serializer according to name
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



### Frame reference dependency

> Even though this is just a simple toy frame, this chapter is set aside to express my gratitude for the work of my predecessors and remind me that I still need to work hard

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





## Benchmark

* Reference
    * Use dubbo as the same way
* Type
    * Test 1: Request string and return as is
    * Test 2: Request pojo and return as is
    * Test  3: Mix request String + pojo and return as is
* Performance index
    *  $TPS = n / t$
    * Request n times, time-consuming T seconds
* Result format
    * TPS-time
    * example: `41500-0.0338` means the test duration is 0.0338s, average TPS is 41500



**Result**

> Alpaca

| Number of requests |     Test 1     |     Test 2     |     Test 3     |
| :----------------: | :------------: | :------------: | :------------: |
|        100         |  41500-0.0338  |  44999-0.0263  |  49000-0.0445  |
|        1000        |  46274-0.2242  |  46771-0.2199  |  46526-0.4353  |
|       10_000       |  44651-2.2373  |  45107-2.2373  |  46721-4.2860  |
|      100_000       | 43869-22.9185  | 40168-24.9939  | 38172-52.5001  |
|      1000_000      | 39472-253.4669 | 38104-262.4530 | 37422-535.0844 |

> dubbo

| Number of requests |     Test 1     |     Test 2     |     Test 3     |
| :----------------: | :------------: | :------------: | :------------: |
|        100         |  5363-0.3919   |  5901-0.1868   |  7397-0.2816   |
|        1000        |  21284-0.5242  |  21928-0.4706  |  28797-0.7307  |
|       10_000       |  46385-2.6013  |  54777-1.8742  |  65484-3.0690  |
|      100_000       |  66198-15.328  | 63716-15.7487  | 57532-35.0569  |
|      1000_000      | 61639-162.2954 | 61393-163.1888 | 62901-317.9762 |

> Comparison chart

![基准测试.png](https://s2.loli.net/2022/04/14/U6sLguOVoxSc5wZ.png)





## Advantages and Shortage & Improvement

**Advantages**

* Light, transparent
* Low network transmission burden
* In the case of multi-thread, the execution results of different threads are isolated, and the long-time tasks will not block the short-time tasks
* Using the configuration file, you can flexibly switch different components, such as serialization mode, registry, etc



**Shortage**

* No robust communication protocol is implemented
* There are no disaster-recovery, overtime retransmission, routing control and other mechanisms
* There is no monitoring center similar to Dubbo's
* Due to design problems, load balancing stategy cannot implement consistent hash, smooth polling and other algorithms
* The function of caching the provider URL locally by the consumer is not implemented. Each service call must obtain the provider list from the registry
* It cannot be used without Springboot and other container frameworks



**Improvement**

1. Insufficient improvement
2. Insist on "using the configuration file can flexibly switch different components, such as serialization method, registry, etc." I think it should be allowed to adjust most configurations through the configuration file
3. Insist that programs with different identities only load the parts used, so as to save resources as much as possible (lightweight!)
4. Programs that do not use Springboot should also be allowed to use the framework to achieve minimum additional consumption and complete RPC requirements
5. The security problems exposed by the service should only be allowed to be used by authorized consumers
6. Consumer can specify to use only one or a subset of providers
7. The asynchronous mechanism should be made full use of. The thread pool for network communication of complex tasks should be separated from the thread executing the service (separating responsibilities)
8. Provide a controller mechanism to allow flow control and graceful exit of nodes





## Contact

* Author：Mr.Alpaca
* QQ：2031403630
* Email：2031403630@qq.com

