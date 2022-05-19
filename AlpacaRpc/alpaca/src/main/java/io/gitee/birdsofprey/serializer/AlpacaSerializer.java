package io.gitee.birdsofprey.serializer;

import java.io.IOException;

/**
 * 序列化接口
 * 所有自定义的提供序列化能力的类都应实现该接口
 *
 * @author Mr.Alpaca
 * @version 1.0.2
 */
public interface AlpacaSerializer {
    /**
     * 序列化
     *
     * @param object 对象
     * @return 字节数组
     * @throws IOException 异常
     */
    byte[] serialize(Object object) throws IOException;

    /**
     * 反序列化
     *
     * @param clazz 接口
     * @param data  对象
     * @param <T>   泛型
     * @return 接口对象
     */
    <T> T deserialze(Class<T> clazz, byte[] data) throws IOException, ClassNotFoundException;
}
