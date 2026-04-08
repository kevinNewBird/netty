package com.pomelo.rpc.jdk.server;

import com.pomelo.rpc.jdk.ifs.RpcProtocol;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * 服务实现类需继承 UnicastRemoteObject（用于导出远程对象），并实现自定义接口RpcProtocol
 */
public class RpcProtocolImpl extends UnicastRemoteObject implements RpcProtocol {


    protected RpcProtocolImpl() throws RemoteException {
        super();
    }

    @Override
    public String login(String username, String password) {
        return "用户 " + username + " 登录成功！";
    }
}
