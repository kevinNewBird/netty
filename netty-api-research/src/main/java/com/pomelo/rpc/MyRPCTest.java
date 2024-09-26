package com.pomelo.rpc;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.Data;
import org.junit.Test;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * description  MyRPCTest <BR>
 * <p>
 * author: zhao.song
 * date: created in 22:15  2021/7/21
 * company: TRS信息技术有限公司
 * version 1.0
 */
public class MyRPCTest implements Serializable {

    public static final int HEADER_LENGTH = 106;

    public static void main(String[] args) {

        byte[] src = {1, 2};
        byte[] dest = {3, 4};
        byte[] combine = new byte[src.length + dest.length];
        System.arraycopy(src, 0, combine, 0, src.length);
        System.arraycopy(dest, 0, combine, src.length, dest.length);
        System.out.println(Arrays.toString(combine));
        System.out.println("---------------------------------");

        Object[] objects = new Object[2];
        System.out.println(objects[0]);

        for (int i = 0; i < 100; i++) {
            UUID uuid = UUID.randomUUID();
            System.out.println("least : " + uuid.getLeastSignificantBits());
            System.out.println("most  : " + uuid.getMostSignificantBits());
            System.out.println("--------------------------------------");
        }
    }

    /**
     * 1.先假设一个需求,写一个rpc
     * 2.来回通信,连接数量,拆包?
     * 3.动态代理,序列化,协议封装
     * 4.连接池
     * 5.就像调用本地方法一样调用远程方法, 面向java中就是所谓的 面向interface开发
     */

    /**
     * description   模拟consumer端  <BR>
     *
     * @param :
     * @return
     * @author zhao.song  2021/7/22  10:13
     */
    @Test
    public void get() throws IOException {

        new Thread(() -> {
            startServer();
        }).start();

        try {
            TimeUnit.SECONDS.sleep(1);
            System.out.println("server started...");// 系统调用: 为了服务端首先启动
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // 小问题, 当并发通过一个连接发送后,服务端解析bytebuf转对象的过程中出错
        // 问题描述: clientPool 只有一个连接, 也就是说套接字socket相同, 不同线程的发送会首先进入到kernel的recv-queue
        int size = 20;
        Thread[] threads = new Thread[size];
        AtomicInteger num = new AtomicInteger(0);
        for (int i = 0; i < size; i++) {
            threads[i] = new Thread(() -> {
                Car car = proxyGet(Car.class); //动态代理
                String srcArg = "hello" + num.incrementAndGet();
                String res = car.ooxx(srcArg);
                System.out.println("client over msg## " + res + " @@  src arg: " + srcArg);
//                Fly fly = proxyGet(Fly.class);
//                fly.xxoo("hello");
            });
        }

        for (Thread thread : threads) {
            thread.start();
        }

        System.in.read();


    }


    public <T> T proxyGet(Class<T> clazz) {
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                // 如何设计我们的consumer对provider的调用过程

                // 1.调用 服务,方法,参数 ==> 封装成message  [content]
                String clazzName = clazz.getName();// 接口名 == 服务名
                String methodName = method.getName();//方法名
                Class<?>[] parameterTypes = method.getParameterTypes();
                MyContent content = new MyContent();
                content.setArgs(args);
                content.setClazzName(clazzName);
                content.setMethodName(methodName);
                content.setParameterTypes(parameterTypes);

                byte[] msgBody = SerDerUtil.serializable(content);

                // 2.requestID + message , 本地要缓存
                // 协议: [header<>]+[msgBody]
                MyHeader header = createHeader(msgBody);
                // TODO: 解决数据decode问题
                byte[] msgHeader = SerDerUtil.serializable(header);
//                System.out.println("msgHeader @length:  " + msgHeader.length);

                // 3.连接池 :: 取得连接
                ClientFactory factory = ClientFactory.getInstance();

                NioSocketChannel clientChannel = factory.getClient(new InetSocketAddress("127.0.0.1", 9090));
                // 获取连接过程中:  开始-创建, 过程-直接取

                // 4.发送 --> 走IO   out   --> 走 netty (event 驱动)
//                byte[] msgCombine = new byte[msgHeader.length + msgBody.length];
//                System.arraycopy(msgHeader, 0, msgCombine, 0, msgHeader.length);
//                System.arraycopy(msgBody, 0, msgCombine, msgHeader.length, msgHeader.length);
                long id = header.getRequestId();
                CompletableFuture<String> future = new CompletableFuture();
//                CountDownLatch countDownLatch = new CountDownLatch(1);
                ResponseMappingCallBack.addCallBack(id, future);

                ByteBuf byteBuf = PooledByteBufAllocator.DEFAULT.directBuffer(msgHeader.length + msgBody.length);
                byteBuf.writeBytes(msgHeader);
                byteBuf.writeBytes(msgBody);
                ChannelFuture channelFuture = clientChannel.writeAndFlush(byteBuf);
                channelFuture.sync(); // 阻塞,待其发送完成(io是双向的, 你看似有个sync,它仅代表out)

//                countDownLatch.await();

                // 5.?, 如果从IO, 未来回来了, 怎么将代码执行到这里
                // {睡眠/回调}

                return future.get();//阻塞的
            }
        });
    }

    /**
     * description   构建协议头  <BR>
     *
     * @param msgBody:
     * @return {@link MyHeader}
     * @author zhao.song  2021/7/22  11:22
     */
    public MyHeader createHeader(byte[] msgBody) {
        MyHeader header = new MyHeader();
        int size = msgBody.length;
        // 0x14    0001  0100
        int f = 0x14141414;
        long requestId = Math.abs(UUID.randomUUID().getLeastSignificantBits());
        header.setFlag(f);
        header.setDataLen(size);
        header.setRequestId(requestId);
        return header;
    }

    /**
     * description   [正确] channelRead读取数据  <BR>
     *
     * @param bufIn:
     * @return
     * @author zhao.song  2021/7/23  16:55
     */
    private static void correctChannelRead(ByteBuf bufIn, List<Object> out) throws Exception {
        while (bufIn.readableBytes() >= HEADER_LENGTH) {
            byte[] headerCache = new byte[HEADER_LENGTH];
            bufIn.getBytes(bufIn.readerIndex(), headerCache); // 不会移动指针,即readerIndex不变
//            ByteArrayInputStream in = new ByteArrayInputStream(headerCache);
//            ObjectInputStream oin = new ObjectInputStream(in);
//            MyHeader header = (MyHeader) oin.readObject();
            MyHeader header = (MyHeader) SerDerUtil.deserializable(headerCache);

//            System.out.println("server @requestId:  " + header.getRequestId());
            // 如果是一个完整的数据包, 就获取出来; 不完整的就传入到下个buf, 在下次进行处理
            // 通信协议: decode 在两个方向都使用
            if (bufIn.readableBytes() >= (header.getDataLen() + HEADER_LENGTH)) {
                bufIn.readBytes(HEADER_LENGTH);
                byte[] data = new byte[(int) header.getDataLen()];
                bufIn.readBytes(data);
                // 服务端接收
                if (header.getFlag() == 0x14141414) {
                    // 处理指针,移动指针到数据data索引位置
                    MyContent content = (MyContent) SerDerUtil.deserializable(data);
//                    System.out.println(content.getClazzName());
                    out.add(new PackMsg(header, content));
                    // 服务端返回
                } else if (header.getFlag() == 0x14141424) {
                    MyContent content = (MyContent) SerDerUtil.deserializable(data);
                    out.add(new PackMsg(header, content));
                }

            } else {
                // 本次留存的buf, 拼接到下次的buf
//                System.out.println("server buf end @size:  " + buf.readableBytes());
                break;

            }

//            MyHeader header = (MyHeader) SerDerUtil.deserializable(headerCache);
//                System.out.println(header.dataLen);


        }
    }

    /**
     * description   [错误] channelRead读取数据  <BR>
     *
     * @param buf:
     * @return
     * @author zhao.song  2021/7/23  16:55
     */
    private static void errorChannelRead(ByteBuf buf) throws Exception {
        if (buf.readableBytes() >= HEADER_LENGTH) {
            byte[] headerCache = new byte[HEADER_LENGTH];
            buf.readBytes(headerCache);
            MyHeader header = (MyHeader) SerDerUtil.deserializable(headerCache);
//                System.out.println(header.dataLen);
//            System.out.println("server @requestId:  " + header.getRequestId());

            if (buf.readableBytes() >= header.getDataLen()) {
                byte[] data = new byte[(int) header.getDataLen()];
                buf.readBytes(data);
                MyContent content = (MyContent) SerDerUtil.deserializable(data);
//                System.out.println(content.getClazzName());
            } else {
                System.out.println("server buf end @size:  " + buf.readableBytes());
            }
        }
    }

    interface Car {

        String ooxx(String msg);
    }


    interface Fly {
        void xxoo(String msg);
    }

    public static void startServer() {
        NioEventLoopGroup boss = new NioEventLoopGroup(50);
        ServerBootstrap bootstrap = new ServerBootstrap();

        try {
            ChannelFuture future = bootstrap.group(boss, boss)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            System.out.println("server accept client: " + socketChannel.remoteAddress().getPort());
                            socketChannel.pipeline()
                                    // TIP:
                                    .addLast(new ServerDecode())
                                    .addLast(new ServerResponse());
                        }
                    }).bind(new InetSocketAddress(9090)).sync();
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }


    public static class ServerDecode extends ByteToMessageDecoder {

        // 父类里一定有channelRead()  -> byteBuf
        // 前老的拼buf  decode(); 剩余留存
        @Override
        protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf in, List<Object> out) throws Exception {
            //
            correctChannelRead(in, out);
        }
    }


    public static class ServerResponse extends ChannelInboundHandlerAdapter {
        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            System.out.println("server active...");
        }

        // provider: netty.channelRead不能保证数据的完整性; 而且,不是一次处理一个message
        //思考下解决方法?
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            // 1.使用解码器前
//            ByteBuf buf = (ByteBuf) msg;
//            ByteBuf sendBuf = buf.copy();
//            System.out.println("server buf start @size:  " + buf.readableBytes());
//            // 判断数据包的协议头
//            // 32+64+64 = 160 (int 4byte; long 8byte)
//            errorChannelRead(buf);
//
//            ChannelFuture channelFuture = ctx.writeAndFlush(sendBuf);
//            channelFuture.sync();

            // 2.使用解码器后
            PackMsg requestPkg = (PackMsg) msg;
//            System.out.println(requestPkg.content.getArgs()[0]);

            // 2.1.如果假设处理完了,要给客户端返回了~!!!
            // 你需要注意哪些环节~!!!!!

            // TIP:
            // 1. 返回的数据类型要求: byteBuf
            // 2. 因为是个RPC, 得有requestID!!!
            // 3. 关注rpc通信协议, 来的时候flag 0x14141414, 返回: 有新的header+content
            String ioThreadName = Thread.currentThread().getName();
            // 1.直接在当前方法,处理IO和业务返回
            // 2. 使用netty自己的eventloop来处理业务及返回
            // 3. 自己创建线程池
//            ctx.executor().execute(new Runnable() {
            ctx.executor().parent().next().execute(new Runnable() {

                @Override
                public void run() {
                    String execThreadName = Thread.currentThread().getName();
                    MyContent content = new MyContent();
                    String resData = "io thread:" + ioThreadName + "  exec thread: " + execThreadName
                            + " from args:" + requestPkg.content.getArgs()[0];
//                    System.out.println(resData);
                    content.setRes(resData);
                    byte[] contentSer = SerDerUtil.serializable(content);
                    MyHeader responseHeader = new MyHeader();
                    responseHeader.setRequestId(requestPkg.header.getRequestId());
                    responseHeader.setFlag(0x14141424);
                    responseHeader.setDataLen(contentSer.length);
                    byte[] headerSer = SerDerUtil.serializable(responseHeader);
                    ByteBuf byteBuf = PooledByteBufAllocator.DEFAULT.directBuffer(headerSer.length + contentSer.length);
                    byteBuf.writeBytes(headerSer)
                            .writeBytes(contentSer);
                    // 106 length
//                    System.out.println("server send header length:  "+headerSer.length);
                    // 发送时,一定要确保协议
                    ctx.writeAndFlush(byteBuf);

                }
            });

        }


        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            cause.printStackTrace();
//            ctx.channel().close();
        }
    }

    /**
     * description: 发送内容对象
     */
    @Data
    public static class MyContent implements Serializable {
        String clazzName;
        String methodName;
        Class<?>[] parameterTypes;
        Object[] args;
        String res;
    }

    /**
     * description: 发送协议头对象
     */
    @Data
    public static class MyHeader implements Serializable {
        //通讯上的协议
        /**
         * 1. ooxx值
         * 2. uuid: requestId
         * 3. DATA_LEN
         */
        int flag;  //32bit可以设置很多信息
        long requestId;
        long dataLen;
    }


    /**
     * description: 主要用于接收到服务端的返回后, 使代理方法的countDownLatch继续往下执行
     */
    public static class ResponseMappingCallBack {
        // 使用runnable的主要目的是为了解耦(也可以自己实现一个)
        static ConcurrentHashMap<Long, CompletableFuture> mapping = new ConcurrentHashMap<>();

        public static void addCallBack(long requestId, CompletableFuture cb) {
            mapping.putIfAbsent(requestId, cb);
        }

        public static void runCallBack(PackMsg msg) {
            CompletableFuture runnable = mapping.get(msg.header.getRequestId());
            if (runnable != null) {
                runnable.complete(msg.content.getRes());
                removeCallBack(msg.header.getRequestId());
            }

        }

        private static void removeCallBack(long requestId) {
            mapping.remove(requestId);
        }
    }


    /**
     * description: 连接池对象
     */
    public static class ClientPool {
        NioSocketChannel[] clients;

        Object[] lock;


        public ClientPool(int size) {
            clients = new NioSocketChannel[size];// init  连接都是空的
            lock = new Object[size];// 锁是可以初始化的

            for (int i = 0; i < size; i++) {
                lock[i] = new Object();

            }
        }
    }


    /**
     * description: 连接池工厂对象 (源于 spark 源码)
     */
    public static class ClientFactory {

        NioEventLoopGroup clientWorker;

        int poolSize = 10;

        Random rand = new Random();

        // 一个consumer可以连接很多的provider, 每一个provider都有自己的pool(K,V)
        ConcurrentHashMap<InetSocketAddress, ClientPool> outBoxs = new ConcurrentHashMap<>();


        private ClientFactory() {
        }


        private static class ClientFactoryHolder {
            public static final ClientFactory instance = new ClientFactory();
        }

        public static ClientFactory getInstance() {
            return ClientFactoryHolder.instance;
        }


        public synchronized NioSocketChannel getClient(InetSocketAddress address) {
            ClientPool clientPool = outBoxs.get(address);
            if (clientPool == null) {
//                System.out.println("joking....");
                outBoxs.putIfAbsent(address, new ClientPool(poolSize));
                clientPool = outBoxs.get(address);
            }
            int index = rand.nextInt(poolSize);
//            System.out.println("client pool size: " + clientPool.clients.length);
            if (clientPool.clients[index] != null
                    && clientPool.clients[index].isActive()) {
                return clientPool.clients[index];
            }

            synchronized (clientPool.lock[index]) {
                return clientPool.clients[index] = create(address);
            }
        }

        // 获取一个客户端连接
        private NioSocketChannel create(InetSocketAddress address) {
            clientWorker = new NioEventLoopGroup(1);

            Bootstrap bootstrap = new Bootstrap();
            try {
                ChannelFuture future = bootstrap.group(clientWorker)
                        .channel(NioSocketChannel.class)
                        .handler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel socketChannel) throws Exception {
                                socketChannel.pipeline()
                                        .addLast(new ServerDecode())
                                        .addLast(new ClientResponse());// 解决给谁的? requestID...
                            }
                        }).connect(address).sync();

                return (NioSocketChannel) future.channel();// 如何返回呢? closeFuture会阻塞
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        /**
         * description: netty客户端处理对象
         */
        private static class ClientResponse extends ChannelInboundHandlerAdapter {

            @Override
            public void channelActive(ChannelHandlerContext ctx) throws Exception {
                System.out.println("client active...");
            }

            @Override
            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                PackMsg responsePkg = (PackMsg) msg;
                // callback
                // 曾经没有考虑返回的事情
                ResponseMappingCallBack.runCallBack(responsePkg);

//                ByteBuf buf = (ByteBuf) msg;
//                // 判断数据包的协议头
//                // 32+64+64 = 160 (int 4byte; long 8byte)
//                if (buf.readableBytes() >= HEADER_LENGTH) {
//                    byte[] headerCache = new byte[HEADER_LENGTH];
//                    buf.readBytes(headerCache);
//                    MyHeader header = (MyHeader) SerDerUtil.deserializable(headerCache);
////                    System.out.println(header.dataLen);
//                    // 找到原有的线程,让其的代码继续往下执行
//                    System.out.println("client @requestId:  " + header.getRequestId());
//                    ResponseMappingCallBack.runCallBack(header.getRequestId());
//
////                    // 这一部分应该是server端的代码
////                    if (buf.readableBytes() >= header.getDataLen()) {
////                        byte[] data = new byte[(int) header.getDataLen()];
////                        buf.readBytes(data);
////                        in.reset();
////                        in = new ByteArrayInputStream(headerCache);
////                        oin = new ObjectInputStream(in);
////                        MyContent content = (MyContent) oin.readObject();
////                        System.out.println(content.getClazzName());
////                    }
//                }

            }

            @Override
            public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                cause.printStackTrace();
            }
        }

    }


}


