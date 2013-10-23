/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.invoker.filter;

import java.lang.reflect.Method;

import com.dianping.pigeon.component.invocation.InvocationResponse;
import com.dianping.pigeon.extension.ExtensionLoader;
import com.dianping.pigeon.monitor.Monitor;
import com.dianping.pigeon.monitor.MonitorLogger;
import com.dianping.pigeon.monitor.MonitorTransaction;
import com.dianping.pigeon.remoting.common.filter.ServiceInvocationHandler;
import com.dianping.pigeon.remoting.common.util.InvocationUtils;
import com.dianping.pigeon.remoting.invoker.Client;
import com.dianping.pigeon.remoting.invoker.component.InvokerMetaData;
import com.dianping.pigeon.remoting.invoker.component.context.InvokerContext;
import com.site.helper.Stringizers;

public class RemoteCallMonitorInvokeFilter extends InvocationInvokeFilter {

	private Monitor monitor = ExtensionLoader.getExtension(Monitor.class);

	@Override
	public InvocationResponse invoke(ServiceInvocationHandler handler, InvokerContext invocationContext)
			throws Throwable {
		MonitorLogger logger = null;
		MonitorTransaction transaction = null;
		if (monitor != null) {
			InvokerMetaData metaData = invocationContext.getMetaData();
			Method method = invocationContext.getMethod();
			logger = monitor.createLogger();
			if (logger != null) {
				try {
					transaction = logger.createTransaction(
							"PigeonCall",
							InvocationUtils.getRemoteCallFullName(metaData.getServiceName(), method.getName(),
									method.getParameterTypes()));
					if (transaction != null) {
						transaction.setStatusOk();
						transaction.addData("CallType", metaData.getCallMethod());

						Client client = invocationContext.getClient();

						logger.logEvent("PigeonCall.server", client.getAddress(),
								Stringizers.forJson().from(invocationContext.getArguments(), 1000, 50));

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
