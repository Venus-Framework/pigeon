/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.invoker.process.filter;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.Logger;

import com.dianping.pigeon.config.ConfigManagerLoader;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.monitor.Monitor;
import com.dianping.pigeon.monitor.MonitorLoader;
import com.dianping.pigeon.monitor.MonitorTransaction;
import com.dianping.pigeon.registry.RegistryManager;
import com.dianping.pigeon.remoting.common.codec.SerializerFactory;
import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.common.process.ServiceInvocationHandler;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.invoker.Client;
import com.dianping.pigeon.remoting.invoker.config.InvokerConfig;
import com.dianping.pigeon.remoting.invoker.domain.InvokerContext;
import com.dianping.pigeon.util.ContextUtils;
import com.dianping.pigeon.util.VersionUtils;

public class ContextPrepareInvokeFilter extends InvocationInvokeFilter {

	private static final Logger logger = LoggerLoader.getLogger(ContextPrepareInvokeFilter.class);
	private Monitor monitor = MonitorLoader.getMonitor();
	private ConcurrentHashMap<String, Boolean> versionSupportedMap = new ConcurrentHashMap<String, Boolean>();

	@Override
	public InvocationResponse invoke(ServiceInvocationHandler handler, InvokerContext invocationContext)
			throws Throwable {
		if (logger.isDebugEnabled()) {
			logger.debug("invoke the ContextPrepareInvokeFilter, invocationContext:" + invocationContext);
		}
		initRequest(invocationContext);
		transferContextValueToRequest(invocationContext, invocationContext.getRequest());
		try {
			return handler.handle(invocationContext);
		} finally {
			ContextUtils.clearRequestContext();
		}
	}

	// 初始化Request的createTime和timeout，以便统一这两个值
	private void initRequest(InvokerContext invokerContext) {
		InvocationRequest request = invokerContext.getRequest();
		request.setCreateMillisTime(System.currentTimeMillis());
		request.setMessageType(Constants.MESSAGE_TYPE_SERVICE);

		checkSerializeSupported(invokerContext);

		InvokerConfig<?> invokerConfig = invokerContext.getInvokerConfig();
		if (invokerConfig != null) {
			request.setTimeout(invokerConfig.getTimeout());
			if (Constants.RESET_TIMEOUT) {
				Object timeout = ContextUtils.getLocalContext(Constants.REQUEST_TIMEOUT);
				if (timeout != null) {
					int timeout_ = Integer.parseInt(String.valueOf(timeout));
					if (timeout_ > 0 && timeout_ < request.getTimeout()) {
						request.setTimeout(timeout_);
					}
				}
			}
			MonitorTransaction transaction = monitor.getCurrentTransaction();
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

	private void checkSerializeSupported(InvokerContext invokerContext) {
		InvocationRequest request = invokerContext.getRequest();
		if (request.getSerialize() == SerializerFactory.SERIALIZE_PROTO
				|| request.getSerialize() == SerializerFactory.SERIALIZE_FST) {
			Client client = invokerContext.getClient();
			String version = RegistryManager.getInstance().getReferencedVersion(client.getAddress());
			boolean supported = true;
			if (StringUtils.isBlank(version)) {
				supported = false;
			} else if (versionSupportedMap.containsKey(version)) {
				supported = versionSupportedMap.get(version);
			} else {
				supported = VersionUtils.compareVersion(version, "2.4.3") >= 0;
				versionSupportedMap.putIfAbsent(version, supported);
			}
			if (!supported) {
				request.setSerialize(SerializerFactory.SERIALIZE_HESSIAN);
				invokerContext.getInvokerConfig().setSerialize(SerializerFactory.HESSIAN);
			}
		}
	}

	private void transferContextValueToRequest(final InvokerContext invocationContext, final InvocationRequest request) {
		InvokerConfig<?> invokerConfig = invocationContext.getInvokerConfig();
		Client client = invocationContext.getClient();
		Map<String, Serializable> contextValues = invocationContext.getContextValues();

		Object contextHolder = ContextUtils.createContext(invokerConfig.getUrl(), invocationContext.getMethodName(),
				client.getHost(), client.getPort());
		if (contextValues != null) {
			for (Map.Entry<String, Serializable> entry : contextValues.entrySet()) {
				ContextUtils.putContextValue(contextHolder, entry.getKey(), entry.getValue());
			}
		}
		request.setContext(contextHolder);

		if (ContextUtils.getGlobalContext(Constants.CONTEXT_KEY_SOURCE_APP) == null) {
			ContextUtils.putGlobalContext(Constants.CONTEXT_KEY_SOURCE_APP, ConfigManagerLoader.getConfigManager()
					.getAppName());
			ContextUtils.putGlobalContext(Constants.CONTEXT_KEY_SOURCE_IP, ConfigManagerLoader.getConfigManager()
					.getLocalIp());
		}
		request.setGlobalValues(ContextUtils.getGlobalContext());
		request.setRequestValues(ContextUtils.getRequestContext());
	}

}
