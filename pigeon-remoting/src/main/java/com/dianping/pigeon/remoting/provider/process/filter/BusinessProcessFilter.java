/**
s * Dianping.com Inc.
 * Copyright (c) 2003-${year} All Rights Reserved.
 */
package com.dianping.pigeon.remoting.provider.process.filter;

import java.util.List;

import com.dianping.pigeon.log.LoggerLoader;
import org.apache.logging.log4j.Logger;

import com.dianping.dpsf.exception.NetTimeoutException;
import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.common.exception.InvalidParameterException;
import com.dianping.pigeon.remoting.common.process.ServiceInvocationFilter;
import com.dianping.pigeon.remoting.common.process.ServiceInvocationHandler;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.common.util.InvocationUtils;
import com.dianping.pigeon.remoting.common.util.TimelineManager;
import com.dianping.pigeon.remoting.common.util.TimelineManager.Phase;
import com.dianping.pigeon.remoting.provider.domain.ProviderContext;
import com.dianping.pigeon.remoting.provider.process.ProviderProcessInterceptor;
import com.dianping.pigeon.remoting.provider.process.ProviderProcessInterceptorFactory;
import com.dianping.pigeon.remoting.provider.service.method.ServiceMethod;
import com.dianping.pigeon.remoting.provider.service.method.ServiceMethodFactory;
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
				msg.append("the request has been canceled by timeout checking processor:").append(
						InvocationUtils.toJsonString(request));
				throw new NetTimeoutException(msg.toString());
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
			// TIMELINE_biz_start
			if (TimelineManager.isEnabled()) {
				TimelineManager.time(request, TimelineManager.getRemoteIp(), Phase.BusinessStart);
			}
			Object returnObj = method.invoke(request.getParameters());
			if (TimelineManager.isEnabled()) {
				TimelineManager.time(request, TimelineManager.getRemoteIp(), Phase.BusinessEnd);
			}
			// TIMELINE_biz_end
			if (request.getCallType() == Constants.CALLTYPE_REPLY) {
				response = ProviderUtils.createSuccessResponse(request, returnObj);
			}
			return response;
		}
		throw new InvalidParameterException("message type[" + request.getMessageType() + "] is not supported!");
	}

}
