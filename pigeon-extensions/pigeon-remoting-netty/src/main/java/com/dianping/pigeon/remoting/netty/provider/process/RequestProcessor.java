/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.netty.provider.process;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;

import com.dianping.pigeon.component.invocation.InvocationRequest;
import com.dianping.pigeon.monitor.Log4jLoader;
import com.dianping.pigeon.remoting.common.filter.ServiceInvocationHandler;
import com.dianping.pigeon.remoting.netty.provider.NettyChannel;
import com.dianping.pigeon.remoting.provider.component.ProviderChannel;
import com.dianping.pigeon.remoting.provider.component.context.DefaultProviderContext;
import com.dianping.pigeon.remoting.provider.component.context.ProviderContext;
import com.dianping.pigeon.remoting.provider.component.context.RequestContext;
import com.dianping.pigeon.remoting.provider.listener.TimeoutListener;
import com.dianping.pigeon.remoting.provider.loader.RequestProcessHandlerLoader;
import com.dianping.pigeon.threadpool.DefaultThreadPool;
import com.dianping.pigeon.threadpool.NamedThreadFactory;
import com.dianping.pigeon.threadpool.ThreadPool;

public class RequestProcessor {

	private static final Logger logger = Log4jLoader.getLogger(RequestProcessor.class);

	private static ThreadPool timeCheckThreadPool = new DefaultThreadPool("pigeon-provider-timeout-checker");

	private static final ExecutorService executorService = Executors.newCachedThreadPool(new NamedThreadFactory(
			"pigeon-provider-request-processor", true));

	private ChannelGroup serverChannels;

	private Map<InvocationRequest, RequestContext> contexts;

	/**
	 * 
	 * @param serviceRepository
	 * @param invocationHandler
	 * @param port
	 * @param corePoolSize
	 * @param maxPoolSize
	 * @param workQueueSize
	 */
	public RequestProcessor() {
		this.serverChannels = new DefaultChannelGroup("Pigeon-Server-Channels");
		this.contexts = new ConcurrentHashMap<InvocationRequest, RequestContext>();
		timeCheckThreadPool.execute(new TimeoutListener(contexts));
	}

	public void addChannel(ProviderChannel channel) {
		Channel channel_ = null;
		this.serverChannels.add(channel.getChannel(channel_));
	}

	/**
	 * server 处理业务请求
	 * 
	 * @param request
	 * @param channel
	 */
	public void addRequest(final InvocationRequest request, final Channel channel) {
		RequestContext context = new RequestContext(((InetSocketAddress) channel.getRemoteAddress()).getHostName());
		// 必须新放入context，不然线程执行时找不到此context
		this.contexts.put(request, context);
		Runnable requestExecutor = new Runnable() {
			@Override
			public void run() {
				try {
					ProviderContext invocationContext = new DefaultProviderContext(request, new NettyChannel(channel));
					ServiceInvocationHandler invocationHandler = RequestProcessHandlerLoader
							.selectInvocationHandler(invocationContext.getRequest().getMessageType());
					if (invocationHandler != null) {
						invocationHandler.handle(invocationContext);
					}
				} catch (Throwable t) {
					logger.error("Process request failed with invocation handler, you should never be here.", t);
				} finally {
					contexts.remove(request);
				}
			}
		};
		context.setFuture(executorService.submit(requestExecutor));
	}

}
