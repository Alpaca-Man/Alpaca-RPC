package io.gitee.birdsofprey.net;

import io.gitee.birdsofprey.protocol.AlpacaResponse;
import io.netty.util.concurrent.Promise;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 异步对象存储器
 *
 * @author Mr.Alpaca
 * @version 1.0.2
 */
public class AlpacaContext {
    /**
     * 结果Map, 存储请求与结果的对应关系
     * key: 请求编号, value: 结果Promise
     */
    private static final Map<Integer, Promise<AlpacaResponse>> resultMap;
    /**
     * 请求编号, 每个请求的编号单调递增
     */
    private static final AtomicInteger counter;

    static {
        resultMap = new ConcurrentHashMap<>(16);
        counter = new AtomicInteger(0);
    }

    /**
     * 获取请求编号
     *
     * @return 编号
     */
    public static int getId() {
        return counter.getAndIncrement();
    }

    /**
     * 设置请求编号与结果的映射
     *
     * @param id      请求编号
     * @param promise 结果事件
     */
    public static void setPromise(Integer id, Promise<AlpacaResponse> promise) {
        resultMap.put(id, promise);
    }

    /**
     * 根据编号获取异步事件
     *
     * @param id 请求编号
     * @return 存储结果的事件
     */
    public static Promise<AlpacaResponse> getPromise(Integer id) {
        final Promise<AlpacaResponse> promise = resultMap.get(id);
        resultMap.remove(id);
        return promise;
    }
}
