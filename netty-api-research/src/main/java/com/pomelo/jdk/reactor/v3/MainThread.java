package com.pomelo.jdk.reactor.v3;

/**
 * description  MainThread <BR>
 * <p>
 * author: zhao.song
 * date: created in 9:50  2021/7/19
 * company: TRS信息技术有限公司
 * version 1.0
 */

public class MainThread {

    public static void main(String[] args) {
        // 不做IO和业务的事情, 处理主从的孵化

        // 1.创建 IO Thread (一个或者多个)
        SelectorThreadGroup boss = new SelectorThreadGroup(3);
        // boss 有自己的线程组
        SelectorThreadGroup worker = new SelectorThreadGroup(3);
        // worker 有自己的线程组

        // 2. 应该把监听的server 注册到某一个selector上
        boss.setWorker(worker);
        //但是boss得 多持有worker的引用
        /**
         * boss里选一个线程注册listen, 触发bind.从而 ,这个补选中的线程得持有workerGroup的引用
         * 因为未来 listen 一旦 accept 得到 client后得去worker中next出一个线程分配
         */
        boss.bind(9999);
    }
}
