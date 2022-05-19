package io.gitee.birdsofprey.net;

import io.gitee.birdsofprey.config.DefaultConfig;
import io.gitee.birdsofprey.handler.AlpacaByteToRequestDecoder;
import io.gitee.birdsofprey.handler.AlpacaResponseToByteEncoder;
import io.gitee.birdsofprey.protocol.AlpacaRequest;
import io.gitee.birdsofprey.protocol.AlpacaResponse;
import io.gitee.birdsofprey.serializer.AlpacaSerializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;

/**
 * 服务网络管理器
 *
 * @author Mr.Alpaca
 * @version 1.0.2
 */
@Slf4j
public class ProviderNetManager {
    /**
     * 通道事件
     */
    private final ChannelFuture serverFuture;
    private final NioEventLoopGroup parentGroup;
    private final NioEventLoopGroup childGroup;
    private final AlpacaSerializer serializer;

    public ProviderNetManager(int port, AlpacaSerializer serializer) {
        this(port, DefaultConfig.PARENT_THREADS, DefaultConfig.CHILD_THREADS, serializer);
    }

    public ProviderNetManager(int port, int parentThreads, int childThreads, AlpacaSerializer serializer) {
        this(port, new NioEventLoopGroup(parentThreads), new NioEventLoopGroup(childThreads), serializer);
    }

    public ProviderNetManager(int port, NioEventLoopGroup parentGroup, NioEventLoopGroup childGroup, AlpacaSerializer serializer) {
        this.parentGroup = parentGroup;
        this.childGroup = childGroup;
        this.serializer = serializer;
        // 启动服务端引导
        this.serverFuture = startServerBoostrap(port);
    }

    /**
     * 启动引导
     *
     * @param port 占用端口
     * @return ChannelFuture
     */
    private ChannelFuture startServerBoostrap(int port) {
        return new ServerBootstrap()
                .group(this.parentGroup, this.childGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                        add(nioSocketChannel);
                    }
                })
                .bind(port);
    }

    /**
     * 添加通道流水线
     *
     * @param nioSocketChannel 通道
     */
    private void add(NioSocketChannel nioSocketChannel) {
        ChannelPipeline pipeline = nioSocketChannel.pipeline();
        // pipeline.addLast(new LoggingHandler(LogLevel.DEBUG));
        pipeline.addLast(new LengthFieldBasedFrameDecoder(DefaultConfig.MAX_FRAME_LENGTH, DefaultConfig.LENGTH_FIELD_OFFSET, DefaultConfig.LENGTH_FIELD_LENGTH, DefaultConfig.LENGTH_ADJUSTMENT, DefaultConfig.INITIAL_BYTES_TO_STRIP));
        pipeline.addLast(new AlpacaByteToRequestDecoder(serializer));
        pipeline.addLast(new ChannelInboundHandlerAdapter() {
            @Override
            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                AlpacaRequest request = (AlpacaRequest) msg;
                AlpacaResponse response = getResponse(request);
                nioSocketChannel.writeAndFlush(response);
                super.channelRead(ctx, msg);
            }

            @Override
            public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                log.warn("{} 连接断开", ctx.channel());
            }
        });
        pipeline.addLast(new AlpacaResponseToByteEncoder(serializer));
    }

    /**
     * 获取响应
     *
     * @param request 请求
     * @return 响应
     */
    private AlpacaResponse getResponse(AlpacaRequest request) {
        String interfaceName = request.getInterfaceName();
        String methodName = request.getMethodName();
        Class<?>[] parameterType = request.getParameterType();
        Object[] args = request.getArgs();
        // 响应对象与请求对象的序号相同
        AlpacaResponse response = new AlpacaResponse();
        response.setId(request.getId());
        try {
            // 从context中获取提供服务的bean
            Object serviceObject = ProviderServiceContext.getService(interfaceName);
            Object result = serviceObject.getClass().getMethod(methodName, parameterType).invoke(serviceObject, args);
            response.setReturnValue(result);
        } catch (Exception e) {
            response.setExceptionMessage(e.getMessage());
        }
        return response;
    }

    /**
     * 关闭网络管理器
     */
    public void close() {
        this.parentGroup.shutdownGracefully();
        this.childGroup.shutdownGracefully();
    }
}
