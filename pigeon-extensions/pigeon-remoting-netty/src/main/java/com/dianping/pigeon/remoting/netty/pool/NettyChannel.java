package com.dianping.pigeon.remoting.netty.pool;

import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.log.Logger;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author qi.yin
 *         2016/07/22  上午11:59.
 */
public class NettyChannel implements PooledChannel {

    private static final Logger logger = LoggerLoader.getLogger(NettyChannel.class);

    private ReentrantLock connectLock = new ReentrantLock();

    private int timeout;

    private volatile Channel channel;

    private ClientBootstrap bootstrap;

    private InetSocketAddress localAddress;

    private InetSocketAddress remoteAddress;

    private AtomicBoolean released = new AtomicBoolean(false);

    public NettyChannel(ClientBootstrap bootstrap, String remoteHost, int remotePort, int timeout) {
        this.bootstrap = bootstrap;
        this.remoteAddress = new InetSocketAddress(remoteHost, remotePort);
        this.timeout = timeout;
    }

    @Override
    public void connect() throws ChannelException {
        connectLock.lock();
        try {
            if (isActive()) {
                logger.warn("[connect] is connected to remote " + remoteAddress + ".");
                return;
            }

            ChannelFuture future = bootstrap.connect(remoteAddress);

            try {
                if (future.awaitUninterruptibly(timeout, TimeUnit.MILLISECONDS)) {

                    if (future.isSuccess()) {
                        disConnect();
                        this.channel = future.getChannel();
                        localAddress = (InetSocketAddress) this.channel.getLocalAddress();
                    } else {
                        logger.error("[connect] connected to remote " + remoteAddress + " failed.");
                        throw new ChannelException("connected to remote " + remoteAddress + " failed.");
                    }

                } else {
                    logger.error("[connect] timeout connecting to remote " + remoteAddress + ".");
                    throw new ChannelException("timeout connecting to remote " + remoteAddress + ".");
                }

            } catch (Throwable e) {
                logger.error("[connect] error connecting to remote " + remoteAddress + ".", e);

                throw new ChannelException("error connecting to remote " + remoteAddress + ".", e);
            } finally {
                if (!isConnected()) {
                    future.cancel();
                }
            }
        } finally {
            connectLock.unlock();
        }

    }

    @Override
    public void reconnect() throws ChannelException {
        disConnect();
        connect();
    }

    protected void disConnect() {
        connectLock.lock();
        try {
            if (this.channel != null) {
                this.channel.close();
            }
        } catch (Throwable e) {
            logger.error("[disConnect] error disConnecting channel. ", e);
        } finally {
            connectLock.lock();
        }
    }

    @Override
    public ChannelFuture write(Object message) throws ChannelException {
        if (!isActive()) {
            throw new ChannelException("[write] channel is null or channel is close.");
        }

        return channel.write(message);
    }

    private boolean isConnected() {
        if (this.channel != null) {
            return this.channel.isConnected();
        }
        return false;
    }

    @Override
    public boolean isActive() {
        return !released.get() && channel != null && channel.isConnected();
    }

    public boolean isWritable() {
        return channel.isWritable();
    }

    @Override
    public InetSocketAddress getLocalAddress() {
        return localAddress;
    }

    @Override
    public InetSocketAddress getRemoteAddress() {
        return remoteAddress;
    }


    public int getTimeout() {
        return timeout;
    }

}