/**
 * Dianping.com Inc.
 * Copyright (c) 2003-${year} All Rights Reserved.
 */
package com.dianping.pigeon.remoting.provider.process.filter;

import java.net.InetSocketAddress;

import org.apache.log4j.Logger;

import com.dianping.pigeon.component.invocation.InvocationRequest;
import com.dianping.pigeon.component.invocation.InvocationResponse;
import com.dianping.pigeon.extension.ExtensionLoader;
import com.dianping.pigeon.monitor.LoggerLoader;
import com.dianping.pigeon.monitor.Monitor;
import com.dianping.pigeon.monitor.MonitorLogger;
import com.dianping.pigeon.monitor.MonitorTransaction;
import com.dianping.pigeon.remoting.common.filter.ServiceInvocationFilter;
import com.dianping.pigeon.remoting.common.filter.ServiceInvocationHandler;
import com.dianping.pigeon.remoting.common.util.InvocationUtils;
import com.dianping.pigeon.remoting.provider.component.ProviderChannel;
import com.dianping.pigeon.remoting.provider.component.context.ProviderContext;

public class MonitorProcessFilter implements ServiceInvocationFilter<ProviderContext> {

	private static final Logger logger = LoggerLoader.getLogger(MonitorProcessFilter.class);
	private Monitor monitor = ExtensionLoader.getExtension(Monitor.class);

	@Override
	public InvocationResponse invoke(ServiceInvocationHandler handler, ProviderContext invocationContext)
			throws Throwable {
		if (logger.isInfoEnabled()) {
			logger.info("invoke the MonitorProcessFilter, invocationContext:" + invocationContext);
		}
		MonitorTransaction transaction = null;
		MonitorLogger monitorLogger = null;
		if (monitor != null) {
			monitorLogger = monitor.createLogger();
		}
		if (monitorLogger != null) {
			InvocationRequest request = invocationContext.getRequest();
			ProviderChannel channel = invocationContext.getChannel();
			try {
				transaction = monitorLogger.createTransaction("PigeonService", InvocationUtils.getRemoteCallFullName(
						request.getServiceName(), request.getMethodName(), request.getParamClassName()),
						invocationContext);
				if (transaction != null) {
					InetSocketAddress address = channel.getRemoteAddress();
					//String parameters = Stringizers.forJson().from(request.getParameters(), 1000, 50);
					String parameters = request.getParameters() + "";
					monitorLogger.logEvent("PigeonService.client", address.getAddress().getHostAddress() + ":"
							+ address.getPort(), parameters);

					transaction.writeMonitorContext();

					transaction.setStatusOk();
				}
			} catch (Exception e) {
				monitorLogger.logError(e);
			}
		}
		try {
			handler.handle(invocationContext);
		} catch (RuntimeException e) {
			if (transaction != null) {
				try {
					transaction.setStatusError(e);
				} catch (Exception e2) {
					monitorLogger.logMonitorError(e2);
				}
			}
			if (monitorLogger != null) {
				monitorLogger.logError(e);
			}
		} finally {
			if (transaction != null) {
				try {
					transaction.complete();
				} catch (Exception e) {
					monitorLogger.logMonitorError(e);
				}
			}
			if (invocationContext.getServiceError() != null && monitorLogger != null) {
				monitorLogger.logError(invocationContext.getServiceError());
			}
		}
		return null;
	}

}
