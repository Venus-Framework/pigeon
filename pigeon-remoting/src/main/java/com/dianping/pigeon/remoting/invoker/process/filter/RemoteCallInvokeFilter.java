/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.invoker.process.filter;

import java.util.Map;

import com.dianping.pigeon.log.Logger;

import com.dianping.dpsf.async.ServiceCallback;
import com.dianping.dpsf.async.ServiceFutureFactory;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.monitor.MonitorLoader;
import com.dianping.pigeon.monitor.MonitorTransaction;
import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.common.domain.InvocationContext.TimePhase;
import com.dianping.pigeon.remoting.common.domain.InvocationContext.TimePoint;
import com.dianping.pigeon.remoting.common.exception.InvalidParameterException;
import com.dianping.pigeon.remoting.common.process.ServiceInvocationHandler;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.invoker.Client;
import com.dianping.pigeon.remoting.invoker.callback.CallbackFuture;
import com.dianping.pigeon.remoting.invoker.callback.ServiceCallbackWrapper;
import com.dianping.pigeon.remoting.invoker.callback.ServiceFutureImpl;
import com.dianping.pigeon.remoting.invoker.config.InvokerConfig;
import com.dianping.pigeon.remoting.invoker.config.InvokerMethodConfig;
import com.dianping.pigeon.remoting.invoker.domain.DefaultInvokerContext;
import com.dianping.pigeon.remoting.invoker.domain.InvokerContext;
import com.dianping.pigeon.remoting.invoker.util.InvokerHelper;
import com.dianping.pigeon.remoting.invoker.util.InvokerUtils;
import com.dianping.pigeon.util.CollectionUtils;

/**
 * 执行实际的Remote Call，包括Sync, Future，Callback，Oneway
 * 
 * @author danson.liu
 */
public class RemoteCallInvokeFilter extends InvocationInvokeFilter {

	private static final Logger logger = LoggerLoader.getLogger(RemoteCallInvokeFilter.class);
	private static final InvocationResponse NO_RETURN_RESPONSE = InvokerUtils.createNoReturnResponse();

	@Override
	public InvocationResponse invoke(ServiceInvocationHandler handler, InvokerContext invocationContext)
			throws Throwable {
		invocationContext.getTimeline().add(new TimePoint(TimePhase.Q));
		Client client = invocationContext.getClient();
		InvocationRequest request = invocationContext.getRequest();
		InvokerConfig<?> invokerConfig = invocationContext.getInvokerConfig();
		String callType = invokerConfig.getCallType();
		beforeInvoke(invocationContext);
		boolean isCancel = InvokerHelper.getCancel();
		if (isCancel) {
			return InvokerUtils.createDefaultResponse(InvokerHelper.getDefaultResult());
		}
		InvocationResponse response = null;
		int timeout = request.getTimeout();
		Map<String, InvokerMethodConfig> methods = invokerConfig.getMethods();
		if (!CollectionUtils.isEmpty(methods)) {
			InvokerMethodConfig methodConfig = methods.get(request.getMethodName());
			if (methodConfig != null && methodConfig.getTimeout() > 0) {
				timeout = methodConfig.getTimeout();
			}
		}
		Integer timeoutThreadLocal = InvokerHelper.getTimeout();
		if (timeoutThreadLocal != null) {
			timeout = timeoutThreadLocal;
		}
		MonitorTransaction transaction = MonitorLoader.getMonitor().getCurrentCallTransaction();
		if (transaction != null) {
			transaction.addData("CurrentTimeout", timeout);
		}
		try {
			if (Constants.CALL_SYNC.equalsIgnoreCase(callType)) {
				CallbackFuture future = new CallbackFuture();
				response = InvokerUtils.sendRequest(client, invocationContext.getRequest(), future);
				invocationContext.getTimeline().add(new TimePoint(TimePhase.Q));
				if (response == null) {
					response = future.get(timeout);
				}
			} else if (Constants.CALL_CALLBACK.equalsIgnoreCase(callType)) {
				ServiceCallback callback = invokerConfig.getCallback();
				ServiceCallback tlCallback = InvokerHelper.getCallback();
				if (tlCallback != null) {
					callback = tlCallback;
					InvokerHelper.clearCallback();
				}
				InvokerUtils.sendRequest(client, invocationContext.getRequest(), new ServiceCallbackWrapper(
						invocationContext, callback));
				response = NO_RETURN_RESPONSE;
				invocationContext.getTimeline().add(new TimePoint(TimePhase.Q));
			} else if (Constants.CALL_FUTURE.equalsIgnoreCase(callType)) {
				ServiceFutureImpl future = new ServiceFutureImpl(invocationContext, timeout);
				InvokerUtils.sendRequest(client, invocationContext.getRequest(), future);
				ServiceFutureFactory.setFuture(future);
				response = InvokerUtils.createFutureResponse(future);
				invocationContext.getTimeline().add(new TimePoint(TimePhase.Q));
			} else if (Constants.CALL_ONEWAY.equalsIgnoreCase(callType)) {
				InvokerUtils.sendRequest(client, invocationContext.getRequest(), null);
				response = NO_RETURN_RESPONSE;
				invocationContext.getTimeline().add(new TimePoint(TimePhase.Q));
			} else {
				throw new InvalidParameterException("Call type[" + callType + "] is not supported!");
			}
			((DefaultInvokerContext) invocationContext).setResponse(response);
			afterInvoke(invocationContext);
		} catch (Throwable t) {
			afterThrowing(invocationContext, t);
			throw t;
		}
		return response;
	}

}
