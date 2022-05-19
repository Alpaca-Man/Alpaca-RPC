package io.gitee.birdsofprey.registry.impl;

import io.gitee.birdsofprey.config.DefaultConfig;
import io.gitee.birdsofprey.registry.AlpacaRegistry;
import io.gitee.birdsofprey.net.AlpacaInvoker;
import org.I0Itec.zkclient.ZkClient;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * Zookeeper注册中心
 * Provider: 路径=/alpaca/服务名/provider/ip:port 叶子节点data=权重
 * Consumer: 路径=/alpaca/服务名/consumer/ip:port 叶子节点data=权重(无用)
 * 父节点为永久节点,叶子节点为临时节点,服务断开时自动删除
 *
 * @author Mr.Alpaca
 * @version 1.0.2
 */
public class ZookeeperRegistry implements AlpacaRegistry {
    /**
     * 客户端
     */
    private final ZkClient client;
    /**
     * 根路径
     */
    private final String root = DefaultConfig.ROOT_PATH;
    /**
     * 本地地址
     */
    private final String localAddress;
    /**
     * Provider权重
     */
    private final int weight;

    public ZookeeperRegistry(String url, int port, int weight) {
        this.client = new ZkClient(url, DefaultConfig.CONNECTION_TIME_OUT);
        try {
            this.localAddress = InetAddress.getLocalHost().getHostAddress() + ":" + port;
        } catch (UnknownHostException e) {
            throw new ExceptionInInitializerError(e);
        }
        this.weight = weight;
    }

    /**
     * 注册服务
     *
     * @param serviceName 服务名
     */
    @Override
    public void registerService(String serviceName) {
        registerIdentity(serviceName, DefaultConfig.PROVIDER);
    }

    /**
     * 获取服务提供者invoker列表
     *
     * @param serviceName 服务名
     * @return List<AlpacaInvoker>
     */
    @Override
    public List<AlpacaInvoker> getServiceInvokers(String serviceName) {
        serviceName = serviceName.replace(".", "/");
        String path = root + "/" + serviceName + "/" + DefaultConfig.PROVIDER;
        List<String> urlList = client.getChildren(path);
        List<AlpacaInvoker> invokerList = new ArrayList<>(urlList.size());
        for (String url : urlList) {
            int nodeWeight = client.readData(path + "/" + url);
            invokerList.add(new AlpacaInvoker(url, nodeWeight));
        }
        return invokerList;
    }

    /**
     * 注册身份
     * 永久路径 /alpaca/服务名/身份
     * 临时节点 永久路径/url
     *
     * @param serviceName 服务名
     * @param identity    身份
     */
    @Override
    public void registerIdentity(String serviceName, String identity) {
        serviceName = serviceName.replace(".", "/");
        StringBuilder builder = new StringBuilder();
        // 永久路径: /alpaca/服务名/provider or consumer
        builder.append(root).append("/").append(serviceName)
                .append("/").append(identity);
        if (!client.exists(builder.toString())) {
            client.createPersistent(builder.toString(), true);
        }
        // 临时节点: 永久路径/ip:port
        builder.append("/").append(localAddress);
        if (!client.exists(builder.toString())) {
            client.createEphemeral(builder.toString(), weight);
        }
    }
}
