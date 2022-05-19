package io.gitee.birdsofprey.strategy;

import io.gitee.birdsofprey.net.AlpacaInvoker;

import java.util.List;

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
