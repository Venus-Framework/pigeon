/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.invoker.process.filter;

import org.apache.log4j.Logger;

import com.dianping.dpsf.exception.NetTimeoutException;
import com.dianping.pigeon.extension.ExtensionLoader;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.monitor.Monitor;
import com.dianping.pigeon.monitor.MonitorLogger;
import com.dianping.pigeon.monitor.MonitorTransaction;
import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.common.process.ServiceInvocationHandler;
import com.dianping.pigeon.remoting.common.util.InvocationUtils;
import com.dianping.pigeon.remoting.common.util.TimelineManager;
import com.dianping.pigeon.remoting.common.util.TimelineManager.Timeline;
import com.dianping.pigeon.remoting.invoker.Client;
import com.dianping.pigeon.remoting.invoker.config.InvokerConfig;
import com.dianping.pigeon.remoting.invoker.domain.InvokerContext;

public class RemoteCallMonitorInvokeFilter extends InvocationInvokeFilter {

	private static final Logger logger = LoggerLoader.getLogger(RemoteCallMonitorInvokeFilter.class);

	private Monitor monitor = ExtensionLoader.getExtension(Monitor.class);

	@Override
	public InvocationResponse invoke(ServiceInvocationHandler handler, InvokerContext invocationContext)
			throws Throwable {
		if (logger.isDebugEnabled()) {
			logger.debug("invoke the RemoteCallMonitorInvokeFilter, invocationContext:" + invocationContext);
		}
		MonitorLogger logger = null;
		MonitorTransaction transaction = null;
		InvocationRequest request = invocationContext.getRequest();
		boolean timeout = false;
		if (monitor != null) {
			InvokerConfig<?> invokerConfig = invocationContext.getInvokerConfig();
			logger = monitor.getLogger();
			if (logger != null) {
				try {
					transaction = logger.createTransaction(
							"PigeonCall",
							InvocationUtils.getRemoteCallFullName(invokerConfig.getUrl(),
									invocationContext.getMethodName(), invocationContext.getParameterTypes()),
							invocationContext);
					if (transaction != null) {
						transaction.setStatusOk();
						transaction.addData("CallType", invokerConfig.getCallType());
						transaction.addData("Timeout", invokerConfig.getTimeout());

						Client client = invocationContext.getClient();
						logger.logEvent("PigeonCall.server", client.getAddress(),
								InvocationUtils.toJsonString(request.getParameters(), 1000, 50));

						transaction.readMonitorContext();
					}
				} catch (Throwable e) {
					logger.logMonitorError(e);
				}
			}
		}
		try {
			return handler.handle(invocationContext);
		} catch(NetTimeoutException e) {
			timeout = true;
			if (transaction != null) {
				try {
					transaction.setStatusError(e);
				} catch (Throwable e2) {
					logger.logMonitorError(e2);
				}
			}
			if (logger != null) {
				logger.logError(e);
			}
			throw e;
		} catch (Throwable e) {
			if (transaction != null) {
				try {
					transaction.setStatusError(e);
				} catch (Throwable e2) {
					logger.logMonitorError(e2);
				}
			}
			if (logger != null) {
				logger.logError(e);
			}
			throw e;
		} finally {
			if (transaction != null) {
				try {
					if(TimelineManager.isEnabled() && 
					  (timeout || TimelineManager.isAbnormalTimeline(request, TimelineManager.getLocalIp()))) {
						Timeline timeline = TimelineManager.getTimeline(request, TimelineManager.getLocalIp());
						transaction.addData("Timeline", timeline);
						RemoteCallMonitorInvokeFilter.logger.warn(String.format("request- %s, timeline- %s", request, timeline));
					}
					transaction.complete();
				} catch (Throwable e) {
					logger.logMonitorError(e);
				}
			}
		}
	}

}
