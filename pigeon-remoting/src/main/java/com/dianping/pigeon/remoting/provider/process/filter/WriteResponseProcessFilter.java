/**
 * Dianping.com Inc.
 * Copyright (c) 2003-${year} All Rights Reserved.
 */
package com.dianping.pigeon.remoting.provider.process.filter;

import java.util.List;

import org.apache.logging.log4j.Logger;

import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.common.domain.InvocationContext.TimePhase;
import com.dianping.pigeon.remoting.common.domain.InvocationContext.TimePoint;
import com.dianping.pigeon.remoting.common.process.ServiceInvocationFilter;
import com.dianping.pigeon.remoting.common.process.ServiceInvocationHandler;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.provider.domain.ProviderChannel;
import com.dianping.pigeon.remoting.provider.domain.ProviderContext;
import com.dianping.pigeon.remoting.provider.process.ProviderInterceptor;
import com.dianping.pigeon.remoting.provider.process.ProviderInterceptorFactory;
import com.dianping.pigeon.remoting.provider.process.ProviderProcessInterceptor;
import com.dianping.pigeon.remoting.provider.process.ProviderProcessInterceptorFactory;

public class WriteResponseProcessFilter implements ServiceInvocationFilter<ProviderContext> {

	private static final Logger logger = LoggerLoader.getLogger(WriteResponseProcessFilter.class);

	@Override
	public InvocationResponse invoke(ServiceInvocationHandler handler, ProviderContext invocationContext)
			throws Throwable {
		try {
			ProviderChannel channel = invocationContext.getChannel();
			InvocationRequest request = invocationContext.getRequest();
			InvocationResponse response = handler.handle(invocationContext);
			if (request.getCallType() == Constants.CALLTYPE_REPLY) {
				invocationContext.getTimeline().add(new TimePoint(TimePhase.P));
				channel.write(response);
				invocationContext.getTimeline().add(new TimePoint(TimePhase.P));
			}
			if (request.getMessageType() == Constants.MESSAGE_TYPE_SERVICE) {
				List<ProviderProcessInterceptor> interceptors = ProviderProcessInterceptorFactory.getInterceptors();
				for (ProviderProcessInterceptor interceptor : interceptors) {
					interceptor.postInvoke(request, response);
				}
				List<ProviderInterceptor> contextInterceptors = ProviderInterceptorFactory.getInterceptors();
				for (ProviderInterceptor interceptor : contextInterceptors) {
					interceptor.postInvoke(invocationContext);
				}
			}
			return response;
		} finally {
			// ContextUtils.clearContext();
			// ContextUtils.clearLocalContext();
		}
	}

}
