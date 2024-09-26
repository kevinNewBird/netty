package com.pomelo.jdk.io;

import java.io.*;
import java.net.Socket;

/**
 * description  SocketClient <BR>
 * <p>
 * author: zhao.song
 * date: created in 0:47  2021/7/15
 * company: TRS信息技术有限公司
 * version 1.0
 */
public class SocketClient {

    public static void main(String[] args) {
        try {
//            Socket client = new Socket("192.168.233.128", 9090);
            Socket client = new Socket("127.0.0.1", 9090);

            // 发送的缓冲区大小
            client.setSendBufferSize(20);
            // true:数据立即发送
            // false: 数据放入缓存区满足一定的大小后一起发送
            client.setTcpNoDelay(false);
            // 配合TcpNoDelay: false 且false  分包发送: 发送时, 先发送一个着急的数据, 然后再发剩下的
            client.setOOBInline(false);
            OutputStream out = client.getOutputStream();

            InputStream in = System.in;
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));

            while (true) {
                String line = reader.readLine();
                if (line != null) {
                    byte[] bb = line.getBytes();
                    for (byte b : bb) {
                        out.write(b);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
