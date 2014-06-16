/**
 * Dianping.com Inc.
 * Copyright (c) 00-0 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.provider.process.threadpool;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

import com.dianping.pigeon.config.ConfigManagerLoader;
import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.common.exception.RejectedException;
import com.dianping.pigeon.remoting.common.process.ServiceInvocationHandler;
import com.dianping.pigeon.remoting.provider.config.ServerConfig;
import com.dianping.pigeon.remoting.provider.domain.ProviderContext;
import com.dianping.pigeon.remoting.provider.process.AbstractRequestProcessor;
import com.dianping.pigeon.remoting.provider.process.ProviderProcessHandlerFactory;
import com.dianping.pigeon.threadpool.DefaultThreadPool;
import com.dianping.pigeon.threadpool.ThreadPool;

public class RequestThreadPoolProcessor extends AbstractRequestProcessor {

	private static final boolean useStandalonePool = ConfigManagerLoader.getConfigManager().getBooleanValue(
			"pigeon.provider.pool.standalone", false);

	private static ThreadPool sharedRequestProcessThreadPool = null;

	private ThreadPool requestProcessThreadPool = null;

	public RequestThreadPoolProcessor(ServerConfig serverConfig) {
		if (useStandalonePool) {
			requestProcessThreadPool = new DefaultThreadPool("Pigeon-Server-Request-Processor-"
					+ serverConfig.getProtocol() + "-" + serverConfig.getPort(), serverConfig.getCorePoolSize(),
					serverConfig.getMaxPoolSize(), new LinkedBlockingQueue<Runnable>(serverConfig.getWorkQueueSize()));
		} else {
			sharedRequestProcessThreadPool = new DefaultThreadPool("Pigeon-Server-Request-Processor",
					serverConfig.getCorePoolSize(), serverConfig.getMaxPoolSize(), new LinkedBlockingQueue<Runnable>(
							serverConfig.getWorkQueueSize()));
			requestProcessThreadPool = sharedRequestProcessThreadPool;
		}
	}

	public void doStop() {
	}

	public Future<InvocationResponse> doProcessRequest(final InvocationRequest request,
			final ProviderContext providerContext) {
		requestContextMap.put(request, providerContext);
		Callable<InvocationResponse> requestExecutor = new Callable<InvocationResponse>() {

			@Override
			public InvocationResponse call() throws Exception {
				try {
					ServiceInvocationHandler invocationHandler = ProviderProcessHandlerFactory
							.selectInvocationHandler(providerContext.getRequest().getMessageType());
					if (invocationHandler != null) {
						providerContext.setThread(Thread.currentThread());
						return invocationHandler.handle(providerContext);
					}
				} catch (Throwable t) {
					logger.error("Process request failed with invocation handler, you should never be here.", t);
				} finally {
					requestContextMap.remove(request);
				}
				return null;
			}
		};

		ThreadPool pool = null;
		if (useStandalonePool) {
			pool = requestProcessThreadPool;
		} else {
			pool = sharedRequestProcessThreadPool;
		}
		try {
			return pool.submit(requestExecutor);
		} catch (RejectedExecutionException e) {
			throw new RejectedException(getProcessorStatistics(), e);
		}
	}

	@Override
	public String getProcessorStatistics() {
		ThreadPool pool = null;
		if (useStandalonePool) {
			pool = requestProcessThreadPool;
		} else {
			pool = sharedRequestProcessThreadPool;
		}
		ThreadPoolExecutor e = pool.getExecutor();
		String stats = String.format(
				"request pool size:%d(active:%d,core:%d,max:%d,largest:%d),task count:%d(completed:%d),queue size:%d",
				e.getPoolSize(), e.getActiveCount(), e.getCorePoolSize(), e.getMaximumPoolSize(),
				e.getLargestPoolSize(), e.getTaskCount(), e.getCompletedTaskCount(), e.getQueue().size());
		return stats;
	}
}
