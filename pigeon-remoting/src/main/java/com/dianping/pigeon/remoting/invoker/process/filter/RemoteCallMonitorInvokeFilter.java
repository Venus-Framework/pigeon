/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.invoker.process.filter;

import com.dianping.pigeon.extension.ExtensionLoader;
import com.dianping.pigeon.monitor.Monitor;
import com.dianping.pigeon.monitor.MonitorLogger;
import com.dianping.pigeon.monitor.MonitorTransaction;
import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.common.process.ServiceInvocationHandler;
import com.dianping.pigeon.remoting.common.util.InvocationUtils;
import com.dianping.pigeon.remoting.invoker.Client;
import com.dianping.pigeon.remoting.invoker.config.InvokerConfig;
import com.dianping.pigeon.remoting.invoker.domain.InvokerContext;

public class RemoteCallMonitorInvokeFilter extends InvocationInvokeFilter {

	private Monitor monitor = ExtensionLoader.getExtension(Monitor.class);

	@Override
	public InvocationResponse invoke(ServiceInvocationHandler handler, InvokerContext invocationContext)
			throws Throwable {
		MonitorLogger logger = null;
		MonitorTransaction transaction = null;
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
						InvocationRequest request = invocationContext.getRequest();
						logger.logEvent("PigeonCall.server", client.getAddress(),
								InvocationUtils.toJsonString(request.getParameters(), 1000, 50));

						transaction.readMonitorContext();
					}
				} catch (Exception e) {
					logger.logMonitorError(e);
				}
			}
		}
		try {
			return handler.handle(invocationContext);
		} catch (Throwable e) {
			if (transaction != null) {
				try {
					transaction.setStatusError(e);
				} catch (Exception e2) {
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
					transaction.complete();
				} catch (Exception e) {
					logger.logMonitorError(e);
				}
			}
		}
	}

}
