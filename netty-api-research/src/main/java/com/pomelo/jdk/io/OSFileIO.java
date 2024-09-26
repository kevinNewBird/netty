package com.pomelo.jdk.io;


import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/**
 * description  磁盘IO测试验证程序 <BR>
 * <p>
 * author: zhao.song
 * date: created in 21:34  2021/7/13
 * company: TRS信息技术有限公司
 * version 1.0
 */
public class OSFileIO {

    static byte[] data = "1234456\n".getBytes(StandardCharsets.UTF_8);
    public static final String linux_path = "/mashibing/os/out.txt";
    public static final String window_path = "out.txt";

    static String path = window_path;

    public static void main(String[] args) throws Exception {
        switch (args[0]) {
            case "0":
                testBasicFileIO();
                break;
            case "1":
                testBufferedFileIO();
                break;
            case "2":
                whatByteBuffer();
                break;
            case "3":
                whatRandomAccessFileWrite();
                break;
            default:
                break;
        }
    }


    //最基本的file写
    public static void testBasicFileIO() throws Exception {
        File file = new File(path);
        FileOutputStream fos = new FileOutputStream(file);
        while (true) {
            // 睡眠会产生很多锁的系统调用的接口, 所以实际测试时需要注释掉
            TimeUnit.SECONDS.sleep(1);
            fos.write(data);
        }
    }

    // 测试buffer 文件io
    // 数据先进入数组jvm 8k -> 然后调用系统的内核[os kernel]
    public static void testBufferedFileIO() throws Exception {
        File file = new File(path);
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
        while (true) {
            // 睡眠会产生很多锁的系统调用的接口, 所以实际测试时需要注释掉
            TimeUnit.SECONDS.sleep(1);
            bos.write(data);
        }
    }

    // 基于字节数组IO
    //什么是byteBuffer, 简单理解为字节数组, 本质上和Buffered没有区别
    public static void whatByteBuffer() {
        ByteBuffer buffer = ByteBuffer.allocate(1024);// 分配在堆上
//        ByteBuffer buffer = ByteBuffer.allocateDirect(1024);// 分配在堆外

        System.out.println("position: " + buffer.position());
        System.out.println("limit: " + buffer.limit());
        System.out.println("capacity: " + buffer.capacity());
        System.out.println("mark: " + buffer);

        // 存入数据时, pos移动
        buffer.put("123".getBytes());
        System.out.println("-------------------put:123--------------------");
        System.out.println("mark: " + buffer);

        // 从写切换为读, 此时limit=pos, pos=0, 这样就能保证读取的时候不会发生异常[pos,limit]
        buffer.flip(); // 读写交替
        System.out.println("-------------------flip---------------------");
        System.out.println("mark: " + buffer);

        // 读取时, pos移动
        System.out.println(buffer.get());
        System.out.println("-------------------get---------------------");
        System.out.println("mark: " + buffer);

        // 从读切换到写,跳过读取的位置, 也就是pos向后移动, limit=cap
        buffer.compact();
        System.out.println("-------------------compact------------------------");
        System.out.println("mark: " + buffer);

        buffer.clear();
        System.out.println("-------------------clear----------------------");
        System.out.println("mark: " + buffer);
    }

    // 基于文件 NIO
    public static void whatRandomAccessFileWrite() throws Exception {
        RandomAccessFile raf = new RandomAccessFile(path, "rw");

        raf.write("hello mashibing\n".getBytes());
        raf.write("hello seanszhou\n".getBytes());
        System.out.println("write----------------------");
        System.in.read();

        raf.seek(4);// 偏移量值:4, 可以根据这个特性实现断点续传
        raf.write("ooxx".getBytes());// 也就是在hell 后继续写入了 ooxx覆盖掉四个字符, 也就是hellooxxshibing
        // 执行后,文件大小: 1k

        System.out.println("seek-----------------------");
        System.in.read();

        // 获取到文件的通道
        FileChannel rafChannel = raf.getChannel();
        // mmap后会得到一个 堆外 且和 文件映射的 bytebuffer
        // 只有文件可以做映射, 因为文件是块设备(只有块设备才能自由寻址, 其它流还是字节是没有映射的)
        // 文件大小会被分配到: 4k
        MappedByteBuffer map = rafChannel.map(FileChannel.MapMode.READ_WRITE, 0, 4096);

        // 减少了系统调用
        map.put("@@@".getBytes());// 不是系统调用,但是数据会达到 内核的 pageCache
        // 曾经我们是需要out.write() 这样的系统调用,才能让程序的data进入内核的pageCache
        // 曾经必须有用户态内核态切换
        // mmap 的内存映射, 依然是内核的 pageCache 体系约束的!!!
        // 换言之 ,丢数据
        // 你可以去github上找一些  其他c程序员写的jni扩展库, 使用linux内核的Direct IO
        // 直接IO 是忽略 linux 的pageCache (也就是自己管理 字节数组)
        // 是把 pageCache 交给了程序自己开辟一个字节数组当作 pageCache
        // , 动用代码逻辑来维护一致性/dirty...一系列复杂问题

        System.out.println("map--put------------------------");
        System.in.read();


//        map.force(); // flush

        raf.seek(0);

        ByteBuffer buffer = ByteBuffer.allocate(8192); // 分配在堆内
//        ByteBuffer buffer = ByteBuffer.allocateDirect(1024);// 分配在堆外

        int read = rafChannel.read(buffer);// 这里就是把rafChannel里的数据写入了buffer里
        System.out.println(buffer);

        buffer.flip(); // 读写翻转
        System.out.println(buffer);

        for (int i = 0; i < buffer.limit(); i++) {
            char c = (char) buffer.get(i);
            if (c == 0) {
                continue;
            }
            System.out.println((char) buffer.get(i));
        }

    }
}
