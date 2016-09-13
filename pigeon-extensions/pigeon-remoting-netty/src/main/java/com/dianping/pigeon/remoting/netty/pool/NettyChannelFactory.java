package com.dianping.pigeon.remoting.netty.pool;

import com.dianping.pigeon.remoting.netty.invoker.NettyClient;

/**
 * @author qi.yin
 *         2016/07/22  下午12:00.
 */
public class NettyChannelFactory implements PooledChannelFactory {

    private NettyClient client;

    public NettyChannelFactory(NettyClient client) {
        this.client = client;
    }

    @Override
    public PooledChannel createChannel() throws ChannelException {

        PooledChannel channel = new NettyChannel(
                client.getBootstrap(),
                client.getHost(),
                client.getPort(),
                client.getTimeout());

        try {
            channel.lock();

            channel.connect();
        } finally {
            channel.unLock();
        }

        return channel;
    }

}