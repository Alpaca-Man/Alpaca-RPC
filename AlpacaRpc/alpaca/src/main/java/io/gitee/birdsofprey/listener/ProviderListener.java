package io.gitee.birdsofprey.listener;

import io.gitee.birdsofprey.annotation.ProviderService;
import io.gitee.birdsofprey.net.ProviderServiceContext;
import io.gitee.birdsofprey.net.ProviderNetManager;
import io.gitee.birdsofprey.registry.AlpacaRegistry;
import io.gitee.birdsofprey.serializer.AlpacaSerializer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import java.util.Objects;

/**
 * 服务提供者监听器
 * 持有provider的网络管理器
 *
 * @author Mr.Alpaca
 * @version 1.0.2
 */
public class ProviderListener implements AlpacaListener, ApplicationListener<ContextRefreshedEvent> {
    private final ProviderNetManager netManager;
    private final AlpacaRegistry registry;

    public ProviderListener(int netPort, AlpacaRegistry registry, AlpacaSerializer serializer) {
        this.netManager = new ProviderNetManager(netPort, serializer);
        this.registry = registry;
    }

    /**
     * 刷新时获取服务提供类
     *
     * @param event 事件
     */
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        ApplicationContext context = event.getApplicationContext();
        if (Objects.isNull(context.getParent())) {
            registerService(context);
        }
    }

    /**
     * 获取Spring容器中被@ServerService注解的bean放入serviceMap中
     * 并注册服务到注册中心
     *
     * @param context 容器
     */
    private void registerService(ApplicationContext context) {
        // 遍历容器中所有bean, 若被@ServerService注解则该bean提供远程服务
        String[] beanNames = context.getBeanDefinitionNames();
        for (String beanName : beanNames) {
            Class<?> clazz = context.getType(beanName);
            if (Objects.isNull(clazz)) {
                continue;
            }
            ProviderService annotation = clazz.getAnnotation(ProviderService.class);
            if (Objects.isNull(annotation)) {
                continue;
            }
            // 若bean继承多个接口则只取第一个接口名
            String interfaceName = clazz.getInterfaces()[0].getName();
            Object bean = context.getBean(beanName);
            // 注册服务到context和注册中心中
            ProviderServiceContext.registery(interfaceName, bean);
            registry.registerService(interfaceName);
        }
    }

    /**
     * 关闭网络资源
     */
    @Override
    public void close() {
        this.netManager.close();
    }
}
