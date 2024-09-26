package com.pomelo.jdk.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;

/***********************
 * @Description: 网络通讯Socket的非阻塞IO<BR>
 * @author: zhao.song
 * @since: 2021/4/5 19:31
 * @version: 1.0
 ***********************/
public class SocketNIO {

    public static void main(String[] args) throws IOException, InterruptedException {

        LinkedList<SocketChannel> clients = new LinkedList<>();

        //Java层面: New IO
        ServerSocketChannel socket = ServerSocketChannel.open(); // new
        socket.bind(new InetSocketAddress(9090));// bind -> listen

        socket.configureBlocking(false); // 重点:OS(操作系统)层面的非阻塞NONBlocking


        while (true) {
            //接受客户端的连接
            Thread.sleep(1000);
            SocketChannel client = socket.accept();//不会阻塞? -1 NULL
            if (client == null) {
                System.out.println("null...");
            }else{
                client.configureBlocking(false);// 重点 socket()
                int port = client.socket().getPort();
                System.out.println("client...port:" + port);
                clients.add(client);
            }
            ByteBuffer buffer = ByteBuffer.allocateDirect(4096);

            //遍历已经链接进来的客户端能不能读写数据
            for (SocketChannel c : clients) {
                int num = c.read(buffer);//>0 -1 0//不会阻塞
                if (num > 0) {
                    buffer.flip();
                    byte[] aaa = new byte[buffer.limit()];

                    String b = new String(aaa);
                    System.out.println(c.socket().getPort() + ":" + b);
                    buffer.clear();
                }
            }
        }



    }
}
