package com.pomelo.rpc.jdk.client;

import com.pomelo.rpc.jdk.ifs.RpcProtocol;

import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class RpcClient {

    public static void main(String[] args) throws RemoteException, NotBoundException {
        // 获取本地 Registry（假设服务端在同一主机）
        Registry registry = LocateRegistry.getRegistry(1099);
        // 查找名为“RpcProtocol”的远程对象
        RpcProtocol service = (RpcProtocol) registry.lookup("RpcProtocol");

        // 调用远程方法
        String result = service.login("rmi", "123456");
        System.out.println("服务端返回：" + result);
    }
}
