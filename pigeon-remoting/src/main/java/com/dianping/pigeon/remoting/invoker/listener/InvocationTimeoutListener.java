/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.invoker.listener;

import java.util.Map;

import com.dianping.pigeon.log.Logger;

import com.dianping.pigeon.config.ConfigManagerLoader;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.invoker.callback.Callback;
import com.dianping.pigeon.remoting.invoker.domain.RemoteInvocationBean;
import com.dianping.pigeon.remoting.invoker.route.statistics.ServiceStatisticsHolder;

public class InvocationTimeoutListener implements Runnable {

	private static final Logger logger = LoggerLoader.getLogger(InvocationTimeoutListener.class);
	private Map<Long, RemoteInvocationBean> invocations;
	private long timeoutInterval = ConfigManagerLoader.getConfigManager().getLongValue(
			"pigeon.invoker.timeout.interval", 1000);

	public InvocationTimeoutListener(Map<Long, RemoteInvocationBean> invocations) {
		this.invocations = invocations;
	}

	@Override
	public void run() {
		int timeoutCountInLastSecond = 0;
		int timeoutCountInCurrentSecond = 0;
		while (true) {
			timeoutCountInLastSecond = timeoutCountInCurrentSecond;
			timeoutCountInCurrentSecond = 0;
			try {
				Thread.sleep(timeoutInterval);
				long currentTime = System.currentTimeMillis();
				for (Long sequence : invocations.keySet()) {
					RemoteInvocationBean invocationBean = invocations.get(sequence);
					if (invocationBean != null) {
						InvocationRequest request = invocationBean.request;
						if (request.getTimeout() > 0 && request.getCreateMillisTime() > 0
								&& request.getCreateMillisTime() + request.getTimeout() < currentTime) {
							timeoutCountInCurrentSecond++;
							Callback callback = invocationBean.callback;
							if (callback != null && callback.getClient() != null) {
								ServiceStatisticsHolder.flowOut(request, callback.getClient().getAddress());
							}
							callback.dispose();
							invocations.remove(sequence);
							boolean isLog = true;
							if (timeoutCountInLastSecond > ConfigManagerLoader.getConfigManager().getIntValue(
									"pigeon.log.threshold", 10)
									&& timeoutCountInCurrentSecond
											% ConfigManagerLoader.getConfigManager().getIntValue("pigeon.log.interval",
													10) != 1) {
								isLog = false;
							}
							if (isLog) {
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
				}
			} catch (Throwable e) {
				logger.warn("checking remote call timeout failed", e);
			}
		}
	}
}
