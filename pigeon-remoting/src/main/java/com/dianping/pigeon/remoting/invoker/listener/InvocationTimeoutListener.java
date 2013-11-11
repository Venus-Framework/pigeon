/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.invoker.listener;

import java.util.Map;

import org.apache.log4j.Logger;

import com.dianping.pigeon.component.invocation.InvocationRequest;
import com.dianping.pigeon.monitor.LoggerLoader;
import com.dianping.pigeon.remoting.invoker.component.RemoteInvocationBean;
import com.dianping.pigeon.remoting.invoker.component.async.Callback;
import com.dianping.pigeon.remoting.invoker.util.RpcEventUtils;

public class InvocationTimeoutListener implements Runnable {

	private static final Logger logger = LoggerLoader.getLogger(InvocationTimeoutListener.class);

	private Map<Long, RemoteInvocationBean> invocations;

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
						if (request.getCreateMillisTime() + request.getTimeout() < currentTime) {
							Callback callback = invocationBean.callback;
							if (callback != null && callback.getClient() != null) {
								RpcEventUtils.channelExceptionCaughtEvent(request, callback.getClient().getAddress());
							}
							invocations.remove(sequence);
							logger.warn("Remove timeout remote call: " + sequence);
						}
					}
				}
				Thread.sleep(1000);
			} catch (Exception e) {
				logger.error("Check remote call timeout failed.", e);
			}
		}
	}
}
