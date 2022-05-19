package io.gitee.birdsofprey.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 消费者端, 用于标致哪些接口调用远程服务
 *
 * @author Mr.Alpaca
 * @version 1.0.2
 * TODO: 版本之类的附加信息
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface ConsumerService {
    /**
     * 调用的远程服务名
     */
    String value() default "";
}
