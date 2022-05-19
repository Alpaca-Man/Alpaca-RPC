package io.gitee.birdsofprey.handler;

import io.gitee.birdsofprey.protocol.AlpacaResponse;
import io.gitee.birdsofprey.serializer.AlpacaSerializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * 编码器, 将AlpacaResponse转码成ByteBuf, 步骤:
 * AlpacaResponse -> 序列化 -> 字节缓冲(缓冲大小 | response序列化后长度 | response序列化字节数组)
 *
 * @author Mr.Alpaca
 * @version 1.0.2
 */
public class AlpacaResponseToByteEncoder extends MessageToByteEncoder<AlpacaResponse> {
    private final AlpacaSerializer serializer;

    public AlpacaResponseToByteEncoder(AlpacaSerializer serializer) {
        this.serializer = serializer;
    }

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, AlpacaResponse alpacaResponse, ByteBuf byteBuf) throws Exception {
        // 可拓展为序列化选择器
        byte[] content = serializer.serialize(alpacaResponse);
        int length = content.length;
        // 可拓展为帧
        byteBuf.writeInt(length + 4);
        byteBuf.writeInt(length);
        byteBuf.writeBytes(content);
    }
}
