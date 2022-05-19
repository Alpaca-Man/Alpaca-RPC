package io.gitee.birdsofprey.registry.impl;

import io.gitee.birdsofprey.config.DefaultConfig;
import io.gitee.birdsofprey.registry.AlpacaRegistry;
import io.gitee.birdsofprey.net.AlpacaInvoker;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Redis注册中心
 * 均以String key-value类型存储, 提供者每隔一段时间更新键生存时间, 而消费者则不需要
 * provider: key=服务名||provider||ip:port value=权重
 * consumer: key=服务名||consumer||ip:port value=权重(无用)
 *
 * @author Mr.Alpaca
 * @version 1.0.2
 */
public class RedisRegistry implements AlpacaRegistry {
    /**
     * Redis客户端
     */
    private final JedisPool jedisPool;
    /**
     * 本地地址
     */
    private final String localAddress;
    /**
     * 定时任务池, 每隔一段时间刷新服务提供者的过期时间(若服务宕机了则键过期)
     */
    private final ScheduledExecutorService executorService;
    /**
     * 键生存时间, 在定时任务中每隔ttlTime - 1刷新
     */
    private final int ttlTime;
    /**
     * 权重
     */
    private final int weight;

    public RedisRegistry(String url, int port, int weight) {
        String[] temp = url.split(":");
        this.jedisPool = new JedisPool(temp[0], Integer.parseInt(temp[1]));
        // 获取本地地址
        try {
            this.localAddress = InetAddress.getLocalHost().getHostAddress() + ":" + port;
        } catch (UnknownHostException e) {
            throw new ExceptionInInitializerError(e);
        }
        this.executorService = new ScheduledThreadPoolExecutor(DefaultConfig.CORE_POOL_SIZE, new ThreadPoolExecutor.DiscardPolicy());
        this.ttlTime = DefaultConfig.TTL_TIME;
        this.weight = weight;
    }

    /**
     * 注册服务
     *
     * @param serviceName 服务名
     */
    @Override
    public void registerService(String serviceName) {
        // 注册提供者
        String key = serviceName + "||" + DefaultConfig.PROVIDER + "||" + localAddress;
        try (Jedis client = jedisPool.getResource()) {
            client.set(key, String.valueOf(weight), "NX", "EX", ttlTime);
        }
        // 每ttlTime-1秒刷新键生存时间
        autoUpdate(key);
    }

    /**
     * 获取服务提供者invoker列表
     *
     * @param serviceName 服务名
     * @return List<AlpacaInvoker>
     */
    @Override
    public List<AlpacaInvoker> getServiceInvokers(String serviceName) {
        List<AlpacaInvoker> invokerList = new ArrayList<>();
        try (Jedis client = jedisPool.getResource()) {
            // 获取所有提供者
            String pattern = serviceName + "||" + DefaultConfig.PROVIDER + "||*";
            ArrayList<String> keyList = new ArrayList<>(client.keys(pattern));
            for (String key : keyList) {
                int nodeWeight = Integer.parseInt(client.get(key));
                // 清洗数据 服务名||provider||ip:port -> ip:port
                String url = key.substring(key.lastIndexOf("||") + 2);
                invokerList.add(new AlpacaInvoker(url, nodeWeight));
            }
        }
        return invokerList;
    }

    /**
     * 注册身份
     * 注意:
     * 1.只有consumer使用该方法
     * 2.provider在注册服务时自动生成有生存时间的键并交由定时任务池维护
     *
     * @param serviceName 服务名
     * @param identity    provider/consumer
     */
    @Override
    public void registerIdentity(String serviceName, String identity) {
        try (Jedis client = jedisPool.getResource()) {
            String consumerKey = serviceName + "|| " + identity + "||" + localAddress;
            client.set(consumerKey, String.valueOf(weight));
        }
    }

    /**
     * 自动更新, 每隔ttl-1秒重新设置服务提供者过期时间
     *
     * @param key 服务提供者
     */
    private void autoUpdate(String key) {
        Jedis client = jedisPool.getResource();
        executorService.scheduleAtFixedRate(() -> client.expire(key, ttlTime), 0, ttlTime - 1, TimeUnit.SECONDS);
    }
}
