/**
 * Dianping.com Inc.
 * Copyright (c) 00-0 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.provider.process.threadpool;

import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;

import com.dianping.pigeon.component.invocation.InvocationRequest;
import com.dianping.pigeon.monitor.LoggerLoader;
import com.dianping.pigeon.remoting.common.process.ServiceInvocationHandler;
import com.dianping.pigeon.remoting.provider.component.context.ProviderContext;
import com.dianping.pigeon.remoting.provider.config.ServerConfig;
import com.dianping.pigeon.remoting.provider.process.RequestProcessHandlerFactory;
import com.dianping.pigeon.remoting.provider.process.RequestProcessor;
import com.dianping.pigeon.threadpool.DefaultThreadPool;
import com.dianping.pigeon.threadpool.ThreadPool;

public class RequestThreadPoolProcessor implements RequestProcessor {

	private static final Logger logger = LoggerLoader.getLogger(RequestThreadPoolProcessor.class);
	private static ThreadPool requestProcessThreadPool = null;

	public RequestThreadPoolProcessor(ServerConfig serverConfig) {
		requestProcessThreadPool = new DefaultThreadPool("Pigeon-Server-Request-Processor",
				serverConfig.getCorePoolSize(), serverConfig.getMaxPoolSize(), new LinkedBlockingQueue<Runnable>(
						serverConfig.getWorkQueueSize()));
	}

	public void stop() {
	}

	public void processRequest(final InvocationRequest request, final ProviderContext providerContext) {
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
				}
			}
		};
		requestProcessThreadPool.submit(requestExecutor);
	}

}
