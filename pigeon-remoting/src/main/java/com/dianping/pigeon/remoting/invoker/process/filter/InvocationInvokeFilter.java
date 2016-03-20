/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.invoker.process.filter;

import java.util.List;

import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.common.process.ServiceInvocationFilter;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.invoker.Client;
import com.dianping.pigeon.remoting.invoker.domain.InvokerContext;
import com.dianping.pigeon.remoting.invoker.process.InvokerProcessInterceptor;
import com.dianping.pigeon.remoting.invoker.process.InvokerProcessInterceptorFactory;

public abstract class InvocationInvokeFilter implements ServiceInvocationFilter<InvokerContext> {

	public void beforeInvoke(InvocationRequest request, Client client) {
		// TIMELINE_start
		if (request.getMessageType() == Constants.MESSAGE_TYPE_SERVICE) {
			List<InvokerProcessInterceptor> interceptors = InvokerProcessInterceptorFactory.getInterceptors();
			for (InvokerProcessInterceptor interceptor : interceptors) {
				interceptor.preInvoke(request);
			}
		}
	}

	public void afterInvoke(InvocationRequest request, InvocationResponse response, Client client) {
		if (request.getMessageType() == Constants.MESSAGE_TYPE_SERVICE) {
			List<InvokerProcessInterceptor> interceptors = InvokerProcessInterceptorFactory.getInterceptors();
			for (InvokerProcessInterceptor interceptor : interceptors) {
				interceptor.postInvoke(request, response);
			}
		}
		// TIMELINE_end
	}

}
