/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.provider.process.akka;

import java.util.Map;

import org.apache.log4j.Logger;

import akka.actor.UntypedActor;

import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.common.process.ServiceInvocationHandler;
import com.dianping.pigeon.remoting.provider.domain.ProviderContext;
import com.dianping.pigeon.remoting.provider.process.ProviderProcessHandlerFactory;
import com.dianping.pigeon.remoting.provider.process.event.RequestEvent;

public class RequestEventAkkaHandler extends UntypedActor {

	private static final Logger logger = LoggerLoader.getLogger(RequestEventAkkaHandler.class);

	private Map<InvocationRequest, ProviderContext> requestContextMap;

	public RequestEventAkkaHandler(Map<InvocationRequest, ProviderContext> requestContextMap) {
		this.requestContextMap = requestContextMap;
	}

	@Override
	public void onReceive(Object message) throws Exception {
		RequestEvent event = (RequestEvent) message;
		ProviderContext providerContext = event.getProviderContext();
		try {
			ServiceInvocationHandler invocationHandler = ProviderProcessHandlerFactory
					.selectInvocationHandler(providerContext.getRequest().getMessageType());
			if (invocationHandler != null) {
				providerContext.setThread(Thread.currentThread());
				invocationHandler.handle(providerContext);
			}
		} catch (Throwable t) {
			logger.error("Process request failed with invocation handler, you should never be here.", t);
		} finally {
			requestContextMap.remove(providerContext.getRequest());
		}
	}
}