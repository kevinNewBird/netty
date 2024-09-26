package com.pomelo.netty.c01_introduction.base.tcp.handlers;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;

import java.nio.charset.StandardCharsets;

/**
 * description：TCPNettyServer01的处理器
 *
 * @author zhaosong
 * @version 1.0
 * @company 北京海量数据有限公司
 * @date 2024/9/21 20:33
 */
public class TCPNettyClientHandler extends ChannelInboundHandlerAdapter {

    /**
     * 通道创建就绪后出发
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("client ctx = " + ctx);
        ctx.writeAndFlush(Unpooled.copiedBuffer("Hello, Server!", CharsetUtil.UTF_8));
    }

    /**
     * 当通道由读取事件时，会触发
     *
     * @param ctx
     * @param msg
     * @throws Exception
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = (ByteBuf) msg;
        System.out.println("服务器回复的消息：" + buf.toString(StandardCharsets.UTF_8));
        System.out.println("服务器的地址：" + ctx.channel().remoteAddress());
    }

    /**
     * 处理一场，一般是需要关闭通道
     *
     * @param ctx
     * @param cause
     * @throws Exception
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.err.println("exceptionCaught ->" + cause.getMessage());
        cause.printStackTrace();
        ctx.close();
    }
}
