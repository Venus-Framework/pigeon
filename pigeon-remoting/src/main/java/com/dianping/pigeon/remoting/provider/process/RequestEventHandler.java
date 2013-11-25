/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.provider.process;

import org.apache.log4j.Logger;

import com.dianping.pigeon.monitor.LoggerLoader;
import com.dianping.pigeon.remoting.common.process.ServiceInvocationHandler;
import com.dianping.pigeon.remoting.provider.component.context.ProviderContext;
import com.dianping.pigeon.remoting.provider.process.event.RequestEvent;
import com.lmax.disruptor.EventHandler;

public class RequestEventHandler implements EventHandler<RequestEvent> {

	private static final Logger logger = LoggerLoader.getLogger(RequestEventHandler.class);

	public void onEvent(RequestEvent event, long sequence, boolean endOfBatch) throws Exception {
		ProviderContext providerContext = event.getProviderContext();
		ServiceInvocationHandler invocationHandler = RequestProcessHandlerFactory
				.selectInvocationHandler(providerContext.getRequest().getMessageType());
		if (invocationHandler != null) {
			try {
				invocationHandler.handle(providerContext);
			} catch (Throwable e) {
				logger.error("process request failed with invocation handler", e);
			}
		}
	}
}
