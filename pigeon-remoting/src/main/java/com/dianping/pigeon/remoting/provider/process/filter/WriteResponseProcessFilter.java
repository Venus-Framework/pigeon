/**
 * Dianping.com Inc.
 * Copyright (c) 2003-${year} All Rights Reserved.
 */
package com.dianping.pigeon.remoting.provider.process.filter;

import org.apache.log4j.Logger;

import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.common.process.ServiceInvocationFilter;
import com.dianping.pigeon.remoting.common.process.ServiceInvocationHandler;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.provider.domain.ProviderChannel;
import com.dianping.pigeon.remoting.provider.domain.ProviderContext;
import com.dianping.pigeon.util.ContextUtils;

/**
 * 
 * 
 * @author jianhuihuang
 * @version $Id: WriteResponseProcessFilter.java, v 0.1 2013-6-20 下午5:46:19
 *          jianhuihuang Exp $
 */
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
				response.setResponseTime(System.currentTimeMillis());
				channel.write(response);
			}
			return response;
		} finally {
			ContextUtils.clearContext();
			ContextUtils.clearLocalContext();
		}
	}

}
