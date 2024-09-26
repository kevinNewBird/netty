package com.pomelo.netty.c01_introduction.base.udp;

import com.pomelo.netty.c01_introduction.base.udp.handlers.UDPNettyClientHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.CharsetUtil;

import java.net.InetSocketAddress;

/**
 * description：基于netty实现的TCP客户端
 *
 * @author zhaosong
 * @version 1.0
 * @company 北京海量数据有限公司
 * @date 2024/9/21 21:06
 */
public class UDPNettyClient01 {

    public static void main(String[] args) throws InterruptedException {
        // 连接TCP服务器
        connect("127.0.0.1", 9000);
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
                    .channel(NioDatagramChannel.class)
                    .handler(new ChannelInitializer<DatagramChannel>() {
                        @Override
                        protected void initChannel(DatagramChannel dc) throws Exception {
                            // 添加自定义的handler以处理消息
                            ChannelPipeline pipeline = dc.pipeline();
                            pipeline.addLast(new LoggingHandler(LogLevel.INFO))
                                    .addLast(new UDPNettyClientHandler());
                        }
                    });

            // 连接服务器
            Channel channel = bootstrap.bind(0).sync().channel();
            System.out.println("Client started");

            // 发送消息给服务端 (必须在此处发送)
            DatagramPacket responsePacket = new DatagramPacket(Unpooled.copiedBuffer("Hello, Server!"
                    , CharsetUtil.UTF_8), new InetSocketAddress(host, port));
            channel.writeAndFlush(responsePacket).sync();
            channel.closeFuture().sync();
        } finally {
            workGroup.shutdownGracefully();
        }
    }
}
