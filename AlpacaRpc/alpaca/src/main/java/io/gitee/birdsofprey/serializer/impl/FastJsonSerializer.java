package io.gitee.birdsofprey.serializer.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import io.gitee.birdsofprey.serializer.AlpacaSerializer;

import java.io.IOException;

/**
 * FastJson序列化
 *
 * @author Mr.Alpaca
 * @version 1.0.2
 */
public class FastJsonSerializer implements AlpacaSerializer {
    private static final String CHARSET_NAME = "UTF-8";

    static {
        ParserConfig.getGlobalInstance().setAutoTypeSupport(true);
    }

    @Override
    public byte[] serialize(Object object) throws IOException {
        return JSON.toJSONString(object, SerializerFeature.WriteClassName).getBytes(CHARSET_NAME);
    }

    @Override
    public <T> T deserialze(Class<T> clazz, byte[] data) throws IOException, ClassNotFoundException {
        return JSON.parseObject(new String(data), clazz);
    }
}
