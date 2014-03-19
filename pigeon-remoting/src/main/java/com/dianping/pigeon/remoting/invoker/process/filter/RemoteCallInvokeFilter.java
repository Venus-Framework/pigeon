/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.invoker.process.filter;

import com.dianping.dpsf.async.ServiceFuture;
import com.dianping.dpsf.async.ServiceFutureFactory;
import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.common.process.ServiceInvocationHandler;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.common.util.ResponseUtils;
import com.dianping.pigeon.remoting.invoker.Client;
import com.dianping.pigeon.remoting.invoker.config.InvokerConfig;
import com.dianping.pigeon.remoting.invoker.domain.CallbackFuture;
import com.dianping.pigeon.remoting.invoker.domain.InvokerContext;
import com.dianping.pigeon.remoting.invoker.domain.ServiceCallbackWrapper;
import com.dianping.pigeon.remoting.invoker.domain.ServiceFutureImpl;
import com.dianping.pigeon.remoting.invoker.util.InvokerUtils;

/**
 * 执行实际的Remote Call，包括Sync, Future，Callback，Oneway
 * 
 * @author danson.liu
 */
public class RemoteCallInvokeFilter extends InvocationInvokeFilter {

	private static final InvocationResponse NO_RETURN_RESPONSE = ResponseUtils.createNoReturnResponse();

	@Override
	public InvocationResponse invoke(ServiceInvocationHandler handler, InvokerContext invokerContext) throws Throwable {
		Client client = invokerContext.getClient();
		InvocationRequest request = invokerContext.getRequest();
		InvokerConfig<?> invokerConfig = invokerContext.getInvokerConfig();
		String callMethod = invokerConfig.getCallType();
		beforeInvoke(request, client);
		InvocationResponse response = null;
		if (Constants.CALL_SYNC.equalsIgnoreCase(callMethod)) {
			CallbackFuture future = new CallbackFuture();
			response = InvokerUtils.sendRequest(client, invokerContext.getRequest(), future);
			if (response == null) {
				response = future.get(request.getTimeout());
			}
		} else if (Constants.CALL_CALLBACK.equalsIgnoreCase(callMethod)) {
			InvokerUtils.sendRequest(client, invokerContext.getRequest(),
					new ServiceCallbackWrapper(invokerConfig.getCallback()));
			response = NO_RETURN_RESPONSE;
		} else if (Constants.CALL_FUTURE.equalsIgnoreCase(callMethod)) {
			CallbackFuture future = new ServiceFutureImpl(request.getTimeout());
			InvokerUtils.sendRequest(client, invokerContext.getRequest(), future);
			ServiceFutureFactory.setFuture((ServiceFuture) future);
			response = NO_RETURN_RESPONSE;
		} else if (Constants.CALL_ONEWAY.equalsIgnoreCase(callMethod)) {
			InvokerUtils.sendRequest(client, invokerContext.getRequest(), null);
			response = NO_RETURN_RESPONSE;
		} else {
			throw new RuntimeException("Call method[" + callMethod + "] is not supported!");
		}
		afterInvoke(request, response, client);
		return response;
	}

}
