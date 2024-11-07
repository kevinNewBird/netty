package com.pomelo.jdk.nio.introduce;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

/**
 * description: com.pomelo.jdk.nio.introduce
 * company: 北京海量数据有限公司
 * create by: zhaosong 2024/11/7
 * version: 1.0
 */
public class SimpleClient {

    public static void main(String[] args) throws IOException {
        // 1.打开通道
        SocketChannel client = SocketChannel.open();
        // 2.设置连接ip和端口号
        client.connect(new InetSocketAddress("127.0.0.1", 8080));

        // 3.写出数据
        client.write(ByteBuffer.wrap("hello, server....".getBytes(StandardCharsets.UTF_8)));

        ByteBuffer buffer = ByteBuffer.allocate(8192);
        // 4.！！！读取服务器回写数据(当服务端没有写入数据将会一致阻塞)
        int len = client.read(buffer);
        String content = new String(buffer.array(), 0, len);
        System.out.println("【x】 Received server message: " + content);

        // 5.释放资源
        client.close();
    }
}
