/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.provider.process.akka;

import org.apache.log4j.Logger;

import akka.actor.UntypedActor;

import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.remoting.common.process.ServiceInvocationHandler;
import com.dianping.pigeon.remoting.provider.component.context.ProviderContext;
import com.dianping.pigeon.remoting.provider.process.RequestProcessHandlerFactory;
import com.dianping.pigeon.remoting.provider.process.event.RequestEvent;

public class RequestEventAkkaHandler extends UntypedActor {

	private static final Logger logger = LoggerLoader.getLogger(RequestEventAkkaHandler.class);

	@Override
	public void onReceive(Object message) throws Exception {
		RequestEvent event = (RequestEvent) message;
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
