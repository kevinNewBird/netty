package com.pomelo.netty.c01_introduction.base.tcp.handlers;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;

/**
 * description：TCPNettyServer01的处理器
 *
 * @author zhaosong
 * @version 1.0
 * @company 北京海量数据有限公司
 * @date 2024/9/21 20:33
 */
public class TCPNettyServerHandler extends ChannelInboundHandlerAdapter {

    /**
     * 读取数据实际（这里我们可以读取客户端发送的消息）
     *
     * @param ctx：上下文对象
     * @param msg：客户端发送的数据
     * @throws Exception
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("server ctx = " + ctx);
        Channel channel = ctx.channel();
        // ByteBuf是Netty提供的，不是NIO的ByteBuffer
        ByteBuf buf = (ByteBuf) msg;
        System.out.println("客户端发送的消息：" + buf.toString(CharsetUtil.UTF_8));
        System.out.println("客户端地址：" + channel.remoteAddress());
    }


    /**
     * 读取完毕，回复
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        // writeAndFlush 是 write + flush 将数据写入到缓存并刷新
        ctx.writeAndFlush(Unpooled.copiedBuffer("Hello, Client!", CharsetUtil.UTF_8));
    }

    /**
     * 处理异常，一般是需要关闭通道
     *
     * @param ctx
     * @param cause
     * @throws Exception
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.err.println("exceptionCaught ->" + cause.getMessage());
        ctx.close();
    }
}
