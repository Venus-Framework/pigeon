/**
 * Dianping.com Inc.
 * Copyright (c) 00-0 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.provider.process.threadpool;

import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;

import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.common.process.ServiceInvocationHandler;
import com.dianping.pigeon.remoting.provider.config.ServerConfig;
import com.dianping.pigeon.remoting.provider.domain.ProviderContext;
import com.dianping.pigeon.remoting.provider.process.AbstractRequestProcessor;
import com.dianping.pigeon.remoting.provider.process.RequestProcessHandlerFactory;
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

	public Future<?> doProcessRequest(final InvocationRequest request, final ProviderContext providerContext) {
		this.requestContextMap.put(request, providerContext);
		Runnable requestExecutor = new Runnable() {
			@Override
			public void run() {
				try {
					ServiceInvocationHandler invocationHandler = RequestProcessHandlerFactory
							.selectInvocationHandler(providerContext.getRequest().getMessageType());
					if (invocationHandler != null) {
						invocationHandler.handle(providerContext);
					}
				} catch (Throwable t) {
					logger.error("Process request failed with invocation handler, you should never be here.", t);
				} finally {
					requestContextMap.remove(request);
				}
			}
		};
		return requestProcessThreadPool.submit(requestExecutor);
	}

}
