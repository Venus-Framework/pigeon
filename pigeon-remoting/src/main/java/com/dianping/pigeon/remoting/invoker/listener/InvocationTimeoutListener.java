/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.invoker.listener;

import java.util.Map;

import com.dianping.pigeon.log.LoggerLoader;

import org.apache.logging.log4j.Logger;

import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.extension.ExtensionLoader;
import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.common.util.TimelineManager;
import com.dianping.pigeon.remoting.invoker.callback.Callback;
import com.dianping.pigeon.remoting.invoker.domain.RemoteInvocationBean;
import com.dianping.pigeon.remoting.invoker.route.statistics.ServiceStatisticsHolder;

public class InvocationTimeoutListener implements Runnable {

	private static final Logger logger = LoggerLoader.getLogger(InvocationTimeoutListener.class);
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
				Thread.sleep(timeoutInterval);
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
							callback.dispose();
							invocations.remove(sequence);
							StringBuilder msg = new StringBuilder();
							msg.append("remove timeout request, process time:").append(System.currentTimeMillis())
									.append("\r\n").append("request:").append(request);
							logger.warn(msg.toString());
							// RequestTimeoutException e = new
							// RequestTimeoutException(msg.toString());
							// if (monitorLogger != null) {
							// monitorLogger.logError(e);
							// }
						}
					}
				}
				TimelineManager.removeLegacyTimelines();
			} catch (Throwable e) {
				logger.warn("checking remote call timeout failed", e);
			}
		}
	}
}
