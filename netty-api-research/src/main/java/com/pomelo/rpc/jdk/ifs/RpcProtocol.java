package com.pomelo.rpc.jdk.ifs;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * jdk的rmi，即远程方法调用
 * 远程接口必须继承 java.rmi.Remote，且所有方法需声明抛出 RemoteException。
 */
public interface RpcProtocol extends Remote {
    // 定义协议版本ID
    static final long versionID = 1L;

    // 定义rpc的登录方法
    String login(String username, String password) throws RemoteException;
}
