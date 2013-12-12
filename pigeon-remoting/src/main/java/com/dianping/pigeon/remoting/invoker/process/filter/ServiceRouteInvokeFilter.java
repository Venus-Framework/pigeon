/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.invoker.process.filter;

import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.common.process.ServiceInvocationHandler;
import com.dianping.pigeon.remoting.invoker.Client;
import com.dianping.pigeon.remoting.invoker.config.InvokerConfig;
import com.dianping.pigeon.remoting.invoker.domain.InvokerContext;

/**
 * 调用出错立即失败
 * 
 * @author danson.liu
 */
public class ServiceRouteInvokeFilter extends ClusterInvokeFilter {

	public static final String NAME = "service-route";

	@Override
	public InvocationResponse _invoke(ServiceInvocationHandler handler, InvokerContext invocationContext)
			throws Throwable {
		InvokerConfig invokerConfig = invocationContext.getInvokerConfig();
		InvocationRequest request = createRemoteCallRequest(invocationContext, invokerConfig);
		Client remoteClient = clientManager.getClient(invokerConfig, request, null);
		invocationContext.setClient(remoteClient);
		return handler.handle(invocationContext);
	}

	@Override
	public String name() {
		return NAME;
	}

}
