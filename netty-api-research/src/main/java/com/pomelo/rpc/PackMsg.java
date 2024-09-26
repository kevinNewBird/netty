package com.pomelo.rpc;

/**
 * description  PackMsg <BR>
 * <p>
 * author: zhao.song
 * date: created in 10:11  2021/7/26
 * company: TRS信息技术有限公司
 * version 1.0
 */
public class PackMsg {

    MyRPCTest.MyHeader header;
    MyRPCTest.MyContent content;
    public PackMsg(MyRPCTest.MyHeader header, MyRPCTest.MyContent content) {
        this.header = header;
        this.content = content;
    }

}
