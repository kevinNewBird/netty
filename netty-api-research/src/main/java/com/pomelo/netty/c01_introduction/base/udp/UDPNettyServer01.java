package com.pomelo.netty.c01_introduction.base.udp;

import com.pomelo.netty.c01_introduction.base.udp.handlers.UDPNettyServerHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

/**
 * description：基于netty，udp服务器
 *
 * @author zhaosong
 * @version 1.0
 * @company 北京海量数据有限公司
 * @date 2024/9/21 22:17
 */
public class UDPNettyServer01 {

    public static void main(String[] args) throws InterruptedException {
        // 启动udp服务器
        startServer(9000);
    }

    private static void startServer(final int port) throws InterruptedException {
        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(bossGroup)
                    .channel(NioDatagramChannel.class)
                    .handler(new ChannelInitializer<DatagramChannel>() {
                        @Override
                        protected void initChannel(DatagramChannel ch) throws Exception {
                            ch.pipeline()
                                    .addLast(new LoggingHandler(LogLevel.INFO))
                                    .addLast(new UDPNettyServerHandler());
                        }
                    });

            ChannelFuture cf = bootstrap.bind(port).sync();
            System.out.println("Server started on port：" + port);
            cf.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
        }

    }
}
