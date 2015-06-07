/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.provider.process.filter;

import java.io.Serializable;
import java.util.Map;

import org.apache.logging.log4j.Logger;

import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.common.process.ServiceInvocationFilter;
import com.dianping.pigeon.remoting.common.process.ServiceInvocationHandler;
import com.dianping.pigeon.remoting.provider.domain.ProviderContext;
import com.dianping.pigeon.util.ContextUtils;

public class ContextTransferProcessFilter implements ServiceInvocationFilter<ProviderContext> {

	private static final Logger logger = LoggerLoader.getLogger(ContextTransferProcessFilter.class);

	@Override
	public InvocationResponse invoke(ServiceInvocationHandler handler, ProviderContext invocationContext)
			throws Throwable {
		if (logger.isDebugEnabled()) {
			logger.debug("invoke the ContextTransferProcessFilter, invocationContext:" + invocationContext);
		}
		InvocationRequest request = invocationContext.getRequest();
		transferContextValueToProcessor(invocationContext, request);
		InvocationResponse response = null;
		try {
			response = handler.handle(invocationContext);
			return response;
		} finally {
			if (response != null) {
				try {
					transferContextValueToResponse(invocationContext, response);
				} catch (Throwable e) {
					logger.error(e.getMessage(), e);
				}
			}
		}
	}

	private void transferContextValueToProcessor(final ProviderContext processContext, final InvocationRequest request) {
		Object contextHolder = request.getContext();
		Map<String, Serializable> ctx = null;
		if (contextHolder != null) {
			ContextUtils.setContext(contextHolder);
			ctx = ContextUtils.getContextValues(contextHolder);
		}
		if (ctx != null) {
			for (Map.Entry<String, Serializable> entry : ctx.entrySet()) {
				processContext.putContextValue(entry.getKey(), entry.getValue());
			}
		}
		ContextUtils.putLocalContext("CLIENT_IP", processContext.getChannel().getRemoteAddress());
		ContextUtils.putLocalContext("CLIENT_APP", request.getApp());
	}

	private void transferContextValueToResponse(final ProviderContext processContext, final InvocationResponse response) {
		Object contextHolder = ContextUtils.getContext();
		Map<String, Serializable> contextValues = processContext.getContextValues();
		if (contextHolder == null) {
			// response.setContext(contextValues);
		} else {
			if (contextValues != null) {
				for (Map.Entry<String, Serializable> entry : contextValues.entrySet()) {
					ContextUtils.putContextValue(contextHolder, entry.getKey(), entry.getValue());
				}
			}
			response.setContext(contextHolder);
		}
	}

}
