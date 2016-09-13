package com.dianping.pigeon.remoting.netty.pool;

import org.jboss.netty.channel.ChannelFuture;

import java.net.InetSocketAddress;

/**
 * @author qi.yin
 *         2016/07/21  上午11:12.
 */
public interface PooledChannel {

    void connect() throws ChannelException;

    boolean release();

    ChannelFuture write(Object message) throws ChannelException;

    boolean isActive();

    void lock();

    void unLock();

    long getTimestamp();

    void setTimestamp(long timestamp);

    InetSocketAddress getLocalAddress();

    InetSocketAddress getRemoteAddress();

}