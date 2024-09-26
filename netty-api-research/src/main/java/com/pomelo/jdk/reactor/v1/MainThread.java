package com.pomelo.jdk.reactor.v1;

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
        SelectorThreadGroup stg = new SelectorThreadGroup(1);
        // 混杂模式: 只有一个线程负责accept ,每个都会被分配client , 进行R/W
//        SelectorThreadGroup stg = new SelectorThreadGroup(3);

        // 2. 应该把监听的server 注册到某一个selector上
        stg.bind(9999);
    }
}
