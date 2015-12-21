package com.dianping.pigeon.remoting.common.codec;

/**
 * Created by chenchongze on 15/12/16.
 */
public interface SerializerRegister {

    public boolean isRegistered();

    public void registerSerializer();
}
