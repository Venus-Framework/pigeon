/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.invoker.process.filter;

import com.dianping.dpsf.exception.NetException;
import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.common.process.ServiceInvocationHandler;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.common.util.ResponseUtils;
import com.dianping.pigeon.remoting.invoker.Client;
import com.dianping.pigeon.remoting.invoker.config.InvokerConfig;
import com.dianping.pigeon.remoting.invoker.domain.Callback;
import com.dianping.pigeon.remoting.invoker.domain.CallbackFuture;
import com.dianping.pigeon.remoting.invoker.domain.InvokerContext;
import com.dianping.pigeon.remoting.invoker.domain.RemoteInvocationBean;
import com.dianping.pigeon.remoting.invoker.domain.ServiceCallbackWrapper;
import com.dianping.pigeon.remoting.invoker.domain.ServiceFutureImpl;
import com.dianping.pigeon.remoting.invoker.service.ServiceInvocationRepository;

/**
 * 执行实际的Remote Call，包括Sync, Future，Callback，Oneway
 * 
 * @author danson.liu
 */
public class RemoteCallInvokeFilter extends InvocationInvokeFilter {

	private static ServiceInvocationRepository invocationRepository = ServiceInvocationRepository.getInstance();
	private static final InvocationResponse NO_RETURN_RESPONSE = ResponseUtils.createNoReturnResponse();

	@Override
	public InvocationResponse invoke(ServiceInvocationHandler handler, InvokerContext invokerContext) throws Throwable {
		Client client = invokerContext.getClient();
		InvocationRequest request = invokerContext.getRequest();
		InvokerConfig<?> invokerConfig = invokerContext.getInvokerConfig();
		String callMethod = invokerConfig.getCallType();
		beforeInvoke(request, client.getAddress());
		InvocationResponse response = null;
		if (Constants.CALL_SYNC.equalsIgnoreCase(callMethod)) {
			CallbackFuture future = new CallbackFuture();
			response = sendRequest(client, invokerContext, future);
			if (response == null) {
				response = future.get(request.getTimeout());
			}
		} else if (Constants.CALL_CALLBACK.equalsIgnoreCase(callMethod)) {
			sendRequest(client, invokerContext, new ServiceCallbackWrapper(invokerConfig.getCallback()));
			response = NO_RETURN_RESPONSE;
		} else if (Constants.CALL_FUTURE.equalsIgnoreCase(callMethod)) {
			CallbackFuture future = new ServiceFutureImpl(request.getTimeout());
			sendRequest(client, invokerContext, future);
			invokerContext.putTransientContextValue(Constants.CONTEXT_FUTURE, future);
			response = NO_RETURN_RESPONSE;
		} else if (Constants.CALL_ONEWAY.equalsIgnoreCase(callMethod)) {
			sendRequest(client, invokerContext, null);
			response = NO_RETURN_RESPONSE;
		} else {
			throw new RuntimeException("Call method[" + callMethod + "] is not supported!");
		}
		afterInvoke(request, client);
		return response;
	}

	private InvocationResponse sendRequest(Client client, InvokerContext invokerContext, Callback callback) {
		InvocationRequest request = invokerContext.getRequest();
		if (request.getCallType() == Constants.CALLTYPE_REPLY) {
			RemoteInvocationBean invocationBean = new RemoteInvocationBean();
			invocationBean.request = request;
			invocationBean.callback = callback;
			callback.setRequest(request);
			callback.setClient(client);
			invocationRepository.put(request.getSequence(), invocationBean);
		}
		InvocationResponse response = null;
		try {
			response = client.write(invokerContext, callback);
		} catch (RuntimeException e) {
			invocationRepository.remove(request.getSequence());
			throw new NetException("remote call failed:" + invokerContext, e);
		} finally {
			if (response != null) {
				invocationRepository.remove(request.getSequence());
			}
		}
		return response;
	}

}
