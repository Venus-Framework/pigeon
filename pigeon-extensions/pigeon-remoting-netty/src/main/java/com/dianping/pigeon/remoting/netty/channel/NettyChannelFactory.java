package com.dianping.pigeon.remoting.netty.channel;

import com.dianping.pigeon.log.Logger;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.remoting.common.channel.ChannelFactory;
import com.dianping.pigeon.remoting.common.exception.NetworkException;
import com.dianping.pigeon.remoting.common.pool.ChannelPoolException;
import com.dianping.pigeon.remoting.netty.invoker.NettyClient;

/**
 * @author qi.yin
 *         2016/09/23  上午10:47.
 */
public class NettyChannelFactory implements ChannelFactory<NettyChannel> {
    private static final Logger logger = LoggerLoader.getLogger(NettyChannelFactory.class);

    private NettyClient client;

    public NettyChannelFactory(NettyClient client) {
        this.client = client;
    }

    @Override
    public NettyChannel createChannel() {

        NettyChannel channel = new DefaultNettyChannel(
                client.getBootstrap(),
                client.getHost(),
                client.getPort(),
                client.getTimeout());
        try {
            channel.connect();
        } catch (NetworkException e) {
            logger.warn("[createChannel] failed.", e);
        }

        return channel;
    }
}
