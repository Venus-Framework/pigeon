/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.invoker.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.dianping.pigeon.extension.ExtensionLoader;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.monitor.Monitor;
import com.dianping.pigeon.monitor.MonitorLogger;
import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.invoker.Client;
import com.dianping.pigeon.remoting.invoker.domain.Callback;
import com.dianping.pigeon.remoting.invoker.domain.RemoteInvocationBean;
import com.dianping.pigeon.remoting.invoker.exception.ResponseExpiredException;
import com.dianping.pigeon.remoting.invoker.listener.InvocationTimeoutListener;
import com.dianping.pigeon.remoting.invoker.util.RpcEventUtils;
import com.dianping.pigeon.threadpool.DefaultThreadPool;
import com.dianping.pigeon.threadpool.ThreadPool;

public class ServiceInvocationRepository {

	private static final Logger logger = LoggerLoader.getLogger(ServiceInvocationRepository.class);
	private static final MonitorLogger monitorLogger = ExtensionLoader.getExtension(Monitor.class).getLogger();
	private static Map<Long, RemoteInvocationBean> invocations = new ConcurrentHashMap<Long, RemoteInvocationBean>();
	private static ServiceInvocationRepository instance = new ServiceInvocationRepository();
	private static ThreadPool invocatinTimeCheckThreadPool = new DefaultThreadPool(
			"Pigeon-Client-Invoke-Timeout-Check-ThreadPool");

	public static ServiceInvocationRepository getInstance() {
		return instance;
	}

	public void put(long sequence, RemoteInvocationBean invocation) {
		invocations.put(sequence, invocation);
	}

	public void remove(long sequence) {
		invocations.remove(sequence);
	}

	public void receiveResponse(InvocationResponse response) {
		RemoteInvocationBean invocationBean = invocations.get(response.getSequence());
		if (invocationBean != null) {
			if (logger.isDebugEnabled()) {
				logger.debug("received response:" + response);
			}
			InvocationRequest request = invocationBean.request;
			try {
				Callback callback = invocationBean.callback;
				if (callback != null) {
					Client client = callback.getClient();
					if (client != null) {
						RpcEventUtils.clientReceiveResponse(request, client.getAddress());
					}
					callback.callback(response);
					callback.run();
				}
			} finally {
				invocations.remove(response.getSequence());
			}
		} else {
			String msg = "the response has expired:" + response;
			logger.error(msg);
			ResponseExpiredException e = new ResponseExpiredException(msg);
			monitorLogger.logError(e);
		}
	}

	public void init() {
		Runnable invocationTimeoutCheck = new InvocationTimeoutListener(invocations);
		invocatinTimeCheckThreadPool.execute(invocationTimeoutCheck);
	}

}
