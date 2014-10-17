/**
 * Dianping.com Inc.
 * Copyright (c) 2003-${year} All Rights Reserved.
 */
package com.dianping.pigeon.remoting.provider.process.filter;

import java.util.List;

import org.apache.log4j.Logger;

import com.dianping.pigeon.log.LoggerLoader;
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
//				long currentTime = System.currentTimeMillis();
//				if (request.getTimeout() > 0 && request.getCreateMillisTime() > 0
//						&& request.getCreateMillisTime() + request.getTimeout() < currentTime) {
//					StringBuilder msg = new StringBuilder();
//					msg.append("request timeout,\r\nrequest:").append(InvocationUtils.toJsonString(request))
//							.append("\r\nresponse:").append(InvocationUtils.toJsonString(response));
//					ProcessTimeoutException te = new ProcessTimeoutException(msg.toString());
//					logger.error(te.getMessage(), te);
//					if (monitorLogger != null) {
//						monitorLogger.logError(te);
//					}
//				}
			}
			if (request.getMessageType() == Constants.MESSAGE_TYPE_SERVICE) {
				List<ProviderProcessInterceptor> interceptors = ProviderProcessInterceptorFactory.getInterceptors();
				for (ProviderProcessInterceptor interceptor : interceptors) {
					interceptor.postInvoke(request, response);
				}
			}
			return response;
		} finally {
//			ContextUtils.clearContext();
//			ContextUtils.clearLocalContext();
		}
	}

}
