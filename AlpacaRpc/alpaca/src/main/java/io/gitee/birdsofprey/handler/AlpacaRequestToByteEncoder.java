package io.gitee.birdsofprey.handler;

import io.gitee.birdsofprey.protocol.AlpacaRequest;
import io.gitee.birdsofprey.serializer.AlpacaSerializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * 编码器, 将AlpacaRequest转码成ByteBuf, 步骤:
 * AlpacaRequest -> 序列化 -> 字节缓冲(缓冲大小 | request序列化后长度 | request序列化字节数组)
 *
 * @author Mr.Alpaca
 * @version 1.0.2
 */
public class AlpacaRequestToByteEncoder extends MessageToByteEncoder<AlpacaRequest> {
    private final AlpacaSerializer serializer;

    public AlpacaRequestToByteEncoder(AlpacaSerializer serializer) {
        this.serializer = serializer;
    }

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, AlpacaRequest alpacaRequest, ByteBuf byteBuf) throws Exception {
        // 可拓展为序列化选择器
        byte[] content = serializer.serialize(alpacaRequest);
        int length = content.length;
        // 可拓展为帧
        byteBuf.writeInt(length + 4);
        byteBuf.writeInt(length);
        byteBuf.writeBytes(content);
    }
}
