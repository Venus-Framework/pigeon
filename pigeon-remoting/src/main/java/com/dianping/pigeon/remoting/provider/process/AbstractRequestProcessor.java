/**
 * Dianping.com Inc.
 * Copyright (c) 00-0 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.provider.process;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.provider.domain.ProviderContext;
import com.dianping.pigeon.remoting.provider.listener.RequestTimeoutListener;
import com.dianping.pigeon.threadpool.DefaultThreadPool;
import com.dianping.pigeon.threadpool.ThreadPool;

public abstract class AbstractRequestProcessor implements RequestProcessor {

	private static ThreadPool timeCheckThreadPool = new DefaultThreadPool("pigeon-provider-timeout-checker");

	protected Map<InvocationRequest, ProviderContext> requestContextMap;
	
	public abstract Future<?> doProcessRequest(final InvocationRequest request, final ProviderContext providerContext);

	public abstract void doStop();
	
	public AbstractRequestProcessor() {
		this.requestContextMap = new ConcurrentHashMap<InvocationRequest, ProviderContext>();
		timeCheckThreadPool.execute(new RequestTimeoutListener(requestContextMap));
	}
	
	public void stop() {
		timeCheckThreadPool.getExecutor().shutdown();
		doStop();
	}
	
	public void processRequest(final InvocationRequest request, final ProviderContext providerContext) {
		if(request.getRequestTime() == 0) {
			request.setPequestTime(System.currentTimeMillis());
		}
		providerContext.setFuture(doProcessRequest(request, providerContext));
	}

}
