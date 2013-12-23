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
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;

import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.event.EventManager;
import com.dianping.pigeon.event.RuntimeServiceEvent;
import com.dianping.pigeon.extension.ExtensionLoader;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.common.util.ResponseUtils;
import com.dianping.pigeon.remoting.invoker.AbstractClient;
import com.dianping.pigeon.remoting.invoker.Client;
import com.dianping.pigeon.remoting.invoker.domain.Callback;
import com.dianping.pigeon.remoting.invoker.domain.ConnectInfo;
import com.dianping.pigeon.remoting.invoker.domain.InvokerContext;
import com.dianping.pigeon.remoting.invoker.domain.RpcInvokeInfo;
import com.dianping.pigeon.remoting.invoker.util.RpcEventUtils;
import com.dianping.pigeon.threadpool.DefaultThreadFactory;

public class NettyClient extends AbstractClient {

	private static final Logger logger = LoggerLoader.getLogger(NettyClient.class);

	private ClientBootstrap bootstrap;

	private Channel channel;

	private String host;

	private int port = 4625;

	private String address;

	private static final int connectTimeout = 500;

	private volatile boolean connected = false;

	private volatile boolean closed = false;

	private volatile boolean active = true;
	private volatile boolean activeSetable = false;

	private ConnectInfo connectInfo;

	private ConfigManager configManager = ExtensionLoader.getExtension(ConfigManager.class);

	public static final int CLIENT_CONNECTIONS = Runtime.getRuntime().availableProcessors();

	public NettyClient(ConnectInfo connectInfo) {
		this.host = connectInfo.getHost();
		this.port = connectInfo.getPort();
		this.connectInfo = connectInfo;
		this.address = host + ":" + port;

		ExecutorService bossExecutor = Executors.newCachedThreadPool(new DefaultThreadFactory(
				Constants.THREADNAME_CLIENT_NETTY_BOSS_EXECUTOR));

		ExecutorService workExecutor = Executors.newCachedThreadPool(new DefaultThreadFactory(
				Constants.THREADNAME_CLIENT_NETTY_WORKER_EXECUTOR));

		this.bootstrap = new ClientBootstrap(new NioClientSocketChannelFactory(bossExecutor, workExecutor));
		this.bootstrap.setOption("writeBufferHighWaterMark", configManager.getIntValue(
				Constants.KEY_WRITE_BUFFER_HIGH_WATER, Constants.DEFAULT_WRITE_BUFFER_HIGH_WATER));
		this.bootstrap.setOption("writeBufferLowWaterMark", configManager.getIntValue(
				Constants.KEY_WRITE_BUFFER_LOW_WATER, Constants.DEFAULT_WRITE_BUFFER_LOW_WATER));
		this.bootstrap.setPipelineFactory(new NettyClientPipelineFactory(this));
		this.bootstrap.setOption("tcpNoDelay", true);
		this.bootstrap.setOption("keepAlive", true);
		this.bootstrap.setOption("keepAlive", true);
	}

	public synchronized void connect() {
		if (this.connected || this.closed) {
			return;
		}
		if(logger.isDebugEnabled()) {
			logger.debug("client is connecting to " + this.host + ":" + this.port);
		}
		ChannelFuture future = bootstrap.connect(new InetSocketAddress(host, port));
		if (future.awaitUninterruptibly(connectTimeout, TimeUnit.MILLISECONDS)) {
			if (future.isSuccess()) {
				logger.warn("client is connected to " + this.host + ":" + this.port);
				this.connected = true;
			} else {
				logger.error("client is not connected to " + this.host + ":" + this.port);
			}
		}
		this.channel = future.getChannel();
	}

	public InvocationResponse write(InvokerContext invokerContext, Callback callback) {
		return write(invokerContext.getRequest(), callback);
	}

	public InvocationResponse write(InvocationRequest request) {
		return write(request, null);
	}

	public InvocationResponse write(InvocationRequest request, Callback callback) {
		Object[] msg = new Object[] { request, callback };
		ChannelFuture future = null;
		if (channel == null) {
			logger.error("channel:" + null + " ^^^^^^^^^^^^^^");
		} else {
			future = channel.write(msg);
			if (request.getMessageType() == Constants.MESSAGE_TYPE_SERVICE
					|| request.getMessageType() == Constants.MESSAGE_TYPE_HEART) {
				future.addListener(new MsgWriteListener(request));
			}
		}
		return null;
	}

	public InvocationResponse write(InvokerContext invokerContext) {
		return write(invokerContext, null);
	}

	public void connectionException(Object attachment, Throwable e) {
		this.connected = false;
		connectionException(this, attachment, e);
	}

	private void connectionException(Client client, Object attachment, Throwable e) {
		logger.error("exception while connecting to :" + client + ", exception:" + e.getMessage());
		if (attachment == null) {
			return;
		}
		Object[] msg = (Object[]) attachment;
		if (msg[0] instanceof InvokerContext) {
			InvokerContext invokerContext = (InvokerContext) msg[0];
			InvocationRequest request = (InvocationRequest) invokerContext.getRequest();
			if (request.getMessageType() == Constants.MESSAGE_TYPE_SERVICE && msg[1] != null) {
				try {
					Callback callback = (Callback) msg[1];
					if (client != null) {
						error(request, client);
						client.write(invokerContext, callback);
					} else {
						logger.error("no client for use to " + request.getServiceName());
					}
				} catch (Exception ne) {
					logger.error(ne.getMessage(), ne);
				}
				logger.error(e.getMessage(), e);
			}
		}
	}

	private void error(InvocationRequest request, Client client) {
		if (EventManager.IS_EVENT_ENABLED) {
			RpcInvokeInfo rpcInvokeInfo = new RpcInvokeInfo();
			rpcInvokeInfo.setServiceName(request.getServiceName());
			rpcInvokeInfo.setAddressIp(client.getAddress());
			rpcInvokeInfo.setRequest(request);
			RuntimeServiceEvent event = new RuntimeServiceEvent(
					RuntimeServiceEvent.Type.RUNTIME_RPC_INVOKE_CONNECT_EXCEPTION, rpcInvokeInfo);
			EventManager.getInstance().publishEvent(event);
		}
	}

	/**
	 * @return the connected
	 */
	public boolean isConnected() {
		return connected;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		if (this.activeSetable) {
			this.active = active;
		}
	}

	public void setActiveSetable(boolean activeSetable) {
		this.activeSetable = activeSetable;
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
		return this.getAddress() + ",is connected:" + this.isConnected();
	}

	public class MsgWriteListener implements ChannelFutureListener {

		private InvocationRequest request;

		public MsgWriteListener(InvocationRequest request) {
			this.request = request;
		}

		public void operationComplete(ChannelFuture future) throws Exception {
			if (future.isSuccess()) {
				return;
			}
			logger.error("MsgWriteListener........future" + future.isSuccess());
			if (request.getMessageType() != Constants.MESSAGE_TYPE_HEART) {

				connected = false;
			}

			RpcEventUtils.channelOperationComplete(request, NettyClient.this.address);
			InvocationResponse response = ResponseUtils.createFailResponse(request, future.getCause());
			processResponse(response);
		}

	}

	@Override
	public ConnectInfo getConnectInfo() {
		return connectInfo;
	}

}
