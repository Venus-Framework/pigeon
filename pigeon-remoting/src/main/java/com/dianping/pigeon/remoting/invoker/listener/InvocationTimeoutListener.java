/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.invoker.listener;

import java.util.Map;

import org.apache.log4j.Logger;

import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.extension.ExtensionLoader;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.monitor.Monitor;
import com.dianping.pigeon.monitor.MonitorLogger;
import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.common.util.TimelineManager;
import com.dianping.pigeon.remoting.invoker.domain.Callback;
import com.dianping.pigeon.remoting.invoker.domain.RemoteInvocationBean;
import com.dianping.pigeon.remoting.invoker.route.statistics.ServiceStatisticsHolder;

public class InvocationTimeoutListener implements Runnable {

	private static final Logger logger = LoggerLoader.getLogger(InvocationTimeoutListener.class);
	private static final MonitorLogger monitorLogger = ExtensionLoader.getExtension(Monitor.class).getLogger();
	private Map<Long, RemoteInvocationBean> invocations;
	private long timeoutInterval = ExtensionLoader.getExtension(ConfigManager.class).getLongValue(
			Constants.KEY_TIMEOUT_INTERVAL, Constants.DEFAULT_TIMEOUT_INTERVAL);

	public InvocationTimeoutListener(Map<Long, RemoteInvocationBean> invocations) {
		this.invocations = invocations;
	}

	@Override
	public void run() {
		while (true) {
			try {
				long currentTime = System.currentTimeMillis();
				for (Long sequence : invocations.keySet()) {
					RemoteInvocationBean invocationBean = invocations.get(sequence);
					if (invocationBean != null) {
						InvocationRequest request = invocationBean.request;
						if (request.getTimeout() > 0 && request.getCreateMillisTime() > 0
								&& request.getCreateMillisTime() + request.getTimeout() < currentTime) {
							Callback callback = invocationBean.callback;
							if (callback != null && callback.getClient() != null) {
								ServiceStatisticsHolder.flowOut(request, callback.getClient().getAddress());
							}
							invocations.remove(sequence);
							StringBuilder msg = new StringBuilder();
							msg.append("remove timeout request, process time:").append(System.currentTimeMillis())
									.append("\r\n").append("request:").append(request);
							logger.error(msg.toString());
							// RequestTimeoutException e = new
							// RequestTimeoutException(msg.toString());
							// if (monitorLogger != null) {
							// monitorLogger.logError(e);
							// }
						}
					}
				}
				TimelineManager.removeLegacyTimelines();
				Thread.sleep(timeoutInterval);
			} catch (Exception e) {
				logger.error("checking remote call timeout failed", e);
			}
		}
	}
}
