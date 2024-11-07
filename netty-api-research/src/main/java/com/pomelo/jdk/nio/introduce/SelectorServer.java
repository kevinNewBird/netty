package com.pomelo.jdk.nio.introduce;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;

/**
 * description: 注册了选择器的服务端
 * company: 北京海量数据有限公司
 * create by: zhaosong 2024/11/7
 * version: 1.0
 */
public class SelectorServer {

    public static void main(String[] args) throws IOException {
        // 1.打开一个服务端通道
        ServerSocketChannel server = ServerSocketChannel.open();
        // 2.设置服务监听端口
        server.bind(new InetSocketAddress(8080));
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
                if (selectionKey.isAcceptable()) {  // 7.1.判断是否有客户端连接接入
                    doAcceptEvent(selectionKey);
                } else if (selectionKey.isReadable()) { // 7.2.读事件就绪
                    doReadEvent(selectionKey);
                }
            }
        }
    }

    /**
     * 接收客户端连接事件
     * description:
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
            client.register(selectionKey.selector(), SelectionKey.OP_READ);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 读事件就绪处理
     * description:
     * create by: zhaosong 2024/11/7 11:09
     *
     * @param selectionKey
     */
    private static void doReadEvent(SelectionKey selectionKey) {

        try (SocketChannel client = (SocketChannel) selectionKey.channel();) {
            // 1.准备数据缓存
            ByteBuffer buffer = ByteBuffer.allocate(8192);
            // 2.读取数据
            int len = client.read(buffer);
            String content = new String(buffer.array(), 0, len);
            System.out.println("【x】 Received client message: " + content);
            // 3.给客户端回写数据
            client.write(ByteBuffer.wrap("hello, client....".getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
