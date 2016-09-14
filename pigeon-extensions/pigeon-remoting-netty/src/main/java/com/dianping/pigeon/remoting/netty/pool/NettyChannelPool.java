package com.dianping.pigeon.remoting.netty.pool;

import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.log.Logger;
import com.dianping.pigeon.util.AtomicPositiveInteger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author qi.yin
 *         2016/09/07  上午11:57.
 */
public class NettyChannelPool implements ChannelPool {

    private static final Logger logger = LoggerLoader.getLogger(NettyChannelPool.class);

    private List<PooledChannel> pooledChannels = new ArrayList<PooledChannel>();

    private AtomicInteger size = new AtomicInteger();

    private AtomicPositiveInteger selectedIndex = new AtomicPositiveInteger(0);

    private AtomicBoolean isClosed = new AtomicBoolean(true);

    private PoolProperties properties;

    private PooledChannelFactory channelFactory;

    public NettyChannelPool(PooledChannelFactory channelFactory)
            throws ChannelPoolException {
        this(new PoolProperties(), channelFactory);
    }

    public NettyChannelPool(PoolProperties properties, PooledChannelFactory channelFactory)
            throws ChannelPoolException {

        this.properties = properties;
        this.channelFactory = channelFactory;
        init(properties);
        isClosed.compareAndSet(true, false);
    }

    private void init(PoolProperties properties) throws ChannelPoolException {
        if (properties.getMaxActive() < 1) {
            logger.warn("[init] maxActive is smaller than 1, setting maxActive to " + PoolProperties.DEFAULT_MAX_ACTIVE);
            properties.setMaxActive(PoolProperties.DEFAULT_MAX_ACTIVE);
        }
        if (properties.getInitialSize() > properties.getMaxActive()) {
            logger.warn("[init] initialSize is larger than maxActive, setting initialSize to" + properties.getMaxActive());
            properties.setInitialSize(properties.getMaxActive());
        }

        PooledChannel[] initialPools = new NettyChannel[properties.getInitialSize()];
        try {

            for (int i = 0; i < properties.getInitialSize(); i++) {
                initialPools[i] = selectChannel();
            }

        } catch (ChannelPoolException e) {
            logger.error("[init] unable to create initial connections of pool.", e);
            close();
            throw e;
        }
    }

    @Override
    public int getSize() {
        return 0;
    }

    @Override
    public int getActive() {
        return 0;
    }

    @Override
    public PooledChannel selectChannel() throws ChannelPoolException {
        if (isClosed()) {
            throw new ChannelPoolException("Channel pool is closed.");
        }

        long now = System.currentTimeMillis();

        long maxWait = (properties.getMaxWait() <= 0) ? Long.MAX_VALUE : properties.getMaxWait();

        PooledChannel channel = null;

        do {
            channel = doSelectChannel();

            if (channel != null) {
                return channel;
            }

            if (size.get() < properties.getMaxActive()) {

                if (size.incrementAndGet() > properties.getMaxActive()) {
                    size.decrementAndGet();
                } else {
                    return createChannel();
                }
            }

            if (!pooledChannels.isEmpty()) {
                return pooledChannels.get(selectedIndex.getAndIncrement() % pooledChannels.size());
            } else {
                if (channel == null && (System.currentTimeMillis() - now) >= maxWait) {
                    throw new ChannelPoolException("TimeOut:pool empty. Unable to fetch a channel, none avaliable in use." +
                            getChannelPoolDesc());
                }
            }

        } while (channel != null);

        return channel;
    }

    protected PooledChannel doSelectChannel() {

        for (int index = 0; index < size.get(); index++) {

            PooledChannel pooledChannel = pooledChannels.get(0);

            if (pooledChannel.isActive()) {

                if (pooledChannel.isWritable()) {
                    return pooledChannel;
                }
            }
        }

        return null;
    }

    protected PooledChannel createChannel() throws ChannelPoolException {
        PooledChannel channel = null;
        try {

            channel = channelFactory.createChannel();

        } catch (ChannelException e) {
            throw new ChannelPoolException("[createChannel] failed.", e);
        } finally {
            synchronized (pooledChannels) {
                pooledChannels.add(channel);
            }
        }

        return channel;
    }


    @Override
    public List<PooledChannel> getChannels() {
        return null;
    }

    @Override
    public PoolProperties getPoolProperties() {
        return properties;
    }

    @Override
    public void close() {
        if (isClosed.compareAndSet(false, true)) {

        }
    }

    public boolean isClosed() {
        return isClosed.get();
    }

    protected String getChannelPoolDesc() {
        return "Pool[poolSize=" + size.get() + "]";
    }
}
