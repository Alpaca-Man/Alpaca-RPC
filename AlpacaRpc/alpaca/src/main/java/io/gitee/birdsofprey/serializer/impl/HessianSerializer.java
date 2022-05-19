package io.gitee.birdsofprey.serializer.impl;

import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import io.gitee.birdsofprey.serializer.AlpacaSerializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Hessian2序列化
 *
 * @author Mr.Alpaca
 * @version 1.0.2
 */
public class HessianSerializer implements AlpacaSerializer {
    @Override
    public byte[] serialize(Object object) throws IOException {
        byte[] result;
        try (ByteArrayOutputStream byteStream = new ByteArrayOutputStream();) {
            Hessian2Output output = new Hessian2Output(byteStream);
            output.writeObject(object);
            output.flush();
            result = byteStream.toByteArray();
        }
        return result;
    }

    @Override
    public <T> T deserialze(Class<T> clazz, byte[] data) throws IOException, ClassNotFoundException {
        T result;
        try (ByteArrayInputStream byteStream = new ByteArrayInputStream(data)) {
            Hessian2Input input = new Hessian2Input(byteStream);
            result = clazz.cast(input.readObject());
            input.close();
        }
        return result;
    }
}
