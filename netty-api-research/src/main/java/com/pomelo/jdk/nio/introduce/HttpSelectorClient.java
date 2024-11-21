package com.pomelo.jdk.nio.introduce;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * description: NIO实现的客户端
 * company: 北京海量数据有限公司
 * create by: zhaosong 2024/11/21
 * version: 1.0
 */
public class HttpSelectorClient {

    public static void main(String[] args) throws IOException {
        // 0.初始化客户端
        // 客户端也用nio，事实上并不是非要用selector形式的nio
        // 一旦通过网络连接，和如何实现就无关
        Selector selector = Selector.open();
        SocketChannel sc = SocketChannel.open();
        // 设置非阻塞（否则会一直阻塞在读方法，等待服务器写入数据）
        sc.configureBlocking(false);
        // 绑定请求地址端口
        sc.connect(new InetSocketAddress("127.0.0.1", 9080));
        // 注册connect事件，相对于服务端注册的OP_ACCEPT事件
        sc.register(selector, SelectionKey.OP_CONNECT);

        // 1.连接
        out:
        while (true) {
            // 1.1.检查选择器是否有事件
            if (selector.select(2000) == 0) {
                System.err.println("No events are currently being monitored");
                continue;
            }
            // 1.2.获取监听事件集合
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = selectionKeys.iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                iterator.remove(); // 避免重复，否则历史监听事件将会一直被持有
                // 如果检测到连接事件，第一次进来肯定是连接事件，因为上面只注册了连接事件
                // 注意：读就绪和写就绪事件，一定要特别注意SocketChannel的close,不正确的关闭方式将导致数据无法发送出去或者接受
                // 尤其是模拟http请求。比如，客户端发送请求后，关闭了SocketChannel将会无法接受数据，即使有监听到写就绪事件。
                // 同理，服务端接收到请求后，关闭了SocketChannel将会无法发送数据。
                if (key.isConnectable()) {
                    doConnectEvent(key);
                } else if (key.isReadable()) { //在第二次进来写数据后注册了读事件，则第三次进来就会读到读就绪事件
                    doReadEvent(key);
                    break out;
                } else if (key.isWritable()) { // 连接完成注册好写事件，第二次进来则检测到的就是写事件
                    doWriteEvent(key);
                }
            }
        }
    }


    /**
     * description:处理连接事件
     * create by: zhaosong 2024/11/21 11:30
     *
     * @param key
     */
    private static void doConnectEvent(SelectionKey key) throws IOException {
        SocketChannel client = (SocketChannel) key.channel();
        if (client.finishConnect()) {
            client.configureBlocking(false);
            // 注册写事件，因为模拟http的形式，所以客户端是先写再读
            // 即先请求在得到响应数据，如果是想保持长连接则写成：channel.register(key.selector(), SelectionKey.OP_READ | SelectionKey.OP_WRITE)
            client.register(key.selector(), SelectionKey.OP_WRITE);
        }
    }

    /**
     * description: 处理写事件
     * create by: zhaosong 2024/11/21 11:44
     *
     * @param key
     */
    private static void doWriteEvent(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        channel.write(ByteBuffer.wrap("[x] client write".getBytes()));
        // 写完数据注册读事件，用于接受数据
        key.interestOps(SelectionKey.OP_READ);
    }

    /**
     * description:处理读事件
     * create by: zhaosong 2024/11/21 11:38
     *
     * @param key
     */
    private static void doReadEvent(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        try {
            // 读取数据
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            int size = channel.read(buffer);
            if (size < 1) {
                return;
            }
            byte[] content = new byte[size];
            // 切换为读模式
            buffer.flip();
            buffer.get(content);
            System.out.println("客户端读取到的数据：" + new String(content));
            // 清理事件监听
            key.interestOps(0);
        } finally {
            // 关闭连接
            channel.close();
        }
    }
}
