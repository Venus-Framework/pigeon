/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.netty.provider;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

import com.dianping.pigeon.component.phase.Disposable;
import com.dianping.pigeon.remoting.netty.provider.process.RequestProcessor;
import com.dianping.pigeon.remoting.provider.Server;
import com.dianping.pigeon.threadpool.NamedThreadFactory;

/**
 * 
 * 
 * @author jianhuihuang
 * @version $Id: NettyServer.java, v 0.1 2013-6-18 下午12:14:43 jianhuihuang Exp $
 */
public class NettyServer implements Server, Disposable {

	private static final Logger logger = Logger.getLogger(NettyServer.class);

	private String ip = null;

	private int port = 4625;

	private ServerBootstrap bootstrap;
	private ChannelGroup channelGroup = new DefaultChannelGroup();

	private Channel channel;

	private volatile boolean started = false;

	public static final int DEFAULT_IO_THREADS = Runtime.getRuntime().availableProcessors() + 1;

	public NettyServer(int port) {
		this.port = port;
		// Executor boss =
		// ManagerLoadFactory.threadManager.getServerBossProcessThreadPool();
		// Executor worker =
		// ManagerLoadFactory.threadManager.getServerWorkProcessThreadPool();

		ExecutorService boss = Executors.newCachedThreadPool(new NamedThreadFactory("Pigeon-NettyServerBoss", true));
		ExecutorService worker = Executors
				.newCachedThreadPool(new NamedThreadFactory("Pigeon-NettyServerWorker", true));

		// logger.error("DEFAULT_IO_THREADS......" + DEFAULT_IO_THREADS);
		this.bootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(boss, worker, DEFAULT_IO_THREADS));

		RequestProcessor requestProcessor = new RequestProcessor();

		this.bootstrap.setPipelineFactory(new ServerChannelPipelineFactory(channelGroup, requestProcessor));
		this.bootstrap.setOption("child.tcpNoDelay", true);
		this.bootstrap.setOption("child.keepAlive", true);
		bootstrap.setOption("receiveBufferSize", 1048576 * 2);
		bootstrap.setOption("sendBufferSize", 1048576 * 1);
	}

	public void start() {

		if (!started) {
			InetSocketAddress address = null;
			if (this.ip == null) {
				address = new InetSocketAddress(this.port);
			} else {
				address = new InetSocketAddress(this.ip, this.port);
			}
			channel = this.bootstrap.bind(address);
			this.started = true;
		}
		if (logger.isInfoEnabled()) {
			logger.info("Pigeon server startup at port:" + this.port);
		}
	}

	public void stop() {

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

}
