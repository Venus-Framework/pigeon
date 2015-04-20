/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.provider.process.actor;

import java.util.Map;

import org.apache.log4j.Logger;

import akka.actor.UntypedActor;

import com.dianping.pigeon.extension.ExtensionLoader;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.monitor.Monitor;
import com.dianping.pigeon.monitor.MonitorLogger;
import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.common.process.ServiceInvocationHandler;
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

	private Map<InvocationRequest, ProviderContext> requestContextMap;

	private static final MonitorLogger monitorLogger = ExtensionLoader.getExtension(Monitor.class).getLogger();

	public RequestEventActor(Map<InvocationRequest, ProviderContext> requestContextMap) {
		this.requestContextMap = requestContextMap;
	}

	@Override
	public void onReceive(Object message) throws Exception {
		if (message instanceof RequestEvent) {
			RequestEvent event = (RequestEvent) message;
			ProviderContext providerContext = event.getProviderContext();
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
			} finally {
				requestContextMap.remove(request);
			}
		}
	}

}