package com.pomelo.jdk.io;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;

/**
 * description  c10k 问题验证   客户端 <BR>
 * <p>
 * author: zhao.song
 * date: created in 22:06  2021/7/15
 * company: TRS信息技术有限公司
 * version 1.0
 */
public class C10kClient {


    public static void main(String[] args) {
        LinkedList<SocketChannel> clients = new LinkedList<>();
        InetSocketAddress serverAddr = new InetSocketAddress("192.168.233.129", 9090);

        // 端口: 65535
        for (int i = 10_000; i < 65_000; i++) {

            try {
                SocketChannel client11 = SocketChannel.open();
                SocketChannel client12 = SocketChannel.open();

                // 虚拟网卡
                client11.bind(new InetSocketAddress("192.168.233.1", i));

                client11.connect(serverAddr);
                boolean c11 = client11.isOpen();
                clients.add(client11);
                // 无线网卡
                client12.bind(new InetSocketAddress("192.168.1.6", i));

                client12.connect(serverAddr);
                boolean c12 = client12.isOpen();
                clients.add(client12);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        System.out.println("clients: " + clients.size());

        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
