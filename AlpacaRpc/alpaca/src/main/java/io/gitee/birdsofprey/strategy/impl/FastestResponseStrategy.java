package io.gitee.birdsofprey.strategy.impl;

import io.gitee.birdsofprey.net.AlpacaInvoker;
import io.gitee.birdsofprey.strategy.AlpacaStrategy;

import java.io.IOException;
import java.net.InetAddress;
import java.util.List;

/**
 * 最快响应策略
 * 选择所有提供者中响应速度最快的invoker(最快响应速度 != 最快处理速度)
 *
 * @author Mr.Alpaca
 * @version 1.0.2
 */
public class FastestResponseStrategy implements AlpacaStrategy {
    @Override
    public AlpacaInvoker loadBalance(List<AlpacaInvoker> invokerList) {
        long min = Integer.MAX_VALUE;
        AlpacaInvoker result = invokerList.get(0);
        try {
            for (AlpacaInvoker invoker : invokerList) {
                long start = System.currentTimeMillis();
                // 用ping命令来检查连通性以及响应速度
                boolean ping = InetAddress.getByName(invoker.getUrl()).isReachable(1000);
                long waste = System.currentTimeMillis() - start;
                if (ping && waste < min) {
                    min = waste;
                    result = invoker;
                }
            }
        } catch (IOException ignore) {
        }
        return result;
    }
}

