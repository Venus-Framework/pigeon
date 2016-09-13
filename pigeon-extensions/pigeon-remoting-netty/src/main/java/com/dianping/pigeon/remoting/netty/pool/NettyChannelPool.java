package com.dianping.pigeon.remoting.netty.pool;

import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.threadpool.DefaultThreadFactory;
import com.dianping.pigeon.log.Logger;

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author qi.yin
 *         2016/07/21  上午11:06.
 */
public class NettyChannelPool implements ChannelPool {

    private static final Logger logger = LoggerLoader.getLogger(NettyChannelPool.class);

    private volatile boolean isClosed = false;

    private AtomicInteger size = new AtomicInteger();

    private PoolProperties properties;

    private BlockingQueue<PooledChannel> busy;

    private BlockingQueue<PooledChannel> idle;

    private PooledChannelFactory channelFactory;

    private PoolCleaner poolCleaner;

    private static List<PoolCleaner> cleaners = new ArrayList<PoolCleaner>();

    private static volatile ScheduledExecutorService executor;

    public NettyChannelPool(PooledChannelFactory channelFactory)
            throws ChannelPoolException {

        this(new PoolProperties(), channelFactory);
    }

    public NettyChannelPool(PoolProperties properties, PooledChannelFactory channelFactory)
            throws ChannelPoolException {

        this.properties = properties;
        this.channelFactory = channelFactory;
        init(properties);

    }

    protected void init(PoolProperties properties) throws ChannelPoolException {
        if (properties.getMaxActive() < 1) {
            logger.warn("[init] maxActive is smaller than 1, setting maxActive to " + PoolProperties.DEFAULT_MAX_ACTIVE);
            properties.setMaxActive(PoolProperties.DEFAULT_MAX_ACTIVE);
        }
        if (properties.getInitialSize() > properties.getMaxActive()) {
            logger.warn("[init] initialSize is larger than maxActive, setting initialSize to" + properties.getMaxActive());
            properties.setInitialSize(properties.getMaxActive());
        }

        if (properties.getMinIdle() > properties.getMaxActive()) {
            logger.warn("[init] minIdle is larger than maxActive, setting minIdle to" + properties.getMaxActive());
            properties.setMinIdle(properties.getMaxActive());
        }

//        if (properties.getMaxIdle() > properties.getMaxActive()) {
//            logger.warn("[init] maxIdle is larger than maxActive, setting maxIdle to" + properties.getMaxActive());
//            properties.setMaxIdle(properties.getMaxActive());
//        }

//        if (properties.getMinIdle() > properties.getMaxIdle()) {
//            logger.warn("[init] minIdle is larger than maxIdle, setting maxIdle to" + properties.getMinIdle());
//            properties.setMaxIdle(properties.getMinIdle());
//        }

        busy = new LinkedBlockingQueue<PooledChannel>();

        idle = new LinkedBlockingQueue<PooledChannel>();

        initPoolCleaner();

        PooledChannel[] initialPool = new NettyChannel[properties.getInitialSize()];
        try {

            for (int i = 0; i < properties.getInitialSize(); i++) {
                initialPool[i] = borrowChannel();
            }
        } catch (ChannelPoolException e) {
            logger.error("Unable to create initial connections of pool.", e);
            close();
            throw e;
        } finally {
            for (int i = 0; i < properties.getInitialSize(); i++) {
                if (initialPool[i] != null) {
                    returnChannel(initialPool[i]);
                }
            }
        }

    }


    protected void initPoolCleaner() {
        this.poolCleaner = new PoolCleaner(this, getPoolProperties().getTimeBetweenEvictionRunsMillis());
        this.poolCleaner.start();
    }

    @Override
    public PooledChannel borrowChannel() throws ChannelPoolException {
        if (isClosed()) {
            throw new ChannelPoolException("Channel pool is closed.");
        }

        long now = System.currentTimeMillis();

        PooledChannel channel = idle.poll();

        while (true) {

            if (channel != null) {
                channel = doBorrowChannel(channel, now);
                if (channel != null) {
                    return channel;
                }
            }

            if (size.get() < properties.getMaxActive()) {

                if (size.addAndGet(1) > properties.getMaxActive()) {
                    size.decrementAndGet();
                } else {
                    return createChannel(now);
                }
            }

            long maxWait = (properties.getMaxWait() <= 0) ? Long.MAX_VALUE : properties.getMaxWait();

            long timetowait = Math.max(0, maxWait - (System.currentTimeMillis() - now));

            try {
                channel = idle.poll(timetowait, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new ChannelPoolException("Pool wait interrupted.", e);
            }

            if (maxWait == 0 && channel == null) {
                throw new ChannelPoolException("No wait:pool empty. Unable to fetch a channel, none avaliable in use. " +
                        getChannelPoolDesc());
            }

            if (channel == null) {

                if ((System.currentTimeMillis() - now) >= maxWait) {
                    throw new ChannelPoolException("TimeOut:pool empty. Unable to fetch a channel, none avaliable in use." +
                            getChannelPoolDesc());
                } else {
                    continue;
                }
            }

        }
    }

    protected PooledChannel doBorrowChannel(PooledChannel channel, long now) {
        channel.lock();

        try {

            if (!channel.isActive()) {
                releaseChannel(channel);
                return null;
            }

            busy.offer(channel);
            channel.setTimestamp(now);

            return channel;
        } finally {
            channel.unLock();
        }

    }


    @Override
    public void returnChannel(PooledChannel channel) {
        if (isClosed()) {
            releaseChannel(channel);
        } else {
            if (channel != null) {

                try {
                    channel.lock();

                    if (!(busy.remove(channel) && channel.isActive()
                            && idle.offer(channel))) {
                        releaseChannel(channel);
                    }

                } finally {
                    channel.unLock();
                }
            }
        }

    }

    @Override
    public PoolProperties getPoolProperties() {
        return properties;
    }

    @Override
    public int getSize() {
        return size.get();
    }

    @Override
    public int getActive() {
        return busy.size();
    }

    @Override
    public int getIdle() {
        return idle.size();
    }


    public void checkIdle() {
        if (idle.isEmpty()) {
            return;
        }

        long now = System.currentTimeMillis();

        try {
            Iterator<PooledChannel> unlocked = idle.iterator();

            while (idle.size() > properties.getMinIdle()
                    && unlocked.hasNext()) {

                PooledChannel channel = unlocked.next();

                try {
                    channel.lock();

                    if (busy.contains(channel)) {
                        continue;
                    }

                    if (shouldReleaseIdle(now, channel)) {
                        releaseChannel(channel);
                        idle.remove(channel);
                    }

                } finally {
                    channel.unLock();
                    channel = null;
                }
            }
        } catch (Exception e) {
            logger.error("[checkIdle failed. it will be retry]");
        }

    }

    protected boolean shouldReleaseIdle(long now, PooledChannel channel) {
        long timestamp = channel.getTimestamp();

        if (now - timestamp > properties.getMinEvictableIdleTimeMillis()
                && getSize() > properties.getMinIdle()) {
            return true;
        }

        return false;
    }

    protected PooledChannel createChannel(long now) throws ChannelPoolException {
        PooledChannel channel = null;
        boolean error = false;
        try {

            channel = channelFactory.createChannel();
            channel.setTimestamp(now);

            busy.offer(channel);

        } catch (ChannelException e) {
            error = true;
            throw new ChannelPoolException("[createChannel] failed.", e);
        } finally {
            if (error && channel != null) {
                releaseChannel(channel);
            }
        }

        return channel;
    }

    protected void releaseChannel(PooledChannel channel) {
        try {
            channel.lock();

            if (channel.release()) {
                size.decrementAndGet();
            }

        } finally {
            channel.unLock();
        }
    }

    @Override
    public void close() {
        close(false);
    }

    public void close(boolean force) {
        if (isClosed()) {
            return;
        }

        isClosed = true;

        if (poolCleaner != null) {
            poolCleaner.stop();
        }

        BlockingQueue<PooledChannel> pool = (idle.size() > 0) ? idle : (force ? busy : idle);

        while (pool.size() > 0) {
            try {

                PooledChannel channel = pool.poll(1000, TimeUnit.MILLISECONDS);
                while (channel != null) {

                    if (pool == idle) {
                        releaseChannel(channel);
                    }

                    if (pool.size() > 0) {
                        channel = pool.poll(1000, TimeUnit.MILLISECONDS);
                    } else {
                        break;
                    }

                } //while
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
            if (pool.size() == 0 && force && pool != busy) pool = busy;
        }


    }

    public boolean isClosed() {
        return isClosed;
    }

    protected String getChannelPoolDesc() {
        return "Pool[poolSize=" + size.get() + " busySize=" + busy.size() + " idleSize=" + idle.size() + "]";
    }

    public static synchronized void registerCleaner(PoolCleaner cleaner) {

        unregisterCleaner(cleaner);

        cleaners.add(cleaner);

        if (executor == null) {
            executor = Executors.newSingleThreadScheduledExecutor(new DefaultThreadFactory("Pigeon-Netty-Channel-Cleaner"));
        }

        ScheduledFuture<?> scheduled = executor.scheduleAtFixedRate(cleaner,
                cleaner.getInterval(),
                cleaner.getInterval(),
                TimeUnit.MILLISECONDS);

        cleaner.setScheduled(scheduled);
    }

    public static synchronized void unregisterCleaner(PoolCleaner cleaner) {

        boolean removed = cleaners.remove(cleaner);

        if (removed) {

            ScheduledFuture<?> scheduled = cleaner.getScheduled();

            if (scheduled != null) {
                scheduled.cancel(false);
                scheduled = null;
            }

            if (cleaners.isEmpty()) {
                executor.shutdown();
                executor = null;
            }
        }
    }

    public static class PoolCleaner implements Runnable {

        private int interval;

        private WeakReference<ChannelPool> pool;

        private ScheduledFuture<?> scheduled;

        public PoolCleaner(ChannelPool pool, int interval) {
            this.interval = interval;
            this.pool = new WeakReference<ChannelPool>(pool);
        }

        @Override
        public void run() {
            NettyChannelPool pool = (NettyChannelPool) this.pool.get();

            if (pool == null) {
                stop();
            } else if (!pool.isClosed()) {

                if (pool.getPoolProperties().getMinIdle() < pool.idle.size()) {
                    pool.checkIdle();
                }
            }

        }

        public void start() {
            registerCleaner(this);
        }

        public void stop() {
            unregisterCleaner(this);
        }

        public int getInterval() {
            return interval;
        }

        public void setScheduled(ScheduledFuture<?> scheduled) {
            this.scheduled = scheduled;
        }

        public ScheduledFuture<?> getScheduled() {
            return scheduled;
        }
    }

}