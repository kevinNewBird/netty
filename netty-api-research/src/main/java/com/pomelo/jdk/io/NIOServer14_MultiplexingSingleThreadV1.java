package com.pomelo.jdk.io;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * description  NIOServer14_MultiplexingSingleThreadV1 <BR>
 * <p>
 * author: zhao.song
 * date: created in 22:26  2021/6/30
 * company: TRS信息技术有限公司
 * version 1.0
 */
public class NIOServer14_MultiplexingSingleThreadV1 {


    private ServerSocketChannel server = null;

    private Selector selector = null; // linux多路复用器(select/poll  epoll)

    int port = 9090;

    public static void main(String[] args) {
        new NIOServer14_MultiplexingSingleThreadV1().start();
    }

    public void initServer() {
        try {
            // 1.完成了 listen                        -> fd3
            server = ServerSocketChannel.open();
            server.configureBlocking(false);
            server.bind(new InetSocketAddress(port));

            // 2.完成了 epoll_create                 -> fd4
            // epoll: 如果在epoll模型下, open -> epoll_create
            // select/poll: 只是获取了一个多路复用器对象(相当于在底层啥都没有处理)
            selector = Selector.open(); // 优先选择epoll, 但是可以 -D修正

            // 3.完成epoll_ctl                      -> epoll_ctl(fd4,ADD,fd3,EPOLLIN)
            // 相当于listen
            // epoll: 将fd3放进开辟在内核空间的文件描述符空间fd4中
            // select/poll: jvm里开辟一个数组,将fd3放进去
            server.register(selector, SelectionKey.OP_ACCEPT);//懒加载: 当Selector.select()调用发生时,epoll_ctl执行

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start() {
        initServer();

        System.out.println("服务器启动了");

        try {
            while (true) {
                Set<SelectionKey> keys = selector.keys();
//                System.out.println(keys.size() + "  size");

                // 1.调用多路复用器(select/poll  epoll(epoll_wait))
                /*
                select()是啥意思:
                ①.select/poll: 内核的select(fd3), poll(fd3)
                ②.epoll: epoll_wait
                 */
                // 是否存在有状态的fd集合
                while (selector.select(500) > 0) {
                    Set<SelectionKey> selectionKeys = selector.selectedKeys();// 返回有状态的fd集合
                    Iterator<SelectionKey> iter = selectionKeys.iterator();

                    // 逐一遍历每一个IO
                    // 管你是什么多路复用器, 都只会得到状态,需要程序自己去一个个去处理R/W
                    while (iter.hasNext()) {
                        SelectionKey key = iter.next();
                        iter.remove();//不移除会重复循环处理

                        if (key.isAcceptable()) {// 监听事件
                            //这里是重点,如果要去接收一个新的连接
                            //语义上,accept接收连接且返回新连接的fd对吧?
                            //那新的fd怎么办
                            // select/poll: 因为他们内核没有空间,那么在jvm中保存和前边的fd3那个listen的一起
                            // epoll: 通过epoll_ctl把新的客户端fd注册到内核空间
                            acceptHandler(key); // 调用key.channel().accept()创建客户端连接，生成新的fd5与客户端绑定，这一步做完key.attachment()才能获取到客户端通道

                        } else if (key.isReadable()) {// 读事件
                            readHandler(key);
                            //在当前线程,这个方法可能会阻塞, 如果阻塞时间过长,其他的IO早就没电了
                            // 所以   为什么提出了IO Threads
                        } else if (key.isWritable()) {// 写事件
                            writeHandler(key);
                        }
                    }
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readHandler(SelectionKey key) throws IOException {
        SocketChannel client = null;
        try {
            client = (SocketChannel) key.channel();
            // 在acceptHandler 方法中注册的数组
            ByteBuffer buffer = (ByteBuffer) key.attachment();
            System.out.println("init: " + buffer);
            buffer.clear();
            int len = 0;
            while (true) {
                len = client.read(buffer);
                System.out.println(len);
                System.out.println("put: " + buffer);
                if (len > 0) {
                    buffer.flip();
                    System.out.println("flip: " + buffer);
                    byte[] cache = new byte[buffer.limit()];
                    buffer.get(cache);
                    System.out.println("get: " + buffer);
                    System.out.println("接收:" + new String(cache, 0, buffer.limit()));
                } else if (len == 0) {
                    break;
                } else {
                    client.close();
                    break;
                }

            }
        } catch (IOException e) {
            client.close();
            e.printStackTrace();
        }
    }

    private static AtomicInteger counter = new AtomicInteger();

    private void writeHandler(SelectionKey key) throws IOException {
        System.out.println("写数据计数：" + counter.incrementAndGet());
        key.interestOps(SelectionKey.OP_WRITE | SelectionKey.OP_READ); // 可能还需要读取客户端数据
        SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer data = ByteBuffer.allocate(1024);
        data.put("Hello client".getBytes());
        data.flip();// 写数据，需要把ByteBuffer切换为读模式，才能正确的写入数据
        channel.write(data);
    }

    private void acceptHandler(SelectionKey key) {
        try {
            ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
            // 目的是为了创建与客户端建立连接，此时key.attachment()==null
            SocketChannel client = ssc.accept();// 目的是调用accept接收客户端,  产生了一个新的fd5
            client.configureBlocking(false);

            ByteBuffer buffer = ByteBuffer.allocateDirect(8192);
            // 如果需要通道即能接收数据又能发送数据，需要设置ops为可读可写的通道，即 SelectionKey.OP_READ | SelectionKey.OP_WRITE
            client.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE, buffer);
            System.out.println("新客户端:" + client.getRemoteAddress());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
