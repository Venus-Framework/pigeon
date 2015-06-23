/**
 * Dianping.com Inc.
 * Copyright (c) 2003-${year} All Rights Reserved.
 */
package com.dianping.pigeon.remoting.provider.process.filter;

import java.util.List;

import com.dianping.pigeon.log.LoggerLoader;
import org.apache.logging.log4j.Logger;

import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.common.process.ServiceInvocationFilter;
import com.dianping.pigeon.remoting.common.process.ServiceInvocationHandler;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.provider.domain.ProviderChannel;
import com.dianping.pigeon.remoting.provider.domain.ProviderContext;
import com.dianping.pigeon.remoting.provider.process.ProviderProcessInterceptor;
import com.dianping.pigeon.remoting.provider.process.ProviderProcessInterceptorFactory;

public class WriteResponseProcessFilter implements ServiceInvocationFilter<ProviderContext> {

	private static final Logger logger = LoggerLoader.getLogger(WriteResponseProcessFilter.class);

	@Override
	public InvocationResponse invoke(ServiceInvocationHandler handler, ProviderContext invocationContext)
			throws Throwable {
		if (logger.isDebugEnabled()) {
			logger.debug("invoke the WriteResponseProcessFilter, invocationContext:" + invocationContext);
		}
		try {
			ProviderChannel channel = invocationContext.getChannel();
			InvocationRequest request = invocationContext.getRequest();
			InvocationResponse response = handler.handle(invocationContext);
			if (request.getCallType() == Constants.CALLTYPE_REPLY) {
				channel.write(response);
			}
			if (request.getMessageType() == Constants.MESSAGE_TYPE_SERVICE) {
				List<ProviderProcessInterceptor> interceptors = ProviderProcessInterceptorFactory.getInterceptors();
				for (ProviderProcessInterceptor interceptor : interceptors) {
					interceptor.postInvoke(request, response);
				}
			}
			return response;
		} finally {
			// ContextUtils.clearContext();
			// ContextUtils.clearLocalContext();
		}
	}

}
