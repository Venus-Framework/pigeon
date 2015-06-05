/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.provider.process.akka;

import java.util.Map;

import com.dianping.pigeon.log.LoggerLoader;
import org.apache.logging.log4j.Logger;

import akka.actor.UntypedActor;

import com.dianping.pigeon.extension.ExtensionLoader;
import com.dianping.pigeon.monitor.Monitor;
import com.dianping.pigeon.monitor.MonitorLogger;
import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.common.process.ServiceInvocationHandler;
import com.dianping.pigeon.remoting.provider.domain.DefaultProviderContext;
import com.dianping.pigeon.remoting.provider.domain.ProviderContext;
import com.dianping.pigeon.remoting.provider.exception.RequestAbortedException;
import com.dianping.pigeon.remoting.provider.process.ProviderProcessHandlerFactory;
import com.dianping.pigeon.remoting.provider.process.event.RequestEvent;
import com.dianping.pigeon.remoting.provider.util.ProviderUtils;

/**
 * 
 * @author xiangwu
 * 
 */
public class RequestEventActor extends UntypedActor {

	private static final Logger logger = LoggerLoader.getLogger(RequestEventActor.class);

	private static final MonitorLogger monitorLogger = ExtensionLoader.getExtension(Monitor.class).getLogger();

	private Map<InvocationRequest, ProviderContext> requestContextMap;

	public RequestEventActor(Map<InvocationRequest, ProviderContext> requestContextMap) {
		this.requestContextMap = requestContextMap;
	}

	@Override
	public void onReceive(Object message) throws Exception {
		if (message instanceof RequestEvent) {
			RequestEvent event = (RequestEvent) message;
			DefaultProviderContext providerContext = (DefaultProviderContext) event.getProviderContext();
			InvocationRequest request = providerContext.getRequest();
			boolean timeout = false;
			long currentTime = System.currentTimeMillis();
			if (request.getTimeout() > 0 && request.getCreateMillisTime() > 0
					&& request.getCreateMillisTime() + request.getTimeout() < currentTime) {
				timeout = true;
			}
			try {
				if (timeout) {
					Exception te = null;
					te = new RequestAbortedException(ProviderUtils.getRequestDetailInfo(
							"the request has not been executed by actor", providerContext, request));
					te.setStackTrace(new StackTraceElement[] {});
					logger.error(te.getMessage(), te);
					if (monitorLogger != null) {
						monitorLogger.logError(te);
					}
				} else {
					ServiceInvocationHandler invocationHandler = ProviderProcessHandlerFactory
							.selectInvocationHandler(request.getMessageType());
					if (invocationHandler != null) {
						providerContext.setThread(Thread.currentThread());
						invocationHandler.handle(providerContext);
					}
				}
			} catch (Throwable t) {
				logger.error(ProviderUtils.getRequestDetailInfo("Process request failed with event actor",
						providerContext, request), t);
				if (monitorLogger != null) {
					monitorLogger.logError(t);
				}
			} finally {
				requestContextMap.remove(request);
			}
		}
	}

}