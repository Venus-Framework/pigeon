/**
s * Dianping.com Inc.
 * Copyright (c) 2003-${year} All Rights Reserved.
 */
package com.dianping.pigeon.remoting.provider.process.filter;

import java.util.List;

import org.apache.logging.log4j.Logger;

import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.remoting.common.domain.InvocationContext.TimePhase;
import com.dianping.pigeon.remoting.common.domain.InvocationContext.TimePoint;
import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.common.exception.InvalidParameterException;
import com.dianping.pigeon.remoting.common.process.ServiceInvocationFilter;
import com.dianping.pigeon.remoting.common.process.ServiceInvocationHandler;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.provider.domain.ProviderContext;
import com.dianping.pigeon.remoting.provider.exception.RequestAbortedException;
import com.dianping.pigeon.remoting.provider.process.ProviderProcessInterceptor;
import com.dianping.pigeon.remoting.provider.process.ProviderProcessInterceptorFactory;
import com.dianping.pigeon.remoting.provider.service.method.ServiceMethod;
import com.dianping.pigeon.remoting.provider.service.method.ServiceMethodFactory;
import com.dianping.pigeon.remoting.provider.util.ProviderHelper;
import com.dianping.pigeon.remoting.provider.util.ProviderUtils;
import com.dianping.pigeon.util.ContextUtils;

public class BusinessProcessFilter implements ServiceInvocationFilter<ProviderContext> {

	private static final Logger logger = LoggerLoader.getLogger(BusinessProcessFilter.class);

	@Override
	public InvocationResponse invoke(ServiceInvocationHandler handler, ProviderContext invocationContext)
			throws Throwable {
		if (logger.isDebugEnabled()) {
			logger.debug("invoke the BusinessProcessFilter, invocationContext:" + invocationContext);
		}
		InvocationRequest request = invocationContext.getRequest();
		if (request.getMessageType() == Constants.MESSAGE_TYPE_SERVICE) {
			if (Constants.RESET_TIMEOUT && request.getTimeout() > 0) {
				ContextUtils.putLocalContext(Constants.REQUEST_TIMEOUT, request.getTimeout());
			}
			if (Thread.currentThread().isInterrupted()) {
				StringBuilder msg = new StringBuilder();
				msg.append("the request has been canceled by timeout checking processor:").append(request);
				throw new RequestAbortedException(msg.toString());
			}
			List<ProviderProcessInterceptor> interceptors = ProviderProcessInterceptorFactory.getInterceptors();
			for (ProviderProcessInterceptor interceptor : interceptors) {
				interceptor.preInvoke(request);
			}
			InvocationResponse response = null;
			ServiceMethod method = invocationContext.getServiceMethod();
			if (method == null) {
				method = ServiceMethodFactory.getMethod(request);
			}
			if (Constants.REPLY_MANUAL && request.getCallType() == Constants.CALLTYPE_REPLY) {
				request.setCallType(Constants.CALLTYPE_MANUAL);
			}
			if (Constants.REPLY_MANUAL) {
				ProviderHelper.setContext(invocationContext);
			}
			invocationContext.getTimeline().add(new TimePoint(TimePhase.M, System.currentTimeMillis()));
			Object returnObj = null;
			try {
				returnObj = method.invoke(request.getParameters());
			} finally {
				ProviderHelper.clearContext();
			}

			invocationContext.getTimeline().add(new TimePoint(TimePhase.M, System.currentTimeMillis()));
			if (request.getCallType() == Constants.CALLTYPE_REPLY) {
				response = ProviderUtils.createSuccessResponse(request, returnObj);
			}
			return response;
		}
		throw new InvalidParameterException("message type[" + request.getMessageType() + "] is not supported!");
	}

}
