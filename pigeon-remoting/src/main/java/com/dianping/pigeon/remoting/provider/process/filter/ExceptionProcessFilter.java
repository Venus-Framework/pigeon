/**
 * Dianping.com Inc.
 * Copyright (c) 2003-${year} All Rights Reserved.
 */
package com.dianping.pigeon.remoting.provider.process.filter;

import org.apache.log4j.Logger;

import com.dianping.pigeon.component.invocation.InvocationRequest;
import com.dianping.pigeon.component.invocation.InvocationResponse;
import com.dianping.pigeon.monitor.LoggerLoader;
import com.dianping.pigeon.remoting.common.process.ServiceInvocationFilter;
import com.dianping.pigeon.remoting.common.process.ServiceInvocationHandler;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.common.util.ResponseUtils;
import com.dianping.pigeon.remoting.provider.component.context.ProviderContext;

/**
 * 
 * 
 * @author jianhuihuang
 * @version $Id: ExceptionProcessFilter.java, v 0.1 2013-6-20 下午5:45:50
 *          jianhuihuang Exp $
 */
public class ExceptionProcessFilter implements ServiceInvocationFilter<ProviderContext> {

	private static final Logger logger = LoggerLoader.getLogger(ExceptionProcessFilter.class);

	@Override
	public InvocationResponse invoke(ServiceInvocationHandler handler, ProviderContext invocationContext)
			throws Throwable {
		if (logger.isDebugEnabled()) {
			logger.debug("invoke the ExceptionProcessFilter, invocationContext:" + invocationContext);
		}
		InvocationRequest request = invocationContext.getRequest();
		InvocationResponse response = null;
		try {
			response = handler.handle(invocationContext);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			if (request.getCallType() == Constants.CALLTYPE_REPLY
					&& request.getMessageType() != Constants.MESSAGE_TYPE_HEART) {
				response = ResponseUtils.createFailResponse(request, e);
			}
		}
		return response;
	}

}
