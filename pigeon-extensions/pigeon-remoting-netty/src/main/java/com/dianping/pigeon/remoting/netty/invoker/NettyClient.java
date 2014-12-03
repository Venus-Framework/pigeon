/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.netty.invoker;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;

import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.extension.ExtensionLoader;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.common.exception.NetworkException;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.common.util.TimelineManager;
import com.dianping.pigeon.remoting.common.util.TimelineManager.Phase;
import com.dianping.pigeon.remoting.invoker.AbstractClient;
import com.dianping.pigeon.remoting.invoker.Client;
import com.dianping.pigeon.remoting.invoker.domain.Callback;
import com.dianping.pigeon.remoting.invoker.domain.ConnectInfo;
import com.dianping.pigeon.remoting.invoker.domain.InvokerContext;
import com.dianping.pigeon.remoting.invoker.listener.HeartBeatListener;
import com.dianping.pigeon.remoting.provider.config.ServerConfig;
import com.dianping.pigeon.remoting.provider.util.ProviderUtils;
import com.dianping.pigeon.threadpool.DefaultThreadFactory;

public class NettyClient extends AbstractClient {

	private static final Logger logger = LoggerLoader.getLogger(NettyClient.class);

	private ClientBootstrap bootstrap;

	private Channel channel;

	private String host;

	private int port = ServerConfig.DEFAULT_PORT;

	private String address;

	private volatile boolean connected = false;

	private volatile boolean closed = false;

	private volatile boolean active = true;

	private ConnectInfo connectInfo;

	public static final int CLIENT_CONNECTIONS = Runtime.getRuntime().availableProcessors();

	private static final int LOG_LIMIT_INITIAL = 5;

	private static final int LOG_LIMIT_INTERVAL = 60;

	private long logCount;

	private static ConfigManager configManager = ExtensionLoader.getExtension(ConfigManager.class);

	private static ExecutorService bossExecutor = Executors.newCachedThreadPool(new DefaultThreadFactory(
			"Pigeon-Netty-Client-Boss"));

	private static ExecutorService workExecutor = Executors.newCachedThreadPool(new DefaultThreadFactory(
			"Pigeon-Netty-Client-Worker"));

	private static final int bossCount = configManager.getIntValue("pigeon.invoker.netty.bosscount", 1);

	private static final int workerCount = configManager.getIntValue("pigeon.invoker.netty.workercount", Runtime
			.getRuntime().availableProcessors() * 2);

	private static ChannelFactory channelFactory = new NioClientSocketChannelFactory(bossExecutor, workExecutor,
			bossCount, workerCount);

	private static final int connectTimeout = configManager.getIntValue(Constants.KEY_CONNECT_TIMEOUT,
			Constants.DEFAULT_CONNECT_TIMEOUT);

	public int getWriteBufferHighWater() {
		return configManager.getIntValue(Constants.KEY_WRITE_BUFFER_HIGH_WATER,
				Constants.DEFAULT_WRITE_BUFFER_HIGH_WATER);
	}

	public int getWriteBufferLowWater() {
		return configManager
				.getIntValue(Constants.KEY_WRITE_BUFFER_LOW_WATER, Constants.DEFAULT_WRITE_BUFFER_LOW_WATER);
	}

	public NettyClient(ConnectInfo connectInfo) {
		this.host = connectInfo.getHost();
		this.port = connectInfo.getPort();
		this.connectInfo = connectInfo;
		this.address = host + ":" + port;

		this.bootstrap = new ClientBootstrap(channelFactory);
		this.bootstrap.setPipelineFactory(new NettyClientPipelineFactory(this));
		this.bootstrap.setOption("tcpNoDelay", true);
		this.bootstrap.setOption("keepAlive", true);
		this.bootstrap.setOption("reuseAddress", true);
		this.bootstrap.setOption("connectTimeoutMillis", connectTimeout);
		this.bootstrap.setOption("writeBufferHighWaterMark", getWriteBufferHighWater());
		this.bootstrap.setOption("writeBufferLowWaterMark", getWriteBufferLowWater());
	}

	public synchronized void connect() {
		if (this.connected || this.closed) {
			resetLogCount();
			return;
		}
		incLogCount();
		ChannelFuture future = null;
		try {
			future = bootstrap.connect(new InetSocketAddress(host, port));
			if (future.awaitUninterruptibly(connectTimeout, TimeUnit.MILLISECONDS)) {
				if (future.isSuccess()) {
					Channel newChannel = future.getChannel();
					try {
						// 关闭旧的连接
						Channel oldChannel = this.channel;
						if (oldChannel != null) {
							if (logger.isDebugEnabled()) {
								logger.debug("close old netty channel " + oldChannel);
							}
							try {
								oldChannel.close();
							} catch (Throwable t) {
							}
						}
					} finally {
						this.channel = newChannel;
					}
					logger.warn("client is connected to " + this.host + ":" + this.port);
					this.connected = true;
					resetLogCount();
				} else if (isLog()) {
					logger.warn("client is not connected to " + this.host + ":" + this.port);
				}
			} else if (isLog()) {
				logger.warn("timeout while connecting to " + this.host + ":" + this.port);
			}
		} catch (Throwable e) {
			if (isLog()) {
				logger.warn("error while connecting to " + this.host + ":" + this.port, e);
			}
		}
	}

	@Override
	public InvocationResponse doWrite(InvocationRequest request, Callback callback) throws NetworkException {
		Object[] msg = new Object[] { request, callback };
		ChannelFuture future = null;
		if (channel == null) {
			logger.error("channel is null ^^^^^^^^^^^^^^");
		} else {
			future = channel.write(msg);
			if (request.getMessageType() == Constants.MESSAGE_TYPE_SERVICE
					|| request.getMessageType() == Constants.MESSAGE_TYPE_HEART) {
				future.addListener(new MsgWriteListener(request));
			}
		}
		return null;
	}

	public void connectionException(Object attachment, Throwable e) {
		this.connected = false;
		connectionException(this, attachment, e);
	}

	private void connectionException(Client client, Object attachment, Throwable e) {
		if (isLog()) {
			logger.error("exception while connecting to :" + client + ", exception:" + e.getMessage());
		}
		if (attachment == null) {
			return;
		}
		Object[] msg = (Object[]) attachment;
		if (msg[0] instanceof InvokerContext) {
			InvokerContext invokerContext = (InvokerContext) msg[0];
			InvocationRequest request = invokerContext.getRequest();
			if (request.getMessageType() == Constants.MESSAGE_TYPE_SERVICE && msg[1] != null) {
				try {
					Callback callback = (Callback) msg[1];
					if (client != null) {
						client.write(request, callback);
					} else {
						logger.error("no client found with service:" + request.getServiceName());
					}
				} catch (Throwable ex) {
					logger.error("", ex);
				}
				logger.error("", e);
			}
		}
	}

	/**
	 * @return the connected
	 */
	public boolean isConnected() {
		return connected;
	}

	public boolean isActive() {
		return active && HeartBeatListener.isActiveAddress(address);
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	@Override
	public boolean isWritable() {
		return this.channel.isWritable();
	}

	/**
	 * @return the host
	 */
	public String getHost() {
		return host;
	}

	public int getPort() {

		return this.port;
	}

	/**
	 * @return the address
	 */
	public String getAddress() {
		return address;
	}

	public boolean equals(Object obj) {
		if (obj instanceof NettyClient) {
			NettyClient nc = (NettyClient) obj;
			return this.address.equals(nc.getAddress());
		} else {
			return super.equals(obj);
		}
	}

	@Override
	public int hashCode() {
		return address.hashCode();
	}

	@Override
	public void close() {
		closed = true;
		channel.close();
	}

	@Override
	public String toString() {
		return this.getAddress() + ", connected:" + this.isConnected() + ", active:" + this.isActive();
	}

	public class MsgWriteListener implements ChannelFutureListener {

		private InvocationRequest request;

		public MsgWriteListener(InvocationRequest request) {
			this.request = request;
		}

		public void operationComplete(ChannelFuture future) throws Exception {
			// TIMELINE_client_sent
			TimelineManager.time(request, TimelineManager.getLocalIp(), Phase.ClientSent);
			if (future.isSuccess()) {
				return;
			}
			if (request.getMessageType() != Constants.MESSAGE_TYPE_HEART) {
				connected = false;
			}
			InvocationResponse response = ProviderUtils.createFailResponse(request, future.getCause());
			processResponse(response);
		}

	}

	@Override
	public ConnectInfo getConnectInfo() {
		return connectInfo;
	}

	private void resetLogCount() {
		logCount = 0;
	}

	private boolean isLog() {
		boolean isLog = true;
		if (logCount > LOG_LIMIT_INITIAL && logCount % LOG_LIMIT_INTERVAL != 0) {
			isLog = false;
		}
		return isLog;
	}

	private void incLogCount() {
		logCount = logCount + 1;
	}

	@Override
	public boolean isDisposable() {
		return false;
	}

	@Override
	public void dispose() {

	}
}
