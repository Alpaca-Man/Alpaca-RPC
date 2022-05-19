package io.gitee.birdsofprey.factory;

import io.gitee.birdsofprey.net.AlpacaContext;
import io.gitee.birdsofprey.net.ConsumerNetManager;
import io.gitee.birdsofprey.protocol.AlpacaRequest;
import io.gitee.birdsofprey.protocol.AlpacaResponse;
import io.netty.util.concurrent.Promise;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 客户对象代理工厂, 生成客户端rpc服务代理对象
 *
 * @author Mr.Alpaca
 * @version 1.0.2
 */
public class ConsumerProxyFactory {
    /**
     * 网络管理器
     */
    private final ConsumerNetManager netManager;
    /**
     * 代理对象map
     */
    private final Map<Class<?>, Object> proxyMap;

    public ConsumerProxyFactory(ConsumerNetManager netManager) {
        this.netManager = netManager;
        this.proxyMap = new HashMap<>(16);
    }

    /**
     * 动态代理获取代理对象
     * 若未生成该代理对象则直接生成并存储进proxyMap中
     * 若有同类型代理对象则直接返回
     *
     * @param serviceInterfaceClazz 接口
     * @param <T>                   对象泛型
     * @return 代理对象
     */
    @SuppressWarnings("unchecked")
    public <T> T getProxy(Class<T> serviceInterfaceClazz) {
        // 若已有代理对象则直接返回
        Object result = proxyMap.get(serviceInterfaceClazz);
        if (Objects.nonNull(result)) {
            return (T) result;
        }
        // 生成代理对象, 添加到map中再返回
        result = Proxy.newProxyInstance(serviceInterfaceClazz.getClassLoader(), new Class[]{serviceInterfaceClazz}, (proxy, method, args) -> {
            // 构造请求对象
            AlpacaRequest request = buildRequest(serviceInterfaceClazz, method, args);

            // 发送请求消息并返回一个promise用于接收消息
            Promise<AlpacaResponse> promise = netManager.send(request).sync();
            if (!promise.isSuccess()) {
                throw new Exception(promise.cause());
            }
            // 获取响应结果
            AlpacaResponse response = promise.getNow();
            if (Objects.nonNull(response.getExceptionMessage())) {
                throw new Exception(response.getExceptionMessage());
            }
            return response.getReturnValue();
        });
        proxyMap.put(serviceInterfaceClazz, result);
        return (T) result;
    }

    /**
     * 构造请求对象
     *
     * @param serviceInterfaceClazz 类型
     * @param method                方法
     * @param args                  参数
     * @return AlpacaRequest
     */
    private AlpacaRequest buildRequest(Class<?> serviceInterfaceClazz, Method method, Object[] args) {
        // 分配id
        Integer id = AlpacaContext.getId();
        String interfaceName = serviceInterfaceClazz.getName();
        String methodName = method.getName();
        Class<?>[] parameterTypes = method.getParameterTypes();
        return new AlpacaRequest(id, interfaceName, methodName, parameterTypes, args);
    }
}
