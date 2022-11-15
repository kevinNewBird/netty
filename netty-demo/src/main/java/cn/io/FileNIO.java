package cn.io;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * description  FileNIO <BR>
 * <p>
 * author: zhao.song
 * date: created in 10:17  2022/8/2
 * company: TRS信息技术有限公司
 * version 1.0
 */
public class FileNIO {

    /**
     * jdk nio读取文件的原理剖析
     *
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        // NIO模型，本质是在原IO的模型上进行了封装
        final FileInputStream fis = new FileInputStream("D:\\data\\1.log");
        // 搭桥，获取通道（本质就是新建对象new）
        final FileChannel fc = fis.getChannel();
        // 分配数据缓存区（传递数据），内存分为...
        // 分配的是HeapByteBuffer内存（堆内内存）
        final ByteBuffer buf = ByteBuffer.allocate(10);
        /*
         往下扒拉源码：
         1、读取时首先通过Util.getTemporaryDirectBuffer(var1.remaining())创建堆外内存DirectByteBuffer
         2、readIntoNativeBuffer方法将读取的内容放到堆外内存DirectByteBuffer中
         3、将堆外内存的字节赋给ByteBuffer buf
         4、如果我们指定的是堆外内存，那么直接步骤2即可

         总结：是对inputstream/outputstream的封装，调用c的io_read函数或者io_write函数
         */
        fc.read(buf);
        buf.flip();
        System.out.println((char) buf.get());

        // 使用mark和reset完成循环读取
        buf.mark();
        buf.reset();
        System.out.println("remaining:" + buf.remaining());
        int remaining = buf.remaining();
        for (int i = 1; i < 10; i++) {
            System.out.println((char) buf.get());
            if (i % remaining == 0) {
                buf.reset();
            }
        }
    }
}
