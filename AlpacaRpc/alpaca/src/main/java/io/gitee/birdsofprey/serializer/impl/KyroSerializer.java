package io.gitee.birdsofprey.serializer.impl;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import io.gitee.birdsofprey.serializer.AlpacaSerializer;
import lombok.extern.slf4j.Slf4j;
import org.objenesis.strategy.StdInstantiatorStrategy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Kyro序列化
 *
 * @author Mr.Alpaca
 * @version 1.0.2
 */
@Slf4j
public class KyroSerializer implements AlpacaSerializer {
    private static final ThreadLocal<Kryo> KRYO_LOCAL = ThreadLocal.withInitial(() -> {
        Kryo kryo = new Kryo();
        kryo.setInstantiatorStrategy(new Kryo.DefaultInstantiatorStrategy(
                new StdInstantiatorStrategy()));
        return kryo;
    });

    @Override
    public byte[] serialize(Object object) throws IOException {
        byte[] result;
        try (
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                Output output = new Output(byteArrayOutputStream)
        ) {
            Kryo kryo = KRYO_LOCAL.get();
            // Object->byte:将对象序列化为byte数组
            kryo.writeObject(output, object);
            KRYO_LOCAL.remove();
            result = output.toBytes();
        } catch (Exception e) {
            log.debug("对象 {} 序列化出现异常 {}", object, e.getCause().getMessage());
            throw e;
        }
        return result;
    }

    @Override
    public <T> T deserialze(Class<T> clazz, byte[] data) throws IOException, ClassNotFoundException {
        T result;
        try (
                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
                Input input = new Input(byteArrayInputStream)
        ) {
            Kryo kryo = KRYO_LOCAL.get();
            Object o = kryo.readObject(input, clazz);
            KRYO_LOCAL.remove();
            result = clazz.cast(o);
        }
        return result;
    }
}
