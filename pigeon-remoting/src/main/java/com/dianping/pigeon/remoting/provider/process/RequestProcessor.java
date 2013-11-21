/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.provider.process;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

import com.dianping.pigeon.component.invocation.InvocationRequest;
import com.dianping.pigeon.monitor.LoggerLoader;
import com.dianping.pigeon.remoting.common.filter.ServiceInvocationHandler;
import com.dianping.pigeon.remoting.provider.component.context.ProviderContext;
import com.dianping.pigeon.threadpool.NamedThreadFactory;

public class RequestProcessor {

	private static final Logger logger = LoggerLoader.getLogger(RequestProcessor.class);

	private static final ExecutorService executorService = Executors.newCachedThreadPool(new NamedThreadFactory(
			"pigeon-provider-request-processor", true));

	/**
	 * server 处理业务请求
	 * 
	 * @param request
	 * @param channel
	 */
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
		executorService.submit(requestExecutor);
	}

}
