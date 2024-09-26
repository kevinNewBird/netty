package com.pomelo.netty.c01_introduction.base.tcp;

import com.pomelo.netty.c01_introduction.base.tcp.handlers.TCPNettyServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

/**
 * description：基于netty的TCP服务器
 *
 * @author zhaosong
 * @version 1.0
 * @company 北京海量数据有限公司
 * @date 2024/9/21 20:29
 */
public class TCPNettyServer01 {

    public static void main(String[] args) throws InterruptedException {
        // 启动TCP服务器
        startServer(8090);
    }

    private static void startServer(final int port) throws InterruptedException {
        // 配置服务器的管理线程池和工作线程池
        NioEventLoopGroup workerGroup = new NioEventLoopGroup(); // 工作线程组，用于处理就绪连接的实际信息
        TCPNettyServerHandler serverHandler = new TCPNettyServerHandler();

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel channel) throws Exception {
                            ChannelPipeline pipe = channel.pipeline();
                            pipe.addLast(new LoggingHandler(LogLevel.INFO))
                                    .addLast(serverHandler);
                        }
                    });

            // 启动服务器
            ChannelFuture cf = bootstrap.bind(port).sync();
            System.out.println("Server started on port：" + port);
            cf.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
        }
    }
}
