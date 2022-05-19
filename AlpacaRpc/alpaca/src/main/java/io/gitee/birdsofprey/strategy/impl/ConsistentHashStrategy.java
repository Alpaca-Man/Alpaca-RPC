package io.gitee.birdsofprey.strategy.impl;

import io.gitee.birdsofprey.net.AlpacaInvoker;
import io.gitee.birdsofprey.strategy.AlpacaStrategy;

import java.util.*;

/**
 * 一致性哈希策略(不完善)
 *
 * @author Mr.Alpaca
 * @version 1.0.2
 * TODO: 接口传入时并没有传入本机ip或者方法名等可以用来标识的参数
 */
@Deprecated
public class ConsistentHashStrategy implements AlpacaStrategy {
    private static final Map<Integer, Selector> SELECTOR_MAP = new HashMap<>(16);

    @Override
    public AlpacaInvoker loadBalance(List<AlpacaInvoker> invokerList) {
        int hash = Objects.hash(invokerList);
        return null;
    }

    public static void main(String[] args) {

    }
}

@Deprecated
class Selector {
//    private final TreeMap<Integer, AlpacaInvoker> virtualInvokers;
//    private final int nodeNumber;


}
