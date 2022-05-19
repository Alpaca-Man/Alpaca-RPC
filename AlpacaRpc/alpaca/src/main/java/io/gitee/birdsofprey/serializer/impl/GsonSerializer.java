package io.gitee.birdsofprey.serializer.impl;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import io.gitee.birdsofprey.serializer.AlpacaSerializer;

import java.io.IOException;

/**
 * Gson序列化(不完善)
 *
 * @author Mr.Alpaca
 * @version 1.0.2
 */
@Deprecated
public class GsonSerializer implements AlpacaSerializer {
    @Override
    public byte[] serialize(Object object) throws IOException {
        // 注册转换器
        return new GsonBuilder().create().toJson(object).getBytes();
    }

    @Override
    public <T> T deserialze(Class<T> clazz, byte[] data) throws IOException, ClassNotFoundException {
//        Gson gson = new GsonBuilder().
//                registerTypeAdapter(Class.class, new ClassCodec())
//                .create();
        return new Gson().fromJson(new String(data), new TypeToken<T>() {
        }.getType());
    }
}