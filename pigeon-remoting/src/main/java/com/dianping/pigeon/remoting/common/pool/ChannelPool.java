package com.dianping.pigeon.remoting.common.pool;

import com.dianping.pigeon.remoting.common.channel.Channel;

import java.util.List;

/**
 * @author qi.yin
 *         2016/07/21  上午11:05.
 */
public interface ChannelPool<C extends Channel> {

    int getSize();

    boolean isAvaliable();

    C selectChannel() throws ChannelPoolException;

    List<C> getChannels();

    PoolProperties getPoolProperties();

    boolean isClosed();

    void close();

}