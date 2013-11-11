/**
 * Dianping.com Inc.
 * Copyright (c) 2003-${year} All Rights Reserved.
 */
package com.dianping.pigeon.remoting.provider.filter;

import org.apache.log4j.Logger;

import com.dianping.pigeon.component.invocation.InvocationResponse;
import com.dianping.pigeon.monitor.LoggerLoader;
import com.dianping.pigeon.remoting.common.filter.ServiceInvocationFilter;
import com.dianping.pigeon.remoting.common.filter.ServiceInvocationHandler;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.common.util.ResponseUtils;
import com.dianping.pigeon.remoting.provider.component.context.ProviderContext;

/**
 * 
 * 
 * @author jianhuihuang
 * @version $Id: EchoProcessFilter.java, v 0.1 2013-6-20 下午5:45:40 jianhuihuang
 *          Exp $
 */
public class EchoProcessFilter implements ServiceInvocationFilter<ProviderContext> {

	private static final Logger logger = LoggerLoader.getLogger(EchoProcessFilter.class);

	@Override
	public InvocationResponse invoke(ServiceInvocationHandler handler, ProviderContext invocationContext)
			throws Throwable {
		if (logger.isInfoEnabled()) {
			logger.info("invoke the EchoProcessFilter, invocationContext:" + invocationContext);
		}
		Object returnObject = "";
		if (invocationContext.getRequest().getParameters() != null
				|| invocationContext.getRequest().getParameters().length > 0) {
			returnObject = invocationContext.getRequest().getParameters()[0];
		}

		if (invocationContext.getRequest().getMessageType() == Constants.MESSAGE_TYPE_ECHO) {
			return ResponseUtils.createSuccessResponse(invocationContext.getRequest(), returnObject);
		}
		return handler.handle(invocationContext);
	}

}
