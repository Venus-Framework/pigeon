/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.provider.listener;

import java.util.Map;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;

import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.extension.ExtensionLoader;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.monitor.Monitor;
import com.dianping.pigeon.monitor.MonitorLogger;
import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.provider.domain.ProviderContext;
import com.dianping.pigeon.remoting.provider.exception.ProcessTimeoutException;
import com.dianping.pigeon.util.ContextUtils;

public class RequestTimeoutListener implements Runnable {

	private static final Logger logger = LoggerLoader.getLogger(RequestTimeoutListener.class);
	private static final MonitorLogger monitorLogger = ExtensionLoader.getExtension(Monitor.class).getLogger();
	private Map<InvocationRequest, ProviderContext> requestContextMap;
	private long timeoutInterval = ExtensionLoader.getExtension(ConfigManager.class).getLongValue(
			Constants.KEY_TIMEOUT_INTERVAL, Constants.DEFAULT_TIMEOUT_INTERVAL);

	public RequestTimeoutListener(Map<InvocationRequest, ProviderContext> requestContextMap) {
		this.requestContextMap = requestContextMap;
	}

	public void run() {
		while (true) {
			try {
				long currentTime = System.currentTimeMillis();
				// if (logger.isDebugEnabled()) {
				// logger.debug("checking request timeout, count:" +
				// requestContextMap.size());
				// }
				for (InvocationRequest request : requestContextMap.keySet()) {
					if (request.getTimeout() > 0 && request.getCreateMillisTime() > 0
							&& (request.getCreateMillisTime() + request.getTimeout()) < currentTime) {
						try {
							ProviderContext rc = requestContextMap.get(request);
							if (request.getMessageType() == Constants.MESSAGE_TYPE_HEART) {
								Future<?> future = rc.getFuture();
								if (future != null && !future.isCancelled()) {
									future.cancel(true);
								}
							} else {
								StringBuffer msg = new StringBuffer();
								msg.append("timeout while processing request, from:")
										.append(rc.getChannel() == null ? "" : rc.getChannel().getRemoteAddress())
										.append(", to:")
										.append(ExtensionLoader.getExtension(ConfigManager.class).getLocalIp())
										.append(", process time:").append(System.currentTimeMillis()).append("\r\n")
										.append("request:").append(request);
								ProcessTimeoutException te = new ProcessTimeoutException(msg.toString());
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
				Thread.sleep(timeoutInterval);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
	}
}
