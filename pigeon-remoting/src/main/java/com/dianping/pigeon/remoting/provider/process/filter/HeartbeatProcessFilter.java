/**
 * Dianping.com Inc.
 * Copyright (c) 2003-${year} All Rights Reserved.
 */
package com.dianping.pigeon.remoting.provider.process.filter;

import com.dianping.pigeon.log.Logger;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.common.process.ServiceInvocationFilter;
import com.dianping.pigeon.remoting.common.process.ServiceInvocationHandler;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.provider.domain.ProviderContext;
import com.dianping.pigeon.remoting.provider.util.ProviderUtils;

public class HeartbeatProcessFilter implements ServiceInvocationFilter<ProviderContext> {

	private static final Logger logger = LoggerLoader.getLogger(HeartbeatProcessFilter.class);

	@Override
	public InvocationResponse invoke(ServiceInvocationHandler handler, ProviderContext invocationContext)
			throws Throwable {
		if (invocationContext.getRequest().getMessageType() == Constants.MESSAGE_TYPE_HEART) {
			return ProviderUtils.createHeartResponse(invocationContext.getRequest());
		}
		return handler.handle(invocationContext);
	}

}
