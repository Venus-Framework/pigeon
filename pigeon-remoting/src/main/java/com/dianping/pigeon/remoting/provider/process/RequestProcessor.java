/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.provider.process;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor.AbortPolicy;

import org.apache.log4j.Logger;

import com.dianping.pigeon.component.invocation.InvocationRequest;
import com.dianping.pigeon.monitor.LoggerLoader;
import com.dianping.pigeon.remoting.common.process.ServiceInvocationHandler;
import com.dianping.pigeon.remoting.provider.component.context.ProviderContext;
import com.dianping.pigeon.threadpool.DefaultThreadPool;
import com.dianping.pigeon.threadpool.ThreadPool;

public class RequestProcessor {

	private static final Logger logger = LoggerLoader.getLogger(RequestProcessor.class);
	
	private static ThreadPool requestProcessThreadPool = new DefaultThreadPool(
			"Pigeon-Server-Request-Processor", 100, 300, new LinkedBlockingQueue<Runnable>(100),
			new AbortPolicy());

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
