/**
 * Dianping.com Inc.
 * Copyright (c) 2003-${year} All Rights Reserved.
 */
package com.dianping.pigeon.remoting.provider.process.filter;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.dianping.pigeon.config.ConfigManagerLoader;
import com.dianping.pigeon.extension.ExtensionLoader;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.monitor.Monitor;
import com.dianping.pigeon.monitor.MonitorLogger;
import com.dianping.pigeon.monitor.MonitorTransaction;
import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.common.monitor.MonitorHelper;
import com.dianping.pigeon.remoting.common.monitor.SizeMonitor;
import com.dianping.pigeon.remoting.common.monitor.SizeMonitor.SizeMonitorInfo;
import com.dianping.pigeon.remoting.common.process.ServiceInvocationFilter;
import com.dianping.pigeon.remoting.common.process.ServiceInvocationHandler;
import com.dianping.pigeon.remoting.common.util.InvocationUtils;
import com.dianping.pigeon.remoting.common.util.TimelineManager;
import com.dianping.pigeon.remoting.common.util.TimelineManager.Timeline;
import com.dianping.pigeon.remoting.provider.domain.ProviderChannel;
import com.dianping.pigeon.remoting.provider.domain.ProviderContext;
import com.dianping.pigeon.remoting.provider.service.method.ServiceMethod;
import com.dianping.pigeon.remoting.provider.service.method.ServiceMethodFactory;
import com.dianping.pigeon.util.ContextUtils;

public class MonitorProcessFilter implements ServiceInvocationFilter<ProviderContext> {

	private static final Logger logger = LoggerLoader.getLogger(MonitorProcessFilter.class);

	private static final Logger accessLogger = Logger.getLogger("pigeon-access");

	private Monitor monitor = ExtensionLoader.getExtension(Monitor.class);

	private static boolean isAccessLogEnabled = ConfigManagerLoader.getConfigManager().getBooleanValue(
			"pigeon.provider.accesslog.enable", false);

	private String getIp(String address) {
		String ip = address;
		if (address != null) {
			int idx = address.indexOf(":");
			if (idx != -1) {
				ip = address.substring(0, idx);
			}
		}
		return ip;
	}

	@Override
	public InvocationResponse invoke(ServiceInvocationHandler handler, ProviderContext invocationContext)
			throws Throwable {
		if (logger.isDebugEnabled()) {
			logger.debug("invoke the MonitorProcessFilter, invocationContext:" + invocationContext);
		}
		InvocationRequest request = invocationContext.getRequest();
		ProviderChannel channel = invocationContext.getChannel();
		MonitorTransaction transaction = null;
		MonitorLogger monitorLogger = null;
		boolean timeout = false;
		String fromIp = null;
		if (monitor != null) {
			monitorLogger = monitor.getLogger();
		}
		if (monitorLogger != null) {
			String strMethod = null;
			try {
				ServiceMethod serviceMethod = ServiceMethodFactory.getMethod(request);
				invocationContext.setServiceMethod(serviceMethod);
				strMethod = InvocationUtils.getRemoteCallFullName(request.getServiceName(), request.getMethodName(),
						serviceMethod.getOriginalParameterClasses());
			} catch (Throwable e) {
			}
			try {
				if (StringUtils.isBlank(strMethod)) {
					strMethod = InvocationUtils.getRemoteCallFullName(request.getServiceName(),
							request.getMethodName(), request.getParamClassName());
				}
				transaction = monitorLogger.createTransaction("PigeonService", strMethod, invocationContext);
			} catch (Throwable e) {
				monitorLogger.logError(e);
			}
		}
		try {
			InvocationResponse response = null;
			try {
				response = handler.handle(invocationContext);
				long currentTime = System.currentTimeMillis();
				if (request.getTimeout() > 0 && request.getCreateMillisTime() > 0
						&& request.getCreateMillisTime() + request.getTimeout() < currentTime) {
					timeout = true;
				}
			} catch (RuntimeException e) {
				if (transaction != null) {
					try {
						transaction.setStatusError(e);
					} catch (Throwable e2) {
						monitorLogger.logMonitorError(e2);
					}
				}
				if (monitorLogger != null) {
					monitorLogger.logError(e);
				}
			}
			if (transaction != null) {
				try {
					StringBuilder event = new StringBuilder();
					event.append(InvocationUtils.toJsonString(request.getParameters(), 1000, 50));
					fromIp = getIp(channel.getRemoteAddress());
					monitorLogger.logEvent("PigeonService.client", fromIp, event.toString());
					monitorLogger.logEvent("PigeonService.app", request.getApp(), "");
					if (SizeMonitor.isEnable()) {
						SizeMonitorInfo sizeInfo = MonitorHelper.getSize();
						if (sizeInfo != null) {
							SizeMonitor.getInstance().logSize(sizeInfo.getSize(), sizeInfo.getEvent(), fromIp);
						}
					}
					transaction.writeMonitorContext();
					transaction.setStatusOk();
				} catch (Throwable e) {
					monitorLogger.logError(e);
				}
			}
		} finally {
			if (invocationContext.getServiceError() != null && monitorLogger != null) {
				monitorLogger.logError(invocationContext.getServiceError());
			}
			if (transaction != null) {
				try {
					Timeline timeline = TimelineManager.tryRemoveTimeline(request, TimelineManager.getRemoteIp());
					if (TimelineManager.isEnabled()
							&& (timeout || TimelineManager.isAbnormalTimeline(request, TimelineManager.getRemoteIp()))) {
						transaction.addData("Timeline", timeline);
					}
					transaction.complete();
					if (isAccessLogEnabled) {
						accessLogger.debug(request.getApp() + "@" + fromIp + "@" + request + "@" + timeline);
					}
				} catch (Throwable e) {
					monitorLogger.logMonitorError(e);
				}
			}
			ContextUtils.clearContext();
			ContextUtils.clearLocalContext();
		}
		return null;
	}
}
