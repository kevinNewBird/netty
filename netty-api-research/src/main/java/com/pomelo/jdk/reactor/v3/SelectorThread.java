package com.pomelo.jdk.reactor.v3;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * description  SelectorThread <BR>
 * <p>
 * author: zhao.song
 * date: created in 9:50  2021/7/19
 * company: TRS信息技术有限公司
 * version 1.0
 */
public class SelectorThread extends ThreadLocal<LinkedBlockingQueue<Channel>> implements Runnable {


    // 每线程对应一个selecotr:
    // 多线程情况下, 该主机,干程序的并发客户端被分配到多个selector上
    // 注意, 每个客户端,只绑定到其中一个selector
    // 其实不会有交互问题


    Selector selector = null;

    // 通讯队列(硬编码->灵活编码)
    LinkedBlockingQueue<Channel> lbq = get();

    @Override
    protected LinkedBlockingQueue<Channel> initialValue() {
        return new LinkedBlockingQueue<>();
    }

    SelectorThreadGroup stg;

    public void setWorker(SelectorThreadGroup stgWorker) {
        this.stg = stgWorker;
    }

    public SelectorThread(SelectorThreadGroup stg) {
        try {
            this.stg = stg;
            this.selector = Selector.open();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void run() {
        try {
            while (true) {
                // 查看多路复用器是否有状态的事件
                // 1. select()
                System.out.println("[" + Thread.currentThread().getName() + "]" + "before select...." + selector.keys().size());
                int nums = selector.select(); // 阻塞, 没有设置超时时间, 可通过wakeup唤醒
//                TimeUnit.SECONDS.sleep(1); // 这绝对不是解决方案,只是为了演示
                System.out.println("[" + Thread.currentThread().getName() + "]" + "after select...." + selector.keys().size());
                // 2.处理selectedKeys
                if (nums > 0) {
                    Set<SelectionKey> keys = selector.selectedKeys();
                    Iterator<SelectionKey> iter = keys.iterator();
                    while (iter.hasNext()) {
                        SelectionKey key = iter.next();
                        iter.remove();
                        if (key.isAcceptable()) {
                            // 复杂: 接收客户端的过程 (接收之后,要注册,那么在多线程下,新的客户端注册到哪里呢)
                            acceptHandler(key);
                        } else if (key.isReadable()) {
                            readHandler(key);
                        } else if (key.isWritable()) {

                        }
                    }
                }

                //3.处理一些task
                if (!lbq.isEmpty()) {
                    Channel c = lbq.take();
                    if (c instanceof ServerSocketChannel) {
                        ServerSocketChannel server = (ServerSocketChannel) c;
                        server.register(selector, SelectionKey.OP_ACCEPT);
                    } else if (c instanceof SocketChannel) {
                        SocketChannel client = (SocketChannel) c;
                        ByteBuffer buffer = ByteBuffer.allocateDirect(4098);
                        client.register(selector, SelectionKey.OP_READ, buffer);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * description   连接接收处理  <BR>
     *
     * @param key:
     * @return
     * @author zhao.song  2021/7/19  10:03
     */
    private void acceptHandler(SelectionKey key) {

        System.out.println(Thread.currentThread().getName() + "  acceptHandler....");

        ServerSocketChannel server = (ServerSocketChannel) key.channel();
        try {
            SocketChannel client = server.accept();
            client.configureBlocking(false);

            // 单线程: 该客户端注册到服务端的selector上(client.register(selector,...))
            // 多线程下: 当前的selector注册到其他的, choose a selector and register
            stg.nextSelector(client);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    /**
     * description   读数据接受处理  <BR>
     *
     * @param key:
     * @return
     * @author zhao.song  2021/7/19  10:14
     */
    private void readHandler(SelectionKey key) {
        System.out.println(Thread.currentThread().getName() + "  readHandler....");
        ByteBuffer buffer = (ByteBuffer) key.attachment();
        SocketChannel client = (SocketChannel) key.channel();
        buffer.clear();
        while (true) {
            try {
                int num = client.read(buffer);
                if (num > 0) {
                    buffer.flip();
                    while (buffer.hasRemaining()) {
                        client.write(buffer);
                    }
                    buffer.clear();
                } else if (num == 0) {
                    break;
                } else {// 客户端断开了
                    System.out.println("client : " + client.getRemoteAddress() + " closed...");
                    key.cancel();
//                    client.close();
                    break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
