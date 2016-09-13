package com.dianping.pigeon.remoting.netty.pool;

/**
 * @author qi.yin
 *         2016/07/21  上午11:05.
 */
public interface ChannelPool {

    int getSize();

    int getActive();

    int getIdle();

    PooledChannel borrowChannel() throws ChannelPoolException;

    void returnChannel(PooledChannel channel);

    PoolProperties getPoolProperties();

    void close();

}