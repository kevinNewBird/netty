package com.pomelo.rpc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * description  SerDerUtil <BR>
 * <p>
 * author: zhao.song
 * date: created in 10:45  2021/7/26
 * company: TRS信息技术有限公司
 * version 1.0
 */
public class SerDerUtil {


    /**
     * description   序列化  <BR>
     *
     * @param msg:
     * @return {@link byte[]}
     * @author zhao.song  2021/7/26  10:59
     */
    public static byte[] serializable(Object msg) {
        ByteArrayOutputStream out = null;
        try {
            out = new ByteArrayOutputStream();
            out.reset();
            ObjectOutputStream oout = new ObjectOutputStream(out);
            oout.writeObject(msg);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return out.toByteArray();
    }


    /**
     * description   反序列化  <BR>
     *
     * @param array:
     * @return {@link Object}
     * @author zhao.song  2021/7/26  11:06
     */
    public static Object deserializable(byte[] array) {
        try (ByteArrayInputStream in = new ByteArrayInputStream(array);
             ObjectInputStream oin = new ObjectInputStream(in)) {
            return oin.readObject();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }
}
