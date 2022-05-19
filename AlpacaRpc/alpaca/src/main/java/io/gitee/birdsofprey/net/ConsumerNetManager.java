package io.gitee.birdsofprey.net;

import io.gitee.birdsofprey.config.DefaultConfig;
import io.gitee.birdsofprey.handler.AlpacaByteToResponseDecoder;
import io.gitee.birdsofprey.handler.AlpacaRequestToByteEncoder;
import io.gitee.birdsofprey.protocol.AlpacaRequest;
import io.gitee.birdsofprey.protocol.AlpacaResponse;
import io.gitee.birdsofprey.registry.AlpacaRegistry;
import io.gitee.birdsofprey.serializer.AlpacaSerializer;
import io.gitee.birdsofprey.strategy.AlpacaStrategy;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.util.concurrent.DefaultPromise;
import io.netty.util.concurrent.Promise;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 客户端网络管理器
 * 客户端程序请求服务端rpc服务通过该管理器发送请求
 *
 * @author Mr.Alpaca
 * @version 1.0.2
 * TODO: 将初始化错误改为自定义类型
 */
@Slf4j
public class ConsumerNetManager {
    /**
     * 客户端事件循环池
     */
    private final NioEventLoopGroup eventLoopGroup;
    /**
     * 客户端Future, 可通过该Future发送消息, 对引导器进行关闭
     */
    private final Bootstrap bootstrap;
    /**
     * 注册中心
     */
    private final AlpacaRegistry registry;
    /**
     * 序列化器
     */
    private final AlpacaSerializer serializer;
    /**
     * 负载均衡策略
     */
    private final AlpacaStrategy strategy;
    /**
     * 存储已连接的channel
     * key=远程地址 value=channel
     */
    private final Map<InetSocketAddress, Channel> addressChannelMap;


    public ConsumerNetManager(AlpacaRegistry registry, AlpacaSerializer serializer, AlpacaStrategy strategy) {
        this(DefaultConfig.GROUP_THREADS, registry, serializer, strategy);
    }

    public ConsumerNetManager(int groupThreads, AlpacaRegistry registry, AlpacaSerializer serializer, AlpacaStrategy strategy) {
        this(new NioEventLoopGroup(groupThreads), registry, serializer, strategy);
    }

    public ConsumerNetManager(NioEventLoopGroup eventLoopGroup, AlpacaRegistry registry, AlpacaSerializer serializer, AlpacaStrategy strategy) {
        this.eventLoopGroup = eventLoopGroup;
        this.registry = registry;
        this.serializer = serializer;
        this.strategy = strategy;
        this.addressChannelMap = new ConcurrentHashMap<>(16);
        // 获取客户端引导
        this.bootstrap = startBoostrap();
    }

    /**
     * 启动客户端引导并添加编解码器
     *
     * @return 异步对象
     */
    private Bootstrap startBoostrap() {
        return new Bootstrap()
                .group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                        // 添加handler
                        add(nioSocketChannel);
                    }
                });
    }

    /**
     * 给引导器流水线添加编解码器
     * 解码: LTC解码器 -> ByteBuf -> AlpacaResponse -> 消息处理
     * 编码: AlpacaRequest -> ByteBuf
     *
     * @param channel 通道
     */
    private void add(NioSocketChannel channel) {
        ChannelPipeline pipeline = channel.pipeline();
        // pipeline.addLast(new LoggingHandler(LogLevel.DEBUG));
        pipeline.addLast(new LengthFieldBasedFrameDecoder(DefaultConfig.MAX_FRAME_LENGTH, DefaultConfig.LENGTH_FIELD_OFFSET, DefaultConfig.LENGTH_FIELD_LENGTH, DefaultConfig.LENGTH_ADJUSTMENT, DefaultConfig.INITIAL_BYTES_TO_STRIP));
        pipeline.addLast(new AlpacaByteToResponseDecoder(serializer));
        pipeline.addLast(new ChannelInboundHandlerAdapter() {
            @Override
            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                AlpacaResponse response = (AlpacaResponse) msg;
                Promise<AlpacaResponse> promise = AlpacaContext.getPromise(response.getId());
                promise.setSuccess(response);
                super.channelRead(ctx, msg);
            }

            @Override
            public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                log.warn("{} 连接断开", ctx.channel());
            }
        });
        pipeline.addLast(new AlpacaRequestToByteEncoder(serializer));
    }

    /**
     * 发送消息
     * 根据消息id获取存储返回结果的异步promise并存储到context中
     * 发送数据并返回异步对象
     *
     * @param request 请求消息
     * @return 异步对象, 可通过该对象获取响应消息
     */
    public Promise<AlpacaResponse> send(AlpacaRequest request) {
        Integer id = request.getId();
        Promise<AlpacaResponse> promise = new DefaultPromise<>(eventLoopGroup.next());
        AlpacaContext.setPromise(id, promise);
        // 负载均衡获取服务提供者地址
        InetSocketAddress remoteAddress = getServiceProvider(request.getInterfaceName());
        // 获取已连接channel, 若无则创建并保存
        Channel channel = addressChannelMap.get(remoteAddress);
        if (Objects.isNull(channel)) {
            channel = bootstrap.connect(remoteAddress).channel();
            addressChannelMap.put(remoteAddress, channel);
        }
        channel.writeAndFlush(request);
        return promise;
    }

    /**
     * 根据负载均衡策略获取服务提供者的远程地址
     *
     * @param serviceName 服务名
     * @return InetSocketAddress
     */
    private InetSocketAddress getServiceProvider(String serviceName) {
        List<AlpacaInvoker> invokerList = registry.getServiceInvokers(serviceName);
        String url = strategy.loadBalance(invokerList).getUrl();
        String[] split = url.split(":");
        return new InetSocketAddress(split[0], Integer.parseInt(split[1]));
    }

    /**
     * 关闭网络资源
     */
    public void close() {
        this.eventLoopGroup.shutdownGracefully();
    }
}
