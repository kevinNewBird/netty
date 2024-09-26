package com.pomelo.jdk.reactor.v3;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.Channel;
import java.nio.channels.ServerSocketChannel;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * description  SelectorThreadGroup <BR>
 * <p>
 * author: zhao.song
 * date: created in 10:26  2021/7/19
 * company: TRS信息技术有限公司
 * version 1.0
 */
public class SelectorThreadGroup {// 天生都是boss

    SelectorThread[] sts;


    ServerSocketChannel server;

    AtomicInteger xid = new AtomicInteger(0);

    SelectorThreadGroup stg = this;

    public void setWorker(SelectorThreadGroup stg) {
        this.stg = stg;
    }

    public SelectorThreadGroup(int num) {
        // num 线程数
        sts = new SelectorThread[num];

        for (int i = 0; i < num; i++) {
            sts[i] = new SelectorThread(this);
            new Thread(sts[i]).start();
        }
    }

    public void bind(int port) {

        try {
            server = ServerSocketChannel.open();
            server.configureBlocking(false);

            server.bind(new InetSocketAddress(port));

            // TIP:注册到那个selector上呢?
            nextSelector(server);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 无论 serverSocket  socket 都复用这个方法
    public void nextSelector(Channel c) {
        try {
            if (c instanceof ServerSocketChannel) {
                SelectorThread st = next();  //listen 选择了 boss组中的一个线程后，要更新这个线程的work组
                st.lbq.put(c);
                st.setWorker(stg);
                st.selector.wakeup();
            } else {
                SelectorThread st = nextV3();
                // 1.通过队列传递数据/消息
                st.lbq.add(c);
                // 2.通过打断阻塞,让对应的线程去自己在打断后完成注册selector
                st.selector.wakeup();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        // 重点: c 可能是server , 也可能是 client
//        ServerSocketChannel s = (ServerSocketChannel) c;
//        try {
//            s.register(st.selector, SelectionKey.OP_ACCEPT);// 会被阻塞!!!
//            st.selector.wakeup(); // 功能是让 selecotr的 select 方法立刻返回,不阻塞!
//        } catch (ClosedChannelException e) {
//            e.printStackTrace();
//        }

    }

    //无论 serversocket  socket  都复用这个方法
    private SelectorThread next() {
        int index = xid.incrementAndGet() % sts.length;  //轮询就会很尴尬，倾斜
        return sts[index];
    }

    private SelectorThread nextV3() {
        if (sts.length == 1) {
            return sts[0];
        }
        int index = xid.incrementAndGet() % (stg.sts.length - 1);// 轮询就会很尴尬:倾斜
        return stg.sts[index + 1];
    }
}
