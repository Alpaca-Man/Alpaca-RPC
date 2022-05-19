package io.gitee.birdsofprey.serializer.impl;

import io.gitee.birdsofprey.serializer.AlpacaSerializer;
import lombok.extern.slf4j.Slf4j;

import java.io.*;

/**
 * JDK原生序列化
 *
 * @author Mr.Alpaca
 * @version 1.0.2
 */
@Slf4j
public class JDKSerializer implements AlpacaSerializer {
    @Override
    public byte[] serialize(Object object) throws IOException {
        byte[] result = null;
        try (
                ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
                ObjectOutputStream stream = new ObjectOutputStream(byteStream)
        ) {
            stream.writeObject(object);
            result = byteStream.toByteArray();
        } catch (IOException e) {
            log.debug("对象 {} 序列化出现异常 {}", object, e.getCause().getMessage());
            throw e;
        }
        return result;
    }

    @Override
    public <T> T deserialze(Class<T> clazz, byte[] data) throws IOException, ClassNotFoundException {
        T result = null;
        try (
                ByteArrayInputStream byteStream = new ByteArrayInputStream(data);
                ObjectInputStream stream = new ObjectInputStream(byteStream)
        ) {
            result = clazz.cast(stream.readObject());
        } catch (IOException | ClassNotFoundException e) {
            log.debug("类型 {} 反序列化出现异常 {}", clazz, e.getCause().getMessage());
            throw e;
        }
        return result;
    }
}
