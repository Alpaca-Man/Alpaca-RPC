package io.gitee.birdsofprey.strategy.impl;

import io.gitee.birdsofprey.net.AlpacaInvoker;
import io.gitee.birdsofprey.strategy.AlpacaStrategy;

import java.util.List;
import java.util.Random;

/**
 * 权重随机策略
 * 每个服务提供者都有各自的权重, 支持不同权重的节点多次调用尽可能均匀
 * 例:
 * a节点: 4; b节点: 2; c节点: 1
 * 调用7次实现 {a, a, b, a, c, b, a}
 * 而不是 {a, a, a, a, b, b, c}
 *
 * @author Mr.Alpaca
 * @version 1.0.2
 */
public class WeightRandomStrategy implements AlpacaStrategy {
    private static final Random RANDOM = new Random();

    @Override
    public AlpacaInvoker loadBalance(List<AlpacaInvoker> invokerList) {
        boolean same = true;
        int total = 0;
        int size = invokerList.size();
        for (int i = 0; i < size; i++) {
            int weight = invokerList.get(i).getWeight();
            total += weight;
            // 不同权重设置标志位
            if (same && i > 0 && weight != invokerList.get(i - 1).getWeight()) {
                same = false;
            }
        }
        // 不同权重按权重划分
        if (total > 0 && !same) {
            int index = RANDOM.nextInt(total);
            for (AlpacaInvoker invoker : invokerList) {
                index -= invoker.getWeight();
                if (index < 0) {
                    return invoker;
                }
            }
        }
        // 相同权重直接随机
        return invokerList.get(RANDOM.nextInt(size));
    }
}
