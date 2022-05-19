package io.gitee.birdsofprey.strategy;

import io.gitee.birdsofprey.strategy.impl.*;

/**
 * 负载均衡策略枚举器
 *
 * @author Mr.Alpaca
 * @version 1.0.2
 */
public enum AlpacaStrategyOptions {
    /**
     * 默认使用随机
     */
    DEFAULT("default", new RandomStrategy()),
    /**
     * 随机
     */
    RANDOM("random", new RandomStrategy()),
    /**
     * 轮询
     */
    ROUND("round", new RoundStrategy()),
    /**
     * 权重随机
     */
    WEIGHT_RANDOM("weight-random", new WeightRandomStrategy()),
    /**
     * 最快响应轮询
     */
    FASTEST_RESPONSE("fastest-response", new FastestResponseStrategy()),
    /**
     * 一致性哈希
     */
    @Deprecated
    CONSISTENT_HASH("consistent-hash", new ConsistentHashStrategy());

    /**
     * 策略标识
     */
    private final String name;
    /**
     * 策略器
     */
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
