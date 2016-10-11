package com.dianping.pigeon.remoting.common.channel;

import com.dianping.pigeon.remoting.common.exception.NetworkException;

import java.net.InetSocketAddress;

/**
 * @author qi.yin
 *         2016/09/23  上午10:27.
 */
public interface Channel {

    void connect() throws NetworkException;

    void disConnect();

    void write(Object message) throws NetworkException;

    boolean isWritable();

    boolean isAvaliable();

    InetSocketAddress getLocalAddress();

    InetSocketAddress getRemoteAddress();

    String getRemoteAddressString();
}
