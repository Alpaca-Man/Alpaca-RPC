package io.gitee.birdsofprey.config;

import io.gitee.birdsofprey.listener.AlpacaListener;
import io.gitee.birdsofprey.listener.ConsumerListener;
import io.gitee.birdsofprey.listener.ProviderListener;
import io.gitee.birdsofprey.properties.AlpacaProperties;
import io.gitee.birdsofprey.registry.AlpacaRegistry;
import io.gitee.birdsofprey.registry.impl.RedisRegistry;
import io.gitee.birdsofprey.registry.impl.ZookeeperRegistry;
import io.gitee.birdsofprey.serializer.AlpacaSerializer;
import io.gitee.birdsofprey.serializer.AlpacaSerializerOptions;
import io.gitee.birdsofprey.strategy.AlpacaStrategy;
import io.gitee.birdsofprey.strategy.AlpacaStrategyOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Objects;

/**
 * 启动配置类
 *
 * @author Mr.Alpaca
 * @version 1.0.2
 */
@Slf4j
@Configuration
public class AlpacaConfig {
    /**
     * 配置文件类
     *
     * @return AlpacaProperties
     */
    @Bean
    public AlpacaProperties alpacaProperties() {
        return new AlpacaProperties();
    }

    /**
     * 监听器
     * 根据身份自动配置不同监听器
     *
     * @param properties 配置类
     * @param registry   注册中心
     * @param serializer 序列化器
     * @param strategy   负载均衡策略
     * @return AlpacaListener
     */
    @Bean
    public AlpacaListener alpacaListener(
            @Autowired AlpacaProperties properties,
            @Autowired AlpacaRegistry registry,
            @Autowired AlpacaSerializer serializer,
            @Autowired(required = false) AlpacaStrategy strategy
    ) {
        // 根据当前程序身份进行不同处理
        String identity = properties.getIdentity();
        Integer netPort = properties.getNetPort();
        AlpacaListener listener;
        // 消费者端
        if (DefaultConfig.CONSUMER.equals(identity)) {
            listener = new ConsumerListener(registry, serializer, strategy);
        } else { // 提供者端
            listener = new ProviderListener(netPort, registry, serializer);
        }
        log.info("启动监听器: {}", listener);
        return listener;
    }

    /**
     * 根据提供者/消费者提供不同的注册中心
     *
     * @param alpacaProperties 配置
     * @param serverProperties 配置
     * @return AlpacaRegistry
     */
    @Bean
    public AlpacaRegistry alpacaRegistry(
            @Autowired AlpacaProperties alpacaProperties,
            @Autowired ServerProperties serverProperties
    ) {
        String registryType = alpacaProperties.getRegistryType();
        String url = alpacaProperties.getRegistryUrl();
        Integer weight = alpacaProperties.getWeight();
        weight = Objects.isNull(weight) ? DefaultConfig.NODE_WEIGHT : weight;
        // provider的端口是进行网络通信的端口, consumer的端口是应用本身占用的端口
        int port = DefaultConfig.CONSUMER.equals(alpacaProperties.getIdentity()) ? serverProperties.getPort() : alpacaProperties.getNetPort();
        AlpacaRegistry registry;
        if (Objects.isNull(registryType) || DefaultConfig.ZOOKEEPER_REGISTER.equals(registryType)) {
            registry = new ZookeeperRegistry(url, port, weight);
        } else if (DefaultConfig.REDIS_REGISTRY.equals(registryType)) {
            registry = new RedisRegistry(url, port, weight);
        } else {
            // 不合法输入直接抛出错误
            throw new ExceptionInInitializerError("不支持注册中心类型[" + registryType + "]");
        }
        log.info("注册中心: {}", registry);
        return registry;
    }

    /**
     * 网络通信序列化器
     *
     * @param properties 配置
     * @return AlpacaSerializer
     */
    @Bean
    public AlpacaSerializer alpacaSerializer(@Autowired AlpacaProperties properties) {
        String serializer = properties.getSerializer();
        log.info("序列化方式: {}", Objects.isNull(serializer) ? "Default(Hessian)" : serializer);
        // 使用默认类型
        if (Objects.isNull(serializer)) {
            return AlpacaSerializerOptions.DEFAULT.getSerializer();
        }
        // 匹配序列化器
        for (AlpacaSerializerOptions options : AlpacaSerializerOptions.values()) {
            if (options.getName().equals(serializer)) {
                return options.getSerializer();
            }
        }
        // 不合法输入直接抛出错误
        throw new ExceptionInInitializerError("不支持序列化类型[" + serializer + "]");
    }

    /**
     * 负载均衡策略(Consumer)
     *
     * @param properties 配置
     * @return AlpacaStrategy
     */
    @Bean
    public AlpacaStrategy alpacaStrategy(@Autowired AlpacaProperties properties) {
        String identity = properties.getIdentity();
        // Provider不需要使用策略
        if (DefaultConfig.PROVIDER.equals(identity)) {
            return null;
        }
        String strategy = properties.getLoadBalanceStrategy();
        log.info("负载均衡策略: {}", Objects.isNull(strategy) ? "Default(random)" : strategy);
        // 使用默认类型
        if (Objects.isNull(strategy)) {
            return AlpacaStrategyOptions.DEFAULT.getStrategy();
        }
        // 匹配策略
        for (AlpacaStrategyOptions options : AlpacaStrategyOptions.values()) {
            if (options.getName().equals(strategy)) {
                return options.getStrategy();
            }
        }
        // 不合法输入直接抛出错误
        throw new ExceptionInInitializerError("不支持负载均衡策略[" + strategy + "]");
    }
}
