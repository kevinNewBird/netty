package com.pomelo.jdk.nio.introduce;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * description: 注册了选择器的服务端
 * company: 北京海量数据有限公司
 * create by: zhaosong 2024/11/7
 * version: 1.0
 */
public class HttpSelectorServer {

    public static void main(String[] args) throws IOException {
        // 1.打开一个服务端通道
        ServerSocketChannel server = ServerSocketChannel.open();
        // 2.设置服务监听端口
        server.bind(new InetSocketAddress(9080));
        // 3.设置为非阻塞
        server.configureBlocking(false);

        // 4.创建选择器
        Selector selector = Selector.open();
        // 5.注册选择器到服务器通道，并设置监听事件（监听客户端连接）
        // 注意：SeletionKey的监听事件是注册在了SelectionKey对象上，而不是selector选择器上
        server.register(selector, SelectionKey.OP_ACCEPT);
        System.err.println("=======================start server==================");

        while (true) {
            // 6.检查选择器是否有事件
            if (selector.select(2000) == 0) {
                System.err.println("No events are currently being monitored");
                continue;
            }
            // 7.获取监听事件集合
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = selectionKeys.iterator();
            while (iterator.hasNext()) {
                SelectionKey selectionKey = iterator.next();
                iterator.remove(); // 必须移除，否则历史监听事件将会一直被持有
                // 注意：读就绪和写就绪事件，一定要特别注意SocketChannel的close,不正确的关闭方式将导致数据无法发送出去或者接受
                // 尤其是模拟http请求。比如，客户端发送请求后，关闭了SocketChannel将会无法接受数据，即使有监听到写就绪事件。
                // 同理，服务端接收到请求后，关闭了SocketChannel将会无法发送数据。
                if (selectionKey.isAcceptable()) {  // 7.1.判断是否有客户端连接接入
                    doAcceptEvent(selectionKey);
                } else if (selectionKey.isReadable()) { // 7.2.读事件就绪(当注册完读事件，下一次循环读就绪时就会进入此方法)
                    doReadEvent(selectionKey);
                } else if (selectionKey.isWritable()) { // 7.3.处理完读会注册写事件，下一次循环会进入此方法
                    doWriteEvent(selectionKey);
                }
            }
        }
    }

    /**
     * description:接收客户端连接事件
     * create by: zhaosong 2024/11/7 10:58
     *
     * @param selectionKey
     */
    private static void doAcceptEvent(SelectionKey selectionKey) {
        try {
            // 1.获取服务端，并获取客户端连接
            ServerSocketChannel server = (ServerSocketChannel) selectionKey.channel();
            SocketChannel client = server.accept();
            // 设置client为非阻塞
            // 不然服务端监听到读取事件（对应客户端写数据）时，读取数据（read）将会阻塞
            client.configureBlocking(false);

            // 2.设置客户端的选择器，监听该客户端的读事件
            // （即当客户端连接的写入数据准备好，服务端这边接受的客户端连接也就是读准备好）
            // 为什么使用selectionKey.selector，因为服务端获取的是该选择器的监听器，如果创建新的选择器，通过selector事件将无法被监听到。
            // 注册读事件，会覆盖之前注册的OP_ACCEPT事件.因为对于当前连接而言，selectionKey是没有变化的，因为模仿http请求单次请求，单次响应，所以作为服务端肯定是先注册读事件用于处理。
            client.register(selectionKey.selector(), SelectionKey.OP_READ);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * description:读事件就绪处理
     * create by: zhaosong 2024/11/7 11:09
     *
     * @param selectionKey
     */
    private static void doReadEvent(SelectionKey selectionKey) {
        // http请求，读就绪事件一定不能关闭客户端，否则将会导致数据写不出去
        SocketChannel client = (SocketChannel) selectionKey.channel();
        try {
            // 1.准备数据缓存
            ByteBuffer buffer = ByteBuffer.allocate(8192);
            // 2.读取数据
            int len = client.read(buffer);
            String content = new String(buffer.array(), 0, len);
            System.out.println("【x】 Received client message: " + content);
            // 3.给客户端回写数据
//            client.write(ByteBuffer.wrap("hello, client....".getBytes(StandardCharsets.UTF_8)));
            // 改变自身关注事件，可以用位或操作|组合，读完之后注册写事件模拟http的响应，所以用写事件覆盖读事件
            selectionKey.interestOps(SelectionKey.OP_WRITE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * description: 写事件就绪处理
     * create by: zhaosong 2024/11/21 11:58
     *
     * @param selectionKey
     */
    private static void doWriteEvent(SelectionKey selectionKey) throws IOException {
        SocketChannel channel = (SocketChannel) selectionKey.channel();
        channel.write(ByteBuffer.wrap("[x] server write".getBytes()));
        // 改变自身关注事件，此次为清理所有监听事件，模拟http中响应，关闭此连接
        selectionKey.interestOps(0);
        channel.close();
    }
}
