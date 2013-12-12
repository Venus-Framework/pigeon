/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
/**
 * 
 */
package com.dianping.pigeon.remoting.provider.listener;

import java.util.Map;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;

import com.dianping.dpsf.exception.NetTimeoutException;
import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.extension.ExtensionLoader;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.monitor.MonitorLogger;
import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.provider.domain.ProviderContext;
import com.dianping.pigeon.util.ContextUtils;

public class RequestTimeoutListener implements Runnable {

	private static final Logger logger = LoggerLoader.getLogger(RequestTimeoutListener.class);
	private static final MonitorLogger monitorLogger = ExtensionLoader.getExtension(MonitorLogger.class);
	private Map<InvocationRequest, ProviderContext> requestContextMap;

	public RequestTimeoutListener(Map<InvocationRequest, ProviderContext> requestContextMap) {
		this.requestContextMap = requestContextMap;
	}

	public void run() {
		while (true) {
			try {
				long currentTime = System.currentTimeMillis();
				if (logger.isDebugEnabled()) {
					logger.debug("checking request timeout, count:" + requestContextMap.size());
				}
				for (InvocationRequest request : requestContextMap.keySet()) {
					if (request.getRequestTime() + request.getTimeout() < currentTime) {
						try {
							ProviderContext rc = requestContextMap.get(request);
							if (request.getMessageType() == Constants.MESSAGE_TYPE_HEART) {
								Future<?> future = rc.getFuture();
								if (future != null && !future.isCancelled()) {
									future.cancel(true);
								}
							} else {
								StringBuffer msg = new StringBuffer();
								msg.append("pigeon request timeout, seq:").append(request.getSequence())
										.append(", from:")
										.append(rc.getChannel().getRemoteAddress().getAddress().getHostAddress())
										.append(", to:")
										.append(ExtensionLoader.getExtension(ConfigManager.class).getLocalIp())
										.append(", timeout:").append(request.getTimeout()).append(", create time:")
										.append(request.getCreateMillisTime()).append(", request time:")
										.append(request.getRequestTime()).append(", process time:")
										.append(System.currentTimeMillis()).append("\r\n").append("service:")
										.append(request.getServiceName()).append(", method:")
										.append(request.getMethodName()).append("\r\n");
								Object[] params = request.getParameters();
								if (params != null && params.length > 0) {
									for (Object param : params) {
										msg.append("parameters:").append(String.valueOf(param));
									}
									msg.append("\r\n");
								}
								NetTimeoutException te = new NetTimeoutException(msg.toString());
								ContextUtils.setContext(request.getContext());
								logger.error(te.getMessage(), te);
								if (monitorLogger != null) {
									monitorLogger.logError(te);
								}
								Future<?> future = rc.getFuture();
								if (future != null && !future.isCancelled()) {
									future.cancel(true);
								}
							}
						} finally {
							requestContextMap.remove(request);
						}
					}
				}
				Thread.sleep(2000);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
	}
}
