/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.provider.filter;

import java.io.Serializable;
import java.util.Map;

import org.apache.log4j.Logger;

import com.dianping.pigeon.component.invocation.InvocationRequest;
import com.dianping.pigeon.component.invocation.InvocationResponse;
import com.dianping.pigeon.monitor.LoggerLoader;
import com.dianping.pigeon.remoting.common.filter.ServiceInvocationFilter;
import com.dianping.pigeon.remoting.common.filter.ServiceInvocationHandler;
import com.dianping.pigeon.remoting.provider.component.context.ProviderContext;
import com.dianping.pigeon.util.ContextUtils;

/**
 * 
 * 
 * @author jianhuihuang
 * @version $Id: ContextTransferProcessFilter.java, v 0.1 2013-6-18 上午11:11:34
 *          jianhuihuang Exp $
 */
public class ContextTransferProcessFilter implements ServiceInvocationFilter<ProviderContext> {

	private static final Logger logger = LoggerLoader.getLogger(ContextTransferProcessFilter.class);

	@Override
	public InvocationResponse invoke(ServiceInvocationHandler handler, ProviderContext invocationContext)
			throws Throwable {
		if (logger.isInfoEnabled()) {
			logger.info("invoke the ContextTransferProcessFilter, invocationContext:" + invocationContext);
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
				} catch (Exception e) {
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
