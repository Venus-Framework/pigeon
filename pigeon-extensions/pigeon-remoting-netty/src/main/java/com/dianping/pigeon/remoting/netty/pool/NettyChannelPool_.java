package com.dianping.pigeon.remoting.netty.pool;

/**
 * @author qi.yin
 *         2016/09/13  上午10:00.
 */
public class NettyChannelPool_ implements ChannelPool {



    @Override
    public int getSize() {
        return 0;
    }

    @Override
    public int getActive() {
        return 0;
    }

    @Override
    public int getIdle() {
        return 0;
    }

    @Override
    public PooledChannel borrowChannel() throws ChannelPoolException {
        return null;
    }

    @Override
    public void returnChannel(PooledChannel channel) {

    }

    @Override
    public PoolProperties getPoolProperties() {
        return null;
    }

    @Override
    public void close() {

    }
}
