package io.gitee.birdsofprey.net;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 提供服务的bean的容器
 *
 * @author Mr.Alpaca
 * @version 1.0.2
 */
public class ProviderServiceContext {
    /**
     * 服务Map
     * key=服务名 value=bean
     */
    private static final Map<String, Object> SERVICE_MAP = new ConcurrentHashMap<>(16);

    /**
     * 获取提供远程服务的bean
     *
     * @param interfaceName 接口名
     * @return bean
     */
    public static Object getService(String interfaceName) {
        return SERVICE_MAP.get(interfaceName);
    }

    /**
     * 注册服务
     *
     * @param interfaceName 接口名
     * @param serviceObject 服务对象
     */
    public static void registery(String interfaceName, Object serviceObject) {
        SERVICE_MAP.put(interfaceName, serviceObject);
    }
}
