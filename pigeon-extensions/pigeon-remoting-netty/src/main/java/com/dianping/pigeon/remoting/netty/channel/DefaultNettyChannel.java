package com.dianping.pigeon.remoting.netty.channel;

import com.dianping.pigeon.log.Logger;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.remoting.common.exception.NetworkException;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author qi.yin
 *         2016/09/23  上午10:31.
 */
public class DefaultNettyChannel implements NettyChannel {

    private static final Logger logger = LoggerLoader.getLogger(NettyChannel.class);

    private ReentrantLock connectLock = new ReentrantLock();

    private int timeout;

    private volatile Channel channel;

    private ClientBootstrap bootstrap;

    private InetSocketAddress localAddress;

    private InetSocketAddress remoteAddress;

    public DefaultNettyChannel(ClientBootstrap bootstrap, String remoteHost, int remotePort, int timeout) {
        this.bootstrap = bootstrap;
        this.remoteAddress = new InetSocketAddress(remoteHost, remotePort);
        this.timeout = timeout;
    }

    @Override
    public void connect() throws NetworkException {
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
                        throw new NetworkException("connected to remote " + remoteAddress + " failed.");
                    }

                } else {
                    logger.error("[connect] timeout connecting to remote " + remoteAddress + ".");
                    throw new NetworkException("timeout connecting to remote " + remoteAddress + ".");
                }

            } catch (Throwable e) {
                logger.error("[connect] error connecting to remote " + remoteAddress + ".", e);

                throw new NetworkException("error connecting to remote " + remoteAddress + ".", e);
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
    public void disConnect() {
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
    public ChannelFuture write0(Object message) throws NetworkException {
        if (!isActive()) {
            throw new NetworkException("[write0] channel is null or channel is close.");
        }

        return channel.write(message);

    }

    @Override
    public void write(Object message) throws NetworkException {
        throw new UnsupportedOperationException("unsupported this operation");
    }

    private boolean isConnected() {
        if (this.channel != null) {
            return this.channel.isConnected();
        }
        return false;
    }

    @Override
    public boolean isActive() {
        return channel != null && channel.isConnected();
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

    public String toString() {
        return "PooledChannel[ remoteAddress= " + remoteAddress.toString() + "]";
    }

}
