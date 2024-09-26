package com.pomelo.netty.c01_introduction.base.http.handlers;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;

/**
 * description：HttpNettyServer01的业务处理程序
 *
 * @author zhaosong
 * @version 1.0
 * @company 北京海量数据有限公司
 * @date 2024/9/21 20:04
 */
public class HttpNettyServerHandler extends SimpleChannelInboundHandler<HttpObject> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
        if (msg instanceof HttpRequest) {
            HttpRequest req = (HttpRequest) msg;
            DefaultFullHttpResponse resp = new DefaultFullHttpResponse(req.protocolVersion(), HttpResponseStatus.OK
                    , Unpooled.wrappedBuffer("hello world".getBytes()));
            resp.headers()
                    .set(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.TEXT_PLAIN)
                    .setInt(HttpHeaderNames.CONTENT_LENGTH, resp.content().readableBytes());
            // 写入并且要刷新，浏览器才能接收到数据“hello world”
            ChannelFuture cf = ctx.writeAndFlush(resp);
        }
    }
}
