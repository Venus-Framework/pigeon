/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.netty.provider;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

import com.dianping.pigeon.domain.phase.Disposable;
import com.dianping.pigeon.remoting.common.exception.RpcException;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.provider.AbstractServer;
import com.dianping.pigeon.remoting.provider.config.ProviderConfig;
import com.dianping.pigeon.remoting.provider.config.ServerConfig;
import com.dianping.pigeon.threadpool.NamedThreadFactory;
import com.dianping.pigeon.util.NetUtils;

/**
 * 
 * 
 * @author jianhuihuang
 * @version $Id: NettyServer.java, v 0.1 2013-6-18 下午12:14:43 jianhuihuang Exp $
 */
public class NettyServer extends AbstractServer implements Disposable {

	private String ip = null;
	private int port = ServerConfig.DEFAULT_PORT;
	private ServerBootstrap bootstrap;
	private ChannelGroup channelGroup = new DefaultChannelGroup();
	private Channel channel;
	private volatile boolean started = false;
	public static final int DEFAULT_IO_THREADS = Runtime.getRuntime().availableProcessors() + 1;

	public NettyServer() {
		ExecutorService boss = Executors.newCachedThreadPool(new NamedThreadFactory("Pigeon-Netty-Server-Boss", true));
		ExecutorService worker = Executors
				.newCachedThreadPool(new NamedThreadFactory("Pigeon-Netty-Server-Worker", true));

		this.bootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(boss, worker));
		this.bootstrap.setPipelineFactory(new NettyServerPipelineFactory(this));
		this.bootstrap.setOption("child.tcpNoDelay", true);
		this.bootstrap.setOption("child.keepAlive", true);
		this.bootstrap.setOption("child.reuseAddress", true);
		this.bootstrap.setOption("child.connectTimeoutMillis", 1000);
	}

	@Override
	public boolean support(ServerConfig serverConfig) {
		if (serverConfig.getProtocols().contains(Constants.PROTOCOL_DEFAULT)) {
			return true;
		}
		return false;
	}

	@Override
	public void doStart(ServerConfig serverConfig) {
		if (!started) {
			if (serverConfig.isAutoSelectPort()) {
				int availablePort = NetUtils.getAvailablePort(serverConfig.getPort());
				this.port = availablePort;
			} else {
				if (NetUtils.isPortInUse(serverConfig.getPort())) {
					logger.error("unable to start netty server on port " + serverConfig.getPort() + ", the port is in use");
					System.exit(0);
				}
				this.port = serverConfig.getPort();
			}
			InetSocketAddress address = null;
			if (this.ip == null) {
				address = new InetSocketAddress(this.port);
			} else {
				address = new InetSocketAddress(this.ip, this.port);
			}
			channel = this.bootstrap.bind(address);
			serverConfig.setPort(this.port);
			this.started = true;
		}
	}

	@Override
	public void doStop() {
		if (this.started) {
			// this.channelGroup.close().awaitUninterruptibly();
			// this.bootstrap.releaseExternalResources();
			doClose();
			this.started = false;
		}
	}

	protected void doClose() {
		try {
			if (channelGroup != null) {
				// unbind.
				channelGroup.unbind().awaitUninterruptibly();
				channelGroup.close().awaitUninterruptibly();
			}
		} catch (Throwable e) {
			logger.warn(e.getMessage(), e);
		}
		if (channel != null) {
			channel.unbind();
		}
		try {
			if (bootstrap != null) {
				// release external resource.
				bootstrap.releaseExternalResources();
			}
		} catch (Throwable e) {
			logger.warn(e.getMessage(), e);
		}
		try {
			if (channelGroup != null) {
				channelGroup.clear();
			}
		} catch (Throwable e) {
			logger.warn(e.getMessage(), e);
		}
	}

	@Override
	public void destroy() {
		this.stop();
	}

	public ChannelGroup getChannelGroup() {
		return channelGroup;
	}

	@Override
	public <T> void addService(ProviderConfig<T> providerConfig) throws RpcException {
	}

	@Override
	public String toString() {
		return "NettyServer-" + this.port;
	}

	@Override
	public int getPort() {
		return port;
	}

	@Override
	public String getRegistryUrl(String url) {
		return url;
	}
}
