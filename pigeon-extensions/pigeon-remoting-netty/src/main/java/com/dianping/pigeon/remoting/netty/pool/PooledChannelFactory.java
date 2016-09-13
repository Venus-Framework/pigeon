package com.dianping.pigeon.remoting.netty.pool;

/**
 * @author qi.yin
 *         2016/07/21  下午3:03.
 */
public interface PooledChannelFactory {

    PooledChannel createChannel() throws ChannelException;

}