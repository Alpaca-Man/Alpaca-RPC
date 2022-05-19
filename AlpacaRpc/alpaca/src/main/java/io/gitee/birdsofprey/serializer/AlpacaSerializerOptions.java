package io.gitee.birdsofprey.serializer;

import io.gitee.birdsofprey.serializer.impl.FastJsonSerializer;
import io.gitee.birdsofprey.serializer.impl.HessianSerializer;
import io.gitee.birdsofprey.serializer.impl.JDKSerializer;
import io.gitee.birdsofprey.serializer.impl.KyroSerializer;

/**
 * 序列化器枚举类
 *
 * @author Mr.Alpaca
 * @version 1.0.2
 */
public enum AlpacaSerializerOptions {
    DEFAULT("default", new HessianSerializer()),
    HESSIAN("hessian", new HessianSerializer()),
    KYRO("kyro", new KyroSerializer()),
    FAST_JSON("fastjson", new FastJsonSerializer()),
    JDK_JSON("jdk", new JDKSerializer());

    private final String name;
    private final AlpacaSerializer serializer;

    AlpacaSerializerOptions(String name, AlpacaSerializer serializer) {
        this.name = name;
        this.serializer = serializer;
    }

    public AlpacaSerializer getSerializer() {
        return serializer;
    }

    public String getName() {
        return name;
    }
}
