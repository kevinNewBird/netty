package com.pomelo.rpc.jdk.server;

import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * 服务端需创建远程对象实例，并将其注册到 RMI 注册表（Registry）。
 */
public class RpcServer {

    public static void main(String[] args) throws RemoteException, AlreadyBoundException {
        // 创建远程对象
        RpcProtocolImpl rpcProtocol = new RpcProtocolImpl();

        // 创建本地 Registry（默认端口 1099）
        Registry registry = LocateRegistry.createRegistry(1099);

        // 将远程对象绑定到名称 "RpcProtocol"
        registry.bind("RpcProtocol", rpcProtocol);

        System.out.println("RMI 服务已启动，等待客户端调用...");
    }
}
