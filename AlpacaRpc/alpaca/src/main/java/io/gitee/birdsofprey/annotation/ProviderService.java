package io.gitee.birdsofprey.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 提供者端, 用于描述哪些类被作为远程服务
 *
 * @author Mr.Alpaca
 * @version 1.0.2
 * TODO: 版本之类的附加信息
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Component
public @interface ProviderService {
    /**
     * 服务名, 默认为接口的全限定名称
     */
    String value() default "";
}
