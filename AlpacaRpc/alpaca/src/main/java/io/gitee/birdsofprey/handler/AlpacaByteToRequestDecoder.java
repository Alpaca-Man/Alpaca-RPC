package io.gitee.birdsofprey.handler;

import io.gitee.birdsofprey.protocol.AlpacaRequest;
import io.gitee.birdsofprey.serializer.AlpacaSerializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * 解码器, 将ByteBuf转码成AlpacaRequest, 步骤:
 * ByteBuf -> 反序列化 -> AlpacaRequest
 *
 * @author Mr.Alpaca
 * @version 1.0.2
 */
public class AlpacaByteToRequestDecoder extends ByteToMessageDecoder {
    private final AlpacaSerializer serializer;

    public AlpacaByteToRequestDecoder(AlpacaSerializer serializer) {
        this.serializer = serializer;
    }

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        // 若当前buf可读字节不够则等待下一个事件
        if (byteBuf.readableBytes() < 4) {
            return;
        }
        // 标记当前读位置, 若可读字节不够则复原buf状态等待下一事件
        byteBuf.markReaderIndex();
        int length = byteBuf.readInt();
        if (byteBuf.readableBytes() < length) {
            byteBuf.resetReaderIndex();
            return;
        }
        byte[] content = new byte[length];
        byteBuf.readBytes(content, 0, length);
        AlpacaRequest request = serializer.deserialze(AlpacaRequest.class, content);
        list.add(request);
    }
}
