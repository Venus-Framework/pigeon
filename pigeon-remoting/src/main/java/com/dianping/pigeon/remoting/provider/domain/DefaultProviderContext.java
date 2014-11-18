/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.provider.domain;

import java.util.concurrent.Future;

import com.dianping.pigeon.remoting.common.domain.AbstractInvocationContext;
import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.provider.service.method.ServiceMethod;

public class DefaultProviderContext extends AbstractInvocationContext implements ProviderContext {

	private Throwable serviceError;
	private ProviderChannel channel;
	private Future<?> future;
	private Thread thread;
	private ServiceMethod serviceMethod;

	public DefaultProviderContext(InvocationRequest request, ProviderChannel channel) {
		super(request);
		this.channel = channel;
	}

	public Throwable getServiceError() {
		return serviceError;
	}

	public void setServiceError(Throwable serviceError) {
		this.serviceError = serviceError;
	}

	@Override
	public ProviderChannel getChannel() {
		return channel;
	}

	public void setFuture(Future<?> future) {
		this.future = future;
	}

	@Override
	public Future<?> getFuture() {
		return this.future;
	}

	@Override
	public Thread getThread() {
		return thread;
	}

	@Override
	public void setThread(Thread thread) {
		this.thread = thread;
	}

	@Override
	public void setServiceMethod(ServiceMethod serviceMethod) {
		this.serviceMethod = serviceMethod;
	}

	@Override
	public ServiceMethod getServiceMethod() {
		return serviceMethod;
	}

}
