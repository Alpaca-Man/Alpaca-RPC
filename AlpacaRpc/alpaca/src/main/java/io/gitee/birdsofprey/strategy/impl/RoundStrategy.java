package io.gitee.birdsofprey.strategy.impl;

import io.gitee.birdsofprey.net.AlpacaInvoker;
import io.gitee.birdsofprey.strategy.AlpacaStrategy;

import java.util.List;

/**
 * 轮询策略
 * 按顺序依次访问invoker
 * 注意: 由于仅使用了ThreadLocal来记录线程已访问过的提供者索引
 * 所以来自同一consumer的请求(Netty使用同一线程处理)会使用同一个ThreadLocal,
 * 即使consumer请求的并不一定是同一个方法
 * 因此该轮询只保证:
 * 1.consumer连续调用同一服务会依次轮询提供该服务的provider
 * 2.consumer需求每次调用不同服务都可能使用不同provider
 *
 * @author Mr.Alpaca
 * @version 1.0.2
 */
public class RoundStrategy implements AlpacaStrategy {
    /**
     * 记录线程当前已轮询的位置
     */
    private final ThreadLocal<Integer> local = ThreadLocal.withInitial(() -> 0);

    @Override
    public AlpacaInvoker loadBalance(List<AlpacaInvoker> invokerList) {
        Integer index = local.get();
        // 防止index无限膨胀
        local.set((index + 1) % 65535);
        return invokerList.get(index % invokerList.size());
    }
}
