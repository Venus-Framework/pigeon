package com.dianping.pigeon.remoting.netty.pool;

import java.util.List;

/**
 * @author qi.yin
 *         2016/07/21  上午11:05.
 */
public interface ChannelPool {

    int getSize();

    int getActive();

    PooledChannel selectChannel() throws ChannelPoolException;

    List<PooledChannel> getChannels();

    PoolProperties getPoolProperties();

    void close();

}