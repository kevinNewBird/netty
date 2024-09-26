package com.pomelo.jdk.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
//import java.net.StandardSocketOptions;

/**
 * description  SocketIOProperties <BR>
 * BIO  多线程的方式
 * <p>
 * author: zhao.song
 * date: created in 23:27  2021/7/14
 * company: TRS信息技术有限公司
 * version 1.0
 */
public class SocketIOProperties {


    // server socket listen property:
    private static final int RECEIVE_BUFFER = 10;
    private static final int SO_TIMEOUT = 0;
    private static final boolean REUSE_ADDR = false;
    private static final int BACK_LOG = 2;

    //client socket listen property on server endpoint:
    private static final boolean CLI_KEEPALIVE = false;
    private static final boolean CLI_OOB = false;
    private static final int CLI_REC_BUF = 20;
    private static final boolean CLI_REUSE_ADDR = false;
    private static final int CLI_SEND_BUF = 20;
    private static final boolean CLI_LINGER = true;
    private static final int CLI_LINGER_N = 0;
    private static final int CLI_TIMEOUT = 0;
    private static final boolean CLI_NO_DELAY = false;

/*
    StandardSocketOptions.TCP_NODELAY;
    StandardSocketOptions.SO_KEEPALIVE;
    StandardSocketOptions.SO_LINGER;
    StandardSocketOptions.SO_RCVBUF;
    StandardSocketOptions.SO_SNDBUF;
    StandardSocketOptions.SO_REUSEADDR;

    */

    public static void main(String[] args) {
        ServerSocket server = null;

        try {
            server = new ServerSocket();
            //服务端socket处理客户端socket连接是需要一定时间的。ServerSocket有一个队列
            // ，存放还没有来得及处理的客户端Socket，这个队列的容量就是backlog的含义。(已成功建立的连接不会占用backlog)
            // 如果队列已经被客户端socket占满了，如果还有新的连接过来，那么ServerSocket会拒绝新的连接。
            // 也就是说backlog提供了容量限制功能，避免太多的客户端socket占用太多服务器资源。
            server.bind(new InetSocketAddress(9090), BACK_LOG);

            server.setReceiveBufferSize(RECEIVE_BUFFER);
            server.setReuseAddress(REUSE_ADDR);
            server.setSoTimeout(SO_TIMEOUT);// >0,到时间后server.accept抛出中断异常,不会一直阻塞,避免恶意的消耗资源
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("server up use 9090!");

        while (true) {
            try {
                System.in.read();// 阻塞(分水岭)

                Socket client = server.accept();
                System.out.println("client port: " + client.getPort());

                // TCP规定: 如果双方建立了连接, 长时间不说话? 对方还活着嘛?
                // 就是心跳
                client.setKeepAlive(CLI_KEEPALIVE);
                client.setOOBInline(CLI_OOB);
                client.setReceiveBufferSize(CLI_REC_BUF);
                client.setReuseAddress(CLI_REUSE_ADDR);
                client.setSendBufferSize(CLI_SEND_BUF);
                client.setSoLinger(CLI_LINGER, CLI_LINGER_N);
                client.setSoTimeout(CLI_TIMEOUT);
                client.setTcpNoDelay(CLI_NO_DELAY);

                new Thread(new Runnable() {
//                    @Override
                    public void run() {
                        try {
                            InputStream in = client.getInputStream();
                            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                            char[] data = new char[1024];
                            while (true) {

                                int num = reader.read(data);

                                if (num > 0) {
                                    System.out.println("client read some data is :" + num + " val :" + new String(data, 0, num));
                                } else if (num == 0) {
                                    System.out.println("client readed nothing!");
                                } else {
                                    System.out.println("client readed -1...");
                                    System.in.read();
                                    client.close();
                                    break;
                                }
                            }

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
