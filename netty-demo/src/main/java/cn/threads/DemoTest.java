package cn.threads;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * description  DemoTest <BR>
 * <p>
 * author: zhao.song
 * date: created in 9:28  2022/11/23
 * company: TRS信息技术有限公司
 * version 1.0
 */
public class DemoTest {
    private static final AtomicInteger idx = new AtomicInteger();

    public static void main(String[] args) {
        int idx = DemoTest.idx.getAndIncrement();
        int length = idx & 2 - 1;
        System.out.println(idx);
        System.out.println(idx&2);
        System.out.println(length);
        System.out.println("------------------------");

        int idx2 = DemoTest.idx.getAndIncrement();
        // 算术运算符的优先级 > 位运算符
        int length2 = idx2 & (4 - 1);
        System.out.println(idx2);
        System.out.println(idx2&2);
        System.out.println(length2);
    }

    public DemoTest2 newChild() {
        return new DemoTest2(this);
    }
}
