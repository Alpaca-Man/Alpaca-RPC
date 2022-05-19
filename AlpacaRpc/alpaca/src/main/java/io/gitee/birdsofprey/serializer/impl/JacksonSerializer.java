package io.gitee.birdsofprey.serializer.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.gitee.birdsofprey.serializer.AlpacaSerializer;

import java.io.IOException;

/**
 * Jackson序列化(不完善)
 *
 * @author Mr.Alpaca
 * @version 1.0.2
 */
@Deprecated
public class JacksonSerializer implements AlpacaSerializer {
    @Override
    public byte[] serialize(Object object) throws IOException {
        return new ObjectMapper().writeValueAsBytes(object);
    }

    @Override
    public <T> T deserialze(Class<T> clazz, byte[] data) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(data, clazz);
    }
}
