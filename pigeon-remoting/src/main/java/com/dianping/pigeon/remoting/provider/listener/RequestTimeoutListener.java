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
import com.dianping.pigeon.remoting.common.util.InvocationUtils;
import com.dianping.pigeon.remoting.common.util.TimelineManager;
import com.dianping.pigeon.remoting.provider.domain.ProviderContext;
import com.dianping.pigeon.remoting.provider.exception.ProcessTimeoutException;
import com.dianping.pigeon.util.ContextUtils;

public class RequestTimeoutListener implements Runnable {

	private static final Logger logger = LoggerLoader.getLogger(RequestTimeoutListener.class);
	private static final MonitorLogger monitorLogger = ExtensionLoader.getExtension(Monitor.class).getLogger();
	private Map<InvocationRequest, ProviderContext> requestContextMap;
	private static ConfigManager configManager = ExtensionLoader.getExtension(ConfigManager.class);
	private long timeoutInterval = configManager.getLongValue(Constants.KEY_TIMEOUT_INTERVAL,
			Constants.DEFAULT_TIMEOUT_INTERVAL);
	private boolean defaultCancelTimeout = configManager.getBooleanValue(Constants.KEY_TIMEOUT_CANCEL,
			Constants.DEFAULT_TIMEOUT_CANCEL);
	private int count = 0;
	
	public RequestTimeoutListener(Map<InvocationRequest, ProviderContext> requestContextMap) {
		this.requestContextMap = requestContextMap;
	}

	public void run() {
		while (true) {
			try {
				long currentTime = System.currentTimeMillis();
				for (InvocationRequest request : requestContextMap.keySet()) {
					if (request.getTimeout() > 0 && request.getCreateMillisTime() > 0
							&& (request.getCreateMillisTime() + request.getTimeout()) < currentTime) {
						try {
							ProviderContext rc = requestContextMap.get(request);
							boolean cancelTimeout = defaultCancelTimeout;
							if (request.getMessageType() == Constants.MESSAGE_TYPE_HEART) {
								Future<?> future = rc.getFuture();
								if (future != null && !future.isCancelled()) {
									future.cancel(cancelTimeout);
								}
							} else {
								StringBuilder msg = new StringBuilder();
								msg.append("timeout while processing request, from:")
										.append(rc.getChannel() == null ? "" : rc.getChannel().getRemoteAddress())
										.append(", to:")
										.append(ExtensionLoader.getExtension(ConfigManager.class).getLocalIp())
										.append(", process time:").append(System.currentTimeMillis()).append("\r\n")
										.append("request:").append(InvocationUtils.toJsonString(request));
								ProcessTimeoutException te = null;
								Thread t = rc.getThread();
								if (t == null) {
									msg.append("\r\n the task has not been executed by threadPool");
									te = new ProcessTimeoutException(msg.toString());
								} else {
									te = new ProcessTimeoutException(msg.toString());
									te.setStackTrace(t.getStackTrace());
								}
								ContextUtils.setContext(request.getContext());
								logger.error(te.getMessage(), te);
								if (monitorLogger != null) {
									monitorLogger.logError(te);
								}
								Future<?> future = rc.getFuture();
								if (future != null && !future.isCancelled()) {
									if(future.cancel(cancelTimeout)) {
										TimelineManager.removeTimeline(request);
									}
								}
							}
						} finally {
							requestContextMap.remove(request);
						}
					}
				}
				if(++count % 10 == 0) {
					TimelineManager.removeLegacyTimelines();
				}
				Thread.sleep(timeoutInterval);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
	}
}
