/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.provider.process.disruptor;

import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;

import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.remoting.common.process.ServiceInvocationHandler;
import com.dianping.pigeon.remoting.provider.domain.ProviderContext;
import com.dianping.pigeon.remoting.provider.process.RequestProcessHandlerFactory;
import com.dianping.pigeon.remoting.provider.process.event.RequestEvent;
import com.dianping.pigeon.threadpool.DefaultThreadPool;
import com.dianping.pigeon.threadpool.ThreadPool;
import com.lmax.disruptor.EventHandler;

public class RequestEventDisruptorHandler implements EventHandler<RequestEvent> {

	private static final Logger logger = LoggerLoader.getLogger(RequestEventDisruptorHandler.class);
	private static ThreadPool requestProcessThreadPool = new DefaultThreadPool("Pigeon-Server-Request-Processor-Disruptor",
			20, 300, new LinkedBlockingQueue<Runnable>(
					100));
	
	@Override
	public void onEvent(RequestEvent event, long sequence, boolean endOfBatch) throws Exception {
		final ProviderContext providerContext = event.getProviderContext();
		Runnable task = new Runnable() {
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
		requestProcessThreadPool.submit(task);
	}
}
