package io.gitee.birdsofprey.listener;

import io.gitee.birdsofprey.annotation.ConsumerService;
import io.gitee.birdsofprey.config.DefaultConfig;
import io.gitee.birdsofprey.factory.ConsumerProxyFactory;
import io.gitee.birdsofprey.net.ConsumerNetManager;
import io.gitee.birdsofprey.registry.AlpacaRegistry;
import io.gitee.birdsofprey.serializer.AlpacaSerializer;
import io.gitee.birdsofprey.strategy.AlpacaStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import java.lang.reflect.Field;
import java.util.Objects;

/**
 * 消费端监听器, 负责将bean中使用了远程服务的指定类型的field赋值为动态代理对象
 * 管理消费者网络处理器 + 动态代理工厂
 *
 * @author Mr.Alpaca
 * @version 1.0.2
 */
@Slf4j
public class ConsumerListener implements AlpacaListener, ApplicationListener<ContextRefreshedEvent> {
    /**
     * 注册中心
     */
    private final AlpacaRegistry registry;
    /**
     * 消费者网络资源
     */
    private final ConsumerNetManager netManager;
    /**
     * 消费者代理工厂
     */
    private final ConsumerProxyFactory factory;

    public ConsumerListener(AlpacaRegistry registry, AlpacaSerializer serializer, AlpacaStrategy strategy) {
        this.registry = registry;
        this.netManager = new ConsumerNetManager(registry, serializer, strategy);
        this.factory = new ConsumerProxyFactory(netManager);
    }

    /**
     * 刷新时获取注射service
     *
     * @param event 事件
     */
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        ApplicationContext context = event.getApplicationContext();
        if (Objects.isNull(context.getParent())) {
            injectService(context);
        }
    }

    /**
     * 注入服务
     *
     * @param context ApplicationContext
     */
    private void injectService(ApplicationContext context) {
        // 遍历容器所有bean, 若当前bean的某一属性使用了@ClientService注解则赋值为代理对象
        String[] beanNames = context.getBeanDefinitionNames();
        for (String beanName : beanNames) {
            Class<?> clazz = context.getType(beanName);
            if (Objects.isNull(clazz)) {
                continue;
            }
            // 遍历对象属性
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                ConsumerService annotation = field.getAnnotation(ConsumerService.class);
                if (Objects.isNull(annotation)) {
                    continue;
                }
                Object bean = context.getBean(beanName);
                Class<?> fieldType = field.getType();
                field.setAccessible(true);
                try {
                    // 属性赋值
                    field.set(bean, factory.getProxy(fieldType));
                    // 注册中心对应服务下注册身份
                    registry.registerIdentity(fieldType.getName(), DefaultConfig.CONSUMER);
                } catch (IllegalAccessException e) {
                    log.warn("Fail to inject service, bean.name: {}, error.msg: {}", beanName, e.getMessage());
                }
            }
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
