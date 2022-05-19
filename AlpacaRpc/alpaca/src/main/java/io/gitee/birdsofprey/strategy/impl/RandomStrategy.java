package io.gitee.birdsofprey.strategy.impl;

import io.gitee.birdsofprey.net.AlpacaInvoker;
import io.gitee.birdsofprey.strategy.AlpacaStrategy;

import java.util.List;
import java.util.Random;

/**
 * 完全随机策略
 *
 * @author Mr.Alpaca
 * @version 1.0.2
 */
public class RandomStrategy implements AlpacaStrategy {
    private static final Random RANDOM = new Random();

    @Override
    public AlpacaInvoker loadBalance(List<AlpacaInvoker> invokerList) {
        return invokerList.get(RANDOM.nextInt(invokerList.size()));
    }
}
