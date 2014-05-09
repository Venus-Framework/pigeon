/**
 * Dianping.com Inc.
 * Copyright (c) 00-0 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.provider.process.threadpool;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;

import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.common.process.ServiceInvocationHandler;
import com.dianping.pigeon.remoting.provider.config.ServerConfig;
import com.dianping.pigeon.remoting.provider.domain.ProviderContext;
import com.dianping.pigeon.remoting.provider.process.AbstractRequestProcessor;
import com.dianping.pigeon.remoting.provider.process.ProviderProcessHandlerFactory;
import com.dianping.pigeon.threadpool.DefaultThreadPool;
import com.dianping.pigeon.threadpool.ThreadPool;

public class RequestThreadPoolProcessor extends AbstractRequestProcessor {

	private static final Logger logger = LoggerLoader.getLogger(RequestThreadPoolProcessor.class);
	private static ThreadPool requestProcessThreadPool = null;

	public RequestThreadPoolProcessor(ServerConfig serverConfig) {
		requestProcessThreadPool = new DefaultThreadPool("Pigeon-Server-Request-Processor",
				serverConfig.getCorePoolSize(), serverConfig.getMaxPoolSize(), new LinkedBlockingQueue<Runnable>(
						serverConfig.getWorkQueueSize()));
	}

	public void doStop() {
	}

	public Future<InvocationResponse> doProcessRequest(final InvocationRequest request,
			final ProviderContext providerContext) {
		this.requestContextMap.put(request, providerContext);
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
		
		return requestProcessThreadPool.submit(requestExecutor);
	}
}
