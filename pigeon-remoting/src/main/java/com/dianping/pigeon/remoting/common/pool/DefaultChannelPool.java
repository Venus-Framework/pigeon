package com.dianping.pigeon.remoting.common.pool;

import com.dianping.pigeon.log.Logger;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.remoting.common.channel.Channel;
import com.dianping.pigeon.remoting.common.channel.ChannelFactory;
import com.dianping.pigeon.remoting.common.exception.NetworkException;
import com.dianping.pigeon.threadpool.DefaultThreadFactory;
import com.dianping.pigeon.util.AtomicPositiveInteger;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author qi.yin
 *         2016/09/23  上午10:52.
 */
public class DefaultChannelPool<C extends Channel> implements ChannelPool<C> {

    private static final Logger logger = LoggerLoader.getLogger(DefaultChannelPool.class);

    private List<C> pooledChannels = new ArrayList<C>();

    private AtomicInteger size = new AtomicInteger();

    private AtomicPositiveInteger selectedIndex = new AtomicPositiveInteger(0);

    private AtomicBoolean isClosed = new AtomicBoolean(true);

    private PoolProperties properties;

    private ChannelFactory<C> channelFactory;

    private ScheduledFuture<?> scheduledFuture = null;

    private static ExecutorService reconnectExecutor = Executors.newFixedThreadPool(
            4, new DefaultThreadFactory("Pigeon-ChannelPool-Reconnect-Pool"));

    private static ScheduledThreadPoolExecutor checkScheduler = new ScheduledThreadPoolExecutor(
            2, new DefaultThreadFactory("Pigeon-ChannelPool-Check-Pool"));

    private final static Object PRESENT = new Object();

    private final ConcurrentMap<C, Object> reconnectChannels = new
            ConcurrentHashMap<C, Object>();

    public DefaultChannelPool(ChannelFactory channelFactory)
            throws ChannelPoolException {
        this(new PoolProperties(), channelFactory);
    }

    public DefaultChannelPool(PoolProperties properties, ChannelFactory channelFactory)
            throws ChannelPoolException {
        if (isClosed.compareAndSet(true, false)) {
            this.properties = properties;
            this.channelFactory = channelFactory;
            init(properties);
        }
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

        try {

            for (int i = 0; i < properties.getInitialSize(); i++) {
                selectChannel();
            }

        } catch (ChannelPoolException e) {
            logger.error("[init] unable to create initial connections of pool.", e);
        }

        initCheckScheduler();
    }

    private void initCheckScheduler() {
        int interval = properties.getTimeBetweenCheckerMillis();

        scheduledFuture = checkScheduler.scheduleWithFixedDelay(new CheckChannelTask(this), interval, interval, TimeUnit.MILLISECONDS);
    }

    @Override
    public int getSize() {
        return pooledChannels.size();
    }

    @Override
    public boolean isActive() {
        if (isClosed()) {
            return false;
        }

        for (int index = 0; index < pooledChannels.size(); index++) {

            C channel = pooledChannels.get(index);

            if (channel != null && channel.isActive()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public C selectChannel() throws ChannelPoolException {
        if (isClosed()) {
            throw new ChannelPoolException("Channel pool is closed.");
        }

        long now = System.currentTimeMillis();

        long maxWait = (properties.getMaxWait() < 0) ? Long.MAX_VALUE : properties.getMaxWait();

        C channel = null;

        do {
            //create
            if (size.get() < properties.getMaxActive()) {
                if (size.incrementAndGet() > properties.getMaxActive()) {
                    size.decrementAndGet();
                } else {
                    return createChannel();
                }
            }

            //random
            if (!pooledChannels.isEmpty()) {
                int selected = selectedIndex.getAndIncrement() % pooledChannels.size();
                C pooledChannel = pooledChannels.get(selected);

                if (pooledChannel != null) {
                    if (!pooledChannel.isActive()) {
                        reconnectChannel(pooledChannel);
                    } else {
                        return pooledChannel;
                    }
                }
            }

            //timeout
            if ((System.currentTimeMillis() - now) >= maxWait) {
                throw new ChannelPoolException("TimeOut:pool empty. Unable to fetch a channel, none avaliable in use." +
                        getChannelPoolDesc());
            }

        }
        while (channel != null);

        return channel;
    }

    protected C createChannel() throws ChannelPoolException {
        C channel = null;

        try {
            channel = channelFactory.createChannel();
        } finally {
            if (channel != null) {
                synchronized (pooledChannels) {
                    pooledChannels.add(channel);
                }
            }
        }

        return channel;
    }

    public void reconnectChannel(C channel) {
        if (reconnectChannels.putIfAbsent(channel, PRESENT) == null) {
            reconnectExecutor.submit(new ReconnectChannelTask(channel, DefaultChannelPool.this));
        }
    }


    @Override
    public List<C> getChannels() {
        return pooledChannels;
    }

    @Override
    public PoolProperties getPoolProperties() {
        return properties;
    }

    @Override
    public void close() {
        if (isClosed.compareAndSet(false, true)) {

            for (int index = 0; index < pooledChannels.size(); index++) {

                C pooledChannel = pooledChannels.get(index);

                if (pooledChannel != null && pooledChannel.isActive()) {
                    pooledChannel.disConnect();
                }
            }

            if (scheduledFuture != null && !scheduledFuture.isCancelled()) {
                scheduledFuture.cancel(true);
                checkScheduler.purge();
            }

            scheduledFuture = null;
        }
    }

    @Override
    public boolean isClosed() {
        return isClosed.get();
    }

    protected String getChannelPoolDesc() {
        return "ChannelPool[poolSize=" + pooledChannels.size() + "]";
    }

    class ReconnectChannelTask implements Runnable {

        private WeakReference<C> channelRef;
        private WeakReference<ChannelPool> poolRef;

        public ReconnectChannelTask(C channel, ChannelPool pool) {
            this.channelRef = new WeakReference<C>(channel);
            this.poolRef = new WeakReference<ChannelPool>(pool);
        }

        @Override
        public void run() {
            ChannelPool channelPool = poolRef.get();
            C channel = channelRef.get();

            if (channelPool != null && !channelPool.isClosed()) {

                if (channel != null && !channel.isActive()) {
                    try {
                        channel.connect();
                    } catch (NetworkException e) {
                        logger.error("[run] pooledChannel connnet failed.", e);
                    }

                }
            }

            DefaultChannelPool.this.reconnectChannels.remove(channel);
        }
    }

    class CheckChannelTask implements Runnable {

        private WeakReference<ChannelPool> poolRef;

        public CheckChannelTask(ChannelPool channelPool) {
            this.poolRef = new WeakReference<ChannelPool>(channelPool);
        }

        @Override
        public void run() {
            ChannelPool channelPool = poolRef.get();

            if (channelPool != null && !channelPool.isClosed()) {

                List<C> channels = channelPool.getChannels();

                for (int index = 0; index < channels.size(); index++) {
                    C channel = channels.get(index);

                    if (!channel.isActive()) {
                        reconnectChannel(channel);
                    }
                }

            }

        }
    }
}
