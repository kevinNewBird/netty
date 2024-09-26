package com.pomelo.netty.c01_introduction.base.tcp;

import com.pomelo.netty.c01_introduction.base.tcp.handlers.TCPNettyClientHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

/**
 * description：基于netty实现的TCP客户端
 *
 * @author zhaosong
 * @version 1.0
 * @company 北京海量数据有限公司
 * @date 2024/9/21 21:06
 */
public class TCPNettyClient01 {

    public static void main(String[] args) throws InterruptedException {
        // 连接TCP服务器
        connect("127.0.0.1", 8090);
    }

    /**
     * 连接TCP服务器
     *
     * @param host
     * @param port
     * @throws InterruptedException
     */
    private static void connect(final String host, final int port) throws InterruptedException {
        NioEventLoopGroup workGroup = new NioEventLoopGroup();

        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(workGroup)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel sc) throws Exception {
                            // 添加自定义的handler以处理消息
                            ChannelPipeline pipeline = sc.pipeline();
                            pipeline.addLast(new LoggingHandler(LogLevel.INFO))
                                    .addLast(new TCPNettyClientHandler());
                        }
                    });

            // 连接服务器
            Channel channel = bootstrap.connect(host, port).sync().channel();
            System.out.println("Connected to " + host + ":" + port);
            channel.closeFuture().sync();
        } finally {
            workGroup.shutdownGracefully();
        }
    }
}
