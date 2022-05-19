package io.gitee.birdsofprey.registry;

import io.gitee.birdsofprey.net.AlpacaInvoker;

import java.util.List;

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
