/**
 * Dianping.com Inc.
 * Copyright (c) 2003-${year} All Rights Reserved.
 */
package com.dianping.pigeon.remoting.provider.process.filter;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.Logger;
import org.springframework.util.CollectionUtils;

import com.dianping.pigeon.config.ConfigManagerLoader;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.monitor.Monitor;
import com.dianping.pigeon.monitor.MonitorLoader;
import com.dianping.pigeon.monitor.MonitorTransaction;
import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.common.monitor.SizeMonitor;
import com.dianping.pigeon.remoting.common.process.ServiceInvocationFilter;
import com.dianping.pigeon.remoting.common.process.ServiceInvocationHandler;
import com.dianping.pigeon.remoting.common.util.Constants;
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

	private static final Logger accessLogger = LoggerLoader.getLogger("pigeon-access");

	private static final Monitor monitor = MonitorLoader.getMonitor();

	private static boolean isAccessLogEnabled = ConfigManagerLoader.getConfigManager().getBooleanValue(
			"pigeon.provider.accesslog.enable", false);

	@Override
	public InvocationResponse invoke(ServiceInvocationHandler handler, ProviderContext invocationContext)
			throws Throwable {
		if (logger.isDebugEnabled()) {
			logger.debug("invoke the MonitorProcessFilter, invocationContext:" + invocationContext);
		}
		InvocationRequest request = invocationContext.getRequest();
		ProviderChannel channel = invocationContext.getChannel();
		MonitorTransaction transaction = null;
		String fromIp = null;
		boolean error = false;
		if (monitor != null) {
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
				transaction = monitor.createTransaction("PigeonService", strMethod, invocationContext,
						!Constants.REPLY_MANUAL);
				monitor.setCurrentServiceTransaction(transaction);
				transaction.logEvent("PigeonService.app", request.getApp(), "");
				String parameters = "";
				fromIp = channel.getRemoteAddress();
				if (Constants.LOG_PARAMETERS) {
					StringBuilder event = new StringBuilder();
					event.append(InvocationUtils.toJsonString(request.getParameters(), 1000, 50));
					parameters = event.toString();
				}
				transaction.logEvent("PigeonService.client", fromIp, parameters);
				transaction.logEvent("PigeonService.QPS", "S" + Calendar.getInstance().get(Calendar.SECOND), "");
				String reqSize = SizeMonitor.getInstance().getLogSize(request.getSize());
				if (reqSize != null) {
					transaction.logEvent("PigeonService.requestSize", reqSize, "" + request.getSize());
				}

				if (!Constants.PROTOCOL_DEFAULT.equals(channel.getProtocol())) {
					transaction.addData("Protocol", channel.getProtocol());
				}
				ContextUtils.putLocalContext("CurrentServiceUrl",
						request.getServiceName() + "#" + request.getMethodName());
			} catch (Throwable e) {
				monitor.logError(e);
			}
		}
		InvocationResponse response = null;
		try {
			try {
				response = handler.handle(invocationContext);
			} catch (RuntimeException e) {
				error = true;
				if (transaction != null) {
					try {
						transaction.setStatusError(e);
					} catch (Throwable e2) {
						monitor.logMonitorError(e2);
					}
				}
				if (monitor != null) {
					monitor.logError(e);
				}
			}
			if (transaction != null) {
				try {
					if (response != null) {
						transaction.logEvent("PigeonService.responseSize",
								SizeMonitor.getInstance().getLogSize(response.getSize()), "" + response.getSize());
					}
					Map<String, Serializable> globalContext = ContextUtils.getGlobalContext();
					if (!CollectionUtils.isEmpty(globalContext)) {
						String sourceApp = (String) globalContext.get(Constants.CONTEXT_KEY_SOURCE_APP);
						if (sourceApp != null) {
							transaction.addData("SourceApp", sourceApp);
						}
						String sourceIp = (String) globalContext.get(Constants.CONTEXT_KEY_SOURCE_IP);
						if (sourceIp != null) {
							transaction.addData("SourceIp", sourceIp);
						}
					}
					String from = (String) ContextUtils.getLocalContext("RequestIp");
					if (from != null) {
						transaction.addData("RequestIp", from);
						transaction.logEvent("PigeonConsole.client", from, "");
					}
					transaction.writeMonitorContext();
					transaction.setStatusOk();
				} catch (Throwable e) {
					monitor.logError(e);
				}
			}
		} finally {
			if (invocationContext.getServiceError() != null && monitor != null) {
				monitor.logError(invocationContext.getServiceError());
			}
			if (transaction != null) {
				try {
					if (TimelineManager.isEnabled()) {
						Timeline timeline = TimelineManager.tryRemoveTimeline(request, TimelineManager.getRemoteIp());
						transaction.addData("Timeline", timeline);
					}
					if (transaction.isAutoCommit() || error) {
						transaction.complete();
					}
					if (isAccessLogEnabled) {
						accessLogger.info(new StringBuilder().append(request.getApp()).append("@").append(fromIp)
								.append("@").append(request).toString());
					}
				} catch (Throwable e) {
					monitor.logMonitorError(e);
				}
				if (monitor != null) {
					monitor.clearServiceTransaction();
				}
			}
			ContextUtils.clearContext();
			ContextUtils.clearLocalContext();
			ContextUtils.clearRequestContext();
			ContextUtils.clearGlobalContext();
		}
		return response;
	}
}
