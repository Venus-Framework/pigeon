package com.dianping.pigeon.remoting.netty.pool;

import org.jboss.netty.channel.ChannelFuture;

import java.net.InetSocketAddress;

/**
 * @author qi.yin
 *         2016/07/21  上午11:12.
 */
public interface PooledChannel {

    void connect() throws ChannelException;

    void reconnect() throws ChannelException;

    ChannelFuture write(Object message) throws ChannelException;

    boolean isWritable();

    boolean isActive();

    InetSocketAddress getLocalAddress();

    InetSocketAddress getRemoteAddress();

}