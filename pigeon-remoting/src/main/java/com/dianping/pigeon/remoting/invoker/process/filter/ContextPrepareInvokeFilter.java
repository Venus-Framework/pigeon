/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.invoker.process.filter;

import java.io.Serializable;
import java.util.Map;

import org.apache.log4j.Logger;

import com.dianping.pigeon.component.invocation.InvocationRequest;
import com.dianping.pigeon.component.invocation.InvocationResponse;
import com.dianping.pigeon.monitor.LoggerLoader;
import com.dianping.pigeon.remoting.common.process.ServiceInvocationHandler;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.invoker.Client;
import com.dianping.pigeon.remoting.invoker.component.context.InvokerContext;
import com.dianping.pigeon.remoting.invoker.config.InvokerConfig;
import com.dianping.pigeon.util.ContextUtils;

public class ContextPrepareInvokeFilter extends InvocationInvokeFilter {

	private static final Logger logger = LoggerLoader.getLogger(ContextPrepareInvokeFilter.class);

	@Override
	public InvocationResponse invoke(ServiceInvocationHandler handler, InvokerContext invocationContext)
			throws Throwable {
		initRequest(invocationContext.getRequest());
		transferContextValueToRequest(invocationContext, invocationContext.getRequest());
		return handler.handle(invocationContext);
	}

	// 初始化Request的createTime和timeout，以便统一这两个值
	private void initRequest(InvocationRequest request) {
		request.setCreateMillisTime(System.currentTimeMillis());
		Object timeout = ContextUtils.getLocalContext(Constants.REQUEST_TIMEOUT);
		if (timeout != null) {
			int timeout_ = Integer.parseInt(String.valueOf(timeout));
			if (timeout_ < request.getTimeout()) {
				request.setTimeout(timeout_);
			}
		}

		// Object createTime =
		// ContextUtils.getLocalContext(Constants.REQUEST_CREATE_TIME);
		// Object timeout =
		// ContextUtils.getLocalContext(Constants.REQUEST_TIMEOUT);
		// if (createTime != null) {
		// long createTime_ = Long.parseLong(String.valueOf(createTime));
		// int timeout_ = Integer.parseInt(String.valueOf(timeout));
		// Object firstFlag =
		// ContextUtils.getLocalContext(Constants.REQUEST_FIRST_FLAG);
		// if (firstFlag == null) {
		// ContextUtils.putLocalContext(Constants.REQUEST_FIRST_FLAG, true);
		// request.setCreateMillisTime(createTime_);
		// } else {
		// long now = System.currentTimeMillis();
		// timeout_ = timeout_ - (int) (now - createTime_);
		// if (timeout_ <= 0) {
		// throw new
		// NetTimeoutException("method has been timeout for first call (startTime:"
		// + new Date(createTime_) + " timeout:" + timeout_ + ")");
		// }
		// request.setCreateMillisTime(now);
		// }
		// if (timeout_ < request.getTimeout()) {
		// request.setTimeout(timeout_);
		// }
		// } else {
		// request.setCreateMillisTime(System.currentTimeMillis());
		// }
	}

	private void transferContextValueToRequest(final InvokerContext invocationContext, final InvocationRequest request) {
		InvokerConfig metaData = invocationContext.getInvokerConfig();
		Client client = invocationContext.getClient();
		Object contextHolder = ContextUtils.createContext(metaData.getUrl(), invocationContext.getMethod().getName(),
				client.getHost(), client.getPort());
		if (contextHolder != null) {
			Map<String, Serializable> contextValues = invocationContext.getContextValues();
			if (contextValues != null) {
				for (Map.Entry<String, Serializable> entry : contextValues.entrySet()) {
					ContextUtils.putContextValue(contextHolder, entry.getKey(), entry.getValue());
				}
			}
		}
		request.setContext(contextHolder);
	}

}
