package com.pomelo.netty.c01_introduction.base.udp.handlers;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.CharsetUtil;

/**
 * description：UDP服务器的消息处理器
 *
 * @author zhaosong
 * @version 1.0
 * @company 北京海量数据有限公司
 * @date 2024/9/21 22:31
 */
public class UDPNettyServerHandler extends SimpleChannelInboundHandler<DatagramPacket> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) throws Exception {
        // 处理接收到的数据
        String receivedMsg = msg.content().toString(CharsetUtil.UTF_8);
        System.out.println("Received message from Client:" + receivedMsg);
        System.out.println("Client address:" + ctx.channel().remoteAddress());

        // 响应客户端
        DatagramPacket responsePacket = new DatagramPacket(Unpooled.copiedBuffer("Hello, Client!"
                , CharsetUtil.UTF_8), msg.sender());
        ctx.writeAndFlush(responsePacket);
    }
}
