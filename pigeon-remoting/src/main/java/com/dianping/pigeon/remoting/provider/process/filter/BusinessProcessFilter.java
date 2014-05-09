/**
s * Dianping.com Inc.
 * Copyright (c) 2003-${year} All Rights Reserved.
 */
package com.dianping.pigeon.remoting.provider.process.filter;

import java.util.List;

import org.apache.log4j.Logger;

import com.dianping.dpsf.exception.DPSFException;
import com.dianping.dpsf.exception.NetTimeoutException;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.common.process.ServiceInvocationFilter;
import com.dianping.pigeon.remoting.common.process.ServiceInvocationHandler;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.provider.domain.ProviderContext;
import com.dianping.pigeon.remoting.provider.process.ProviderProcessInterceptor;
import com.dianping.pigeon.remoting.provider.process.ProviderProcessInterceptorFactory;
import com.dianping.pigeon.remoting.provider.service.method.ServiceMethod;
import com.dianping.pigeon.remoting.provider.service.method.ServiceMethodFactory;
import com.dianping.pigeon.remoting.provider.util.ProviderUtils;
import com.dianping.pigeon.util.ContextUtils;

/**
 * 
 * 
 * @author jianhuihuang
 * @version $Id: BusinessProcessFilter.java, v 0.1 2013-6-30 下午8:33:49
 *          jianhuihuang Exp $
 */
public class BusinessProcessFilter implements ServiceInvocationFilter<ProviderContext> {

	private static final Logger logger = LoggerLoader.getLogger(BusinessProcessFilter.class);

	@Override
	public InvocationResponse invoke(ServiceInvocationHandler handler, ProviderContext invocationContext)
			throws Throwable {
		InvocationRequest request = invocationContext.getRequest();
		if (request.getMessageType() == Constants.MESSAGE_TYPE_SERVICE) {
			if (request.getTimeout() > 0) {
				ContextUtils.putLocalContext(Constants.REQUEST_TIMEOUT, request.getTimeout());
			}
			if (Thread.currentThread().isInterrupted()) {
				StringBuilder msg = new StringBuilder();
				msg.append("the request has been canceled by timeout checking processor:").append(request);
				throw new NetTimeoutException(msg.toString());
			}
			List<ProviderProcessInterceptor> interceptors = ProviderProcessInterceptorFactory.getInterceptors();
			for (ProviderProcessInterceptor interceptor : interceptors) {
				interceptor.preInvoke(request);
			}
			InvocationResponse response = null;
			ServiceMethod method = ServiceMethodFactory.getMethod(request);
			Object returnObj = method.invoke(request.getParameters());
			if (request.getCallType() == Constants.CALLTYPE_REPLY) {
				response = ProviderUtils.createSuccessResponse(request, returnObj);
			}
			return response;
		}
		throw new DPSFException("message type[" + request.getMessageType() + "] is not supported!");
	}

}
