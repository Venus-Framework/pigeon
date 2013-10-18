/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.invoker.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.dianping.dpsf.component.DPSFRequest;
import com.dianping.dpsf.component.DPSFResponse;
import com.dianping.pigeon.remoting.invoker.Client;
import com.dianping.pigeon.remoting.invoker.component.RemoteInvocationBean;
import com.dianping.pigeon.remoting.invoker.component.async.Callback;
import com.dianping.pigeon.remoting.invoker.listener.InvocationTimeoutListener;
import com.dianping.pigeon.remoting.invoker.util.RpcEventUtils;
import com.dianping.pigeon.threadpool.DefaultThreadPool;
import com.dianping.pigeon.threadpool.ThreadPool;

public class ServiceInvocationRepository {

	private Map<Long, RemoteInvocationBean> invocations = new ConcurrentHashMap<Long, RemoteInvocationBean>();

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

	public void receiveResponse(DPSFResponse response) {

		RemoteInvocationBean invocationBean = invocations.get(response.getSequence());
		if (invocationBean != null) {
			DPSFRequest request = invocationBean.request;
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
		}
	}

	public void init() {
		Runnable invocationTimeoutCheck = new InvocationTimeoutListener(invocations);
		invocatinTimeCheckThreadPool.execute(invocationTimeoutCheck);
	}

}
