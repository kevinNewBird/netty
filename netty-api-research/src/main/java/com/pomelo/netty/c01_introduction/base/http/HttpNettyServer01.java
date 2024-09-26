package com.pomelo.netty.c01_introduction.base.http;

import com.pomelo.netty.c01_introduction.base.http.handlers.HttpNettyServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.HttpServerExpectContinueHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

/**
 * description：基于Netty的Http服务器
 *
 * @author zhaosong
 * @version 1.0
 * @company 北京海量数据有限公司
 * @date 2024/9/21 19:53
 */
public class HttpNettyServer01 {

    public static void main(String[] args) throws InterruptedException {
        // 1.启动Http服务器
        startServer();
    }


    /**
     * 启动ntty服务器（http）
     *
     * @throws InterruptedException
     */
    private static void startServer() throws InterruptedException {
        // 配置服务器的管理线程池和工作线程池
        NioEventLoopGroup bossGroup = new NioEventLoopGroup(1); // 用于接收就绪的连接，并分配给工作线程组
        NioEventLoopGroup workerGroup = new NioEventLoopGroup(); // 工作线程组，用于处理就绪连接的实际信息

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer() {
                        @Override
                        protected void initChannel(Channel channel) throws Exception {
                            ChannelPipeline pipe = channel.pipeline();
                            pipe.addLast(new HttpServerCodec())
                                    .addLast(new HttpServerExpectContinueHandler())
                                    .addLast(new HttpNettyServerHandler());
                        }
                    });

            // 启动服务器
            Channel channel = bootstrap.bind(8080).sync().channel();
            System.out.println("Open your web browser and navigate to http://127.0.0.1:8080");
            channel.closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
