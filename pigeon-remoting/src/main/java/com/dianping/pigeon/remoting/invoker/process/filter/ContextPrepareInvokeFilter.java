/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.invoker.process.filter;

import java.io.Serializable;
import java.util.Map;

import org.apache.log4j.Logger;

import com.dianping.pigeon.extension.ExtensionLoader;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.monitor.MonitorLogger;
import com.dianping.pigeon.monitor.MonitorTransaction;
import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.common.process.ServiceInvocationHandler;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.invoker.Client;
import com.dianping.pigeon.remoting.invoker.config.InvokerConfig;
import com.dianping.pigeon.remoting.invoker.domain.InvokerContext;
import com.dianping.pigeon.util.ContextUtils;

public class ContextPrepareInvokeFilter extends InvocationInvokeFilter {

	private static final Logger logger = LoggerLoader.getLogger(ContextPrepareInvokeFilter.class);
	private MonitorLogger monitorLogger = ExtensionLoader.getExtension(MonitorLogger.class);

	@Override
	public InvocationResponse invoke(ServiceInvocationHandler handler, InvokerContext invocationContext)
			throws Throwable {
		if (logger.isDebugEnabled()) {
			logger.debug("invoke the ContextPrepareInvokeFilter, invocationContext:" + invocationContext);
		}
		initRequest(invocationContext);
		transferContextValueToRequest(invocationContext, invocationContext.getRequest());
		return handler.handle(invocationContext);
	}

	// 初始化Request的createTime和timeout，以便统一这两个值
	private void initRequest(InvokerContext invokerContext) {
		InvocationRequest request = invokerContext.getRequest();
		request.setCreateMillisTime(System.currentTimeMillis());
		request.setMessageType(Constants.MESSAGE_TYPE_SERVICE);
		InvokerConfig<?> invokerConfig = invokerContext.getInvokerConfig();
		if (invokerConfig != null) {
			request.setTimeout(invokerConfig.getTimeout());
			Object timeout = ContextUtils.getLocalContext(Constants.REQUEST_TIMEOUT);
			if (timeout != null) {
				int timeout_ = Integer.parseInt(String.valueOf(timeout));
				if (timeout_ > 0 && timeout_ < request.getTimeout()) {
					request.setTimeout(timeout_);
				}
			}
			MonitorTransaction transaction = monitorLogger.getCurrentTransaction();
			if (transaction != null) {
				transaction.addData("CurrentTimeout", request.getTimeout());
			}
			request.setAttachment(Constants.REQ_ATTACH_WRITE_BUFF_LIMIT, invokerConfig.isWriteBufferLimit());
			if (Constants.CALL_ONEWAY.equalsIgnoreCase(invokerConfig.getCallType())) {
				request.setCallType(Constants.CALLTYPE_NOREPLY);
			} else {
				request.setCallType(Constants.CALLTYPE_REPLY);
			}
		}
	}

	private void transferContextValueToRequest(final InvokerContext invocationContext, final InvocationRequest request) {
		InvokerConfig<?> metaData = invocationContext.getInvokerConfig();
		Client client = invocationContext.getClient();
		Object contextHolder = ContextUtils.createContext(metaData.getUrl(), invocationContext.getMethodName(),
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
