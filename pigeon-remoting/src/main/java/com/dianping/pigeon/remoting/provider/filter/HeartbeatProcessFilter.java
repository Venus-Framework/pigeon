/**
 * Dianping.com Inc.
 * Copyright (c) 2003-${year} All Rights Reserved.
 */
package com.dianping.pigeon.remoting.provider.filter;

import org.apache.log4j.Logger;

import com.dianping.dpsf.component.DPSFResponse;
import com.dianping.pigeon.remoting.common.filter.ServiceInvocationFilter;
import com.dianping.pigeon.remoting.common.filter.ServiceInvocationHandler;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.common.util.ResponseUtils;
import com.dianping.pigeon.remoting.provider.component.context.ProviderContext;

/**
 * 
 * 
 * @author jianhuihuang
 * @version $Id: HeartbeatProcessFilter.java, v 0.1 2013-6-20 下午5:45:57
 *          jianhuihuang Exp $
 */
public class HeartbeatProcessFilter implements ServiceInvocationFilter<ProviderContext> {

	private static final Logger logger = Logger.getLogger(HeartbeatProcessFilter.class);

	@Override
	public DPSFResponse invoke(ServiceInvocationHandler handler, ProviderContext invocationContext)
			throws Throwable {
		if (logger.isInfoEnabled()) {
			logger.info("invoke the HeartbeatProcessFilter, invocationContext:" + invocationContext);
		}
		if (invocationContext.getRequest().getMessageType() == Constants.MESSAGE_TYPE_HEART) {
			return ResponseUtils.createHeartResponse(invocationContext.getRequest());
		}
		return handler.handle(invocationContext);
	}

}
