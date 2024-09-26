package com.pomelo.jdk.io;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * description: com.pomelo.jdk.io
 * company: 北京海量数据有限公司
 * create by: zhaosong 2024/9/26
 * version: 1.0
 */
public class SocketChannelClient {

    public static void main(String[] args) throws IOException {
        InetSocketAddress serverAddr = new InetSocketAddress("127.0.0.1", 9090);
        SocketChannel client = SocketChannel.open(serverAddr); // 等同于connect(), 注意不要使用bind
        client.configureBlocking(false);

        // 写线程
        new Thread(() -> {
            try {
                InputStream in = System.in;
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                while (true) {
                    String line = reader.readLine();
                    if (line != null) {
                        client.write(ByteBuffer.wrap(line.getBytes()));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        // 读线程
        new Thread(() -> {
            try {
                ByteBuffer cache = ByteBuffer.allocate(1024);
                System.out.println("cache:" + cache);
                cache.clear();
                while (true) {
                    int len = client.read(cache);
                    System.out.println("len:" + len);
                    if (len > 0) {
                        cache.flip();// 切花为读
                        byte[] data = new byte[cache.limit()];
                        cache.get(data);
                        System.out.println("get: " + cache);
                        System.out.println("接收:" + new String(data, 0, cache.limit()));
                    } else if (len == 0) {
                        break;
                    } else {
                        client.close();
                        break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

    }
}
