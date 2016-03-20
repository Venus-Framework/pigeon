/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.invoker.process.filter;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.Logger;

import com.dianping.pigeon.config.ConfigManagerLoader;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.registry.RegistryManager;
import com.dianping.pigeon.remoting.common.codec.SerializerFactory;
import com.dianping.pigeon.remoting.common.domain.CompactRequest;
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
	private ConcurrentHashMap<String, Boolean> serializeVersionMap = new ConcurrentHashMap<String, Boolean>();
	private ConcurrentHashMap<String, Boolean> compactVersionMap = new ConcurrentHashMap<String, Boolean>();
	private static AtomicLong requestSequenceMaker = new AtomicLong();
	private static final String KEY_COMPACT = "pigeon.invoker.request.compact";

	public ContextPrepareInvokeFilter() {
		ConfigManagerLoader.getConfigManager().getBooleanValue(KEY_COMPACT, true);
	}

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
		compactRequest(invokerContext);

		InvocationRequest request = invokerContext.getRequest();
		request.setSequence(requestSequenceMaker.incrementAndGet() * -1);
		request.setCreateMillisTime(System.currentTimeMillis());
		request.setMessageType(Constants.MESSAGE_TYPE_SERVICE);

		checkSerialize(invokerContext);

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
			if (Constants.CALL_ONEWAY.equalsIgnoreCase(invokerConfig.getCallType())) {
				request.setCallType(Constants.CALLTYPE_NOREPLY);
			} else {
				request.setCallType(Constants.CALLTYPE_REPLY);
			}
		}
	}

	private void checkSerialize(InvokerContext invokerContext) {
		InvocationRequest request = invokerContext.getRequest();
		if (request.getSerialize() == SerializerFactory.SERIALIZE_PROTO
				|| request.getSerialize() == SerializerFactory.SERIALIZE_FST) {
			Client client = invokerContext.getClient();
			String version = RegistryManager.getInstance().getReferencedVersionFromCache(client.getAddress());
			boolean supported = true;
			if (StringUtils.isBlank(version)) {
				supported = false;
			} else if (serializeVersionMap.containsKey(version)) {
				supported = serializeVersionMap.get(version);
			} else {
				supported = VersionUtils.compareVersion(version, "2.4.3") >= 0;
				serializeVersionMap.putIfAbsent(version, supported);
			}
			if (!supported) {
				request.setSerialize(SerializerFactory.SERIALIZE_HESSIAN);
				invokerContext.getInvokerConfig().setSerialize(SerializerFactory.HESSIAN);
			}
		}
	}

	private void compactRequest(InvokerContext invokerContext) {
		boolean isCompact = false;
		if (ConfigManagerLoader.getConfigManager().getBooleanValue(KEY_COMPACT, true)) {
			Client client = invokerContext.getClient();
			String version = RegistryManager.getInstance().getReferencedVersionFromCache(client.getAddress());
			if (StringUtils.isBlank(version)) {
				isCompact = false;
			} else if (compactVersionMap.containsKey(version)) {
				isCompact = compactVersionMap.get(version);
			} else {
				isCompact = VersionUtils.compareVersion(version, "2.7.5") >= 0;
				compactVersionMap.putIfAbsent(version, isCompact);
			}
		}
		if (isCompact) {
			invokerContext.setRequest(new CompactRequest(invokerContext));
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
