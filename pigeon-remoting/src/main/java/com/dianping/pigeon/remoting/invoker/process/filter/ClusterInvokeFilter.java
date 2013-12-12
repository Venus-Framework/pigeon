/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.invoker.process.filter;

import java.util.concurrent.atomic.AtomicLong;

import org.apache.log4j.Logger;

import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.remoting.common.codec.SerializerFactory;
import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.common.process.ServiceInvocationHandler;
import com.dianping.pigeon.remoting.invoker.ClientManager;
import com.dianping.pigeon.remoting.invoker.config.InvokerConfig;
import com.dianping.pigeon.remoting.invoker.domain.InvokerContext;

public abstract class ClusterInvokeFilter extends InvocationInvokeFilter {

	private static final Logger logger = LoggerLoader.getLogger(ClusterInvokeFilter.class);

	protected static ClientManager clientManager = ClientManager.getInstance();

	private static AtomicLong requestSequenceMaker = new AtomicLong();
	public static final String CONTEXT_CLUSTER_ITEM = "context-cluster-item";

	public abstract String name();

	public InvocationResponse invoke(ServiceInvocationHandler handler, InvokerContext invocationContext)
			throws Throwable {
		try {
			return _invoke(handler, invocationContext);
		} catch (Exception e) {
			logger.error("Invoke remote call failed.", e);
			throw e;
		}
	}

	public abstract InvocationResponse _invoke(ServiceInvocationHandler handler, InvokerContext invocationContext)
			throws Throwable;

	protected InvocationRequest createRemoteCallRequest(InvokerContext invocationContext, InvokerConfig<?> invokerConfig) {
		InvocationRequest request = invocationContext.getRequest();
		if (request == null) {
			request = SerializerFactory.getSerializer(invokerConfig.getSerialize()).newRequest(invocationContext);
			invocationContext.setRequest(request);
		}
		request.setSequence(requestSequenceMaker.incrementAndGet() * -1);
		return request;
	}

}
