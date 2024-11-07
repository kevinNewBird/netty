package com.pomelo.jdk.nio.introduce;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

/**
 * description: com.pomelo.jdk.nio.introduce
 * company: 北京海量数据有限公司
 * create by: zhaosong 2024/11/7
 * version: 1.0
 */
public class SimpleServer {

    public static void main(String[] args) throws IOException {
        // 1.打开一个服务端通道
        ServerSocketChannel server = ServerSocketChannel.open();
        // 2.设置服务监听端口
        server.bind(new InetSocketAddress(8080));
        // 3.设置为非阻塞
        server.configureBlocking(false);

        System.err.println("=======================start server==================");

        while (true) {
            // 4.非阻塞获取客户端连接
            SocketChannel client = server.accept();
            if (client == null) {
                continue;
            }

            System.err.println("*****************************************************");
            System.err.println("                     client connect");
            System.err.println("*****************************************************");
            // 5.准备数据缓存区
            ByteBuffer buffer = ByteBuffer.allocate(8192);// 读模式
            // 6.！！！读取数据(当客户端没有发送数据，read将一致阻塞)
            int len = client.read(buffer);
            String content = new String(buffer.array(), 0, len);
            System.out.println("【x】 Received client message: " + content);


            // 7.给客户端回写数据
            client.write(ByteBuffer.wrap("hello, client....".getBytes(StandardCharsets.UTF_8)));
            // 8.关闭客户端
            client.close();
            System.err.println("*****************************************************");
        }
    }
}
