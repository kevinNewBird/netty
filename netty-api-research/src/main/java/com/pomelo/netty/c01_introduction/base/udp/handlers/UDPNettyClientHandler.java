package com.pomelo.netty.c01_introduction.base.udp.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.CharsetUtil;


/**
 * description：UDP客户端消息处理器
 *
 * @author zhaosong
 * @version 1.0
 * @company 北京海量数据有限公司
 * @date 2024/9/21 22:41
 */
public class UDPNettyClientHandler extends SimpleChannelInboundHandler<DatagramPacket> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) throws Exception {
        // 处理接收到的数据
        String receivedMsg = msg.content().toString(CharsetUtil.UTF_8);
        System.out.println("Received response from server: " + receivedMsg);
        System.out.println("Server address: " + ctx.channel().remoteAddress());
    }
}
