/**
 * Dianping.com Inc.
 * Copyright (c) 00-0 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.provider.process;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.provider.domain.ProviderContext;
import com.dianping.pigeon.remoting.provider.listener.RequestTimeoutListener;
import com.dianping.pigeon.threadpool.DefaultThreadPool;
import com.dianping.pigeon.threadpool.ThreadPool;

public abstract class AbstractRequestProcessor implements RequestProcessor {

	private static ThreadPool timeCheckThreadPool = new DefaultThreadPool("pigeon-provider-timeout-checker");

	protected static Map<InvocationRequest, ProviderContext> requestContextMap = new ConcurrentHashMap<InvocationRequest, ProviderContext>();

	static {
		timeCheckThreadPool.execute(new RequestTimeoutListener(requestContextMap));
	}

	public abstract Future<InvocationResponse> doProcessRequest(final InvocationRequest request,
			final ProviderContext providerContext);

	public abstract void doStop();

	public void stop() {
		timeCheckThreadPool.getExecutor().shutdown();
		doStop();
	}

	public Future<InvocationResponse> processRequest(final InvocationRequest request,
			final ProviderContext providerContext) {
		if (request.getCreateMillisTime() == 0) {
			request.setCreateMillisTime(System.currentTimeMillis());
		}
		Future<InvocationResponse> invocationResponse = doProcessRequest(request, providerContext);
		providerContext.setFuture(invocationResponse);
		return invocationResponse;
	}

}
