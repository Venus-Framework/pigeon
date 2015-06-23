/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.provider.service.method;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;
import com.dianping.pigeon.log.LoggerLoader;
import org.apache.logging.log4j.Logger;

import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.provider.config.ProviderConfig;
import com.dianping.pigeon.remoting.provider.exception.InvocationFailureException;
import com.dianping.pigeon.remoting.provider.process.filter.ContextTransferProcessFilter;
import com.dianping.pigeon.remoting.provider.service.ServiceProviderFactory;

public final class ServiceMethodFactory {

	private static final Logger logger = LoggerLoader.getLogger(ContextTransferProcessFilter.class);

	private static Map<String, ServiceMethodCache> methods = new ConcurrentHashMap<String, ServiceMethodCache>();

	private static Set<String> ingoreMethods = new HashSet<String>();

	static {
		Method[] objectMethodArray = Object.class.getMethods();
		for (Method method : objectMethodArray) {
			ingoreMethods.add(method.getName());
		}

		Method[] classMethodArray = Class.class.getMethods();
		for (Method method : classMethodArray) {
			ingoreMethods.add(method.getName());
		}
	}

	public static ServiceMethod getMethod(InvocationRequest request) throws InvocationFailureException {
		String serviceName = request.getServiceName();
		String methodName = request.getMethodName();
		if (StringUtils.isBlank(methodName)) {
			throw new IllegalArgumentException("method name is required");
		}
		String[] paramClassNames = request.getParamClassName();
		String version = request.getVersion();
		String newUrl = ServiceProviderFactory.getServiceUrlWithVersion(serviceName, version);
		if (logger.isDebugEnabled()) {
			logger.debug("get method for service url:" + request);
		}
		ServiceMethodCache serviceMethodCache = getServiceMethodCache(newUrl);
		if (serviceMethodCache == null) {
			if (logger.isDebugEnabled()) {
				logger.debug("no service found for version:" + version + ", use the default version of service:"
						+ serviceName);
			}
			serviceMethodCache = getServiceMethodCache(serviceName);
		}
		if (serviceMethodCache == null) {
			throw new InvocationFailureException("cannot find service for request:" + request);
		}
		return serviceMethodCache.getMethod(methodName, new ServiceParam(paramClassNames));
	}

	public static ServiceMethodCache getServiceMethodCache(String url) {
		ServiceMethodCache serviceMethodCache = methods.get(url);
		if (serviceMethodCache == null) {
			Map<String, ProviderConfig<?>> services = ServiceProviderFactory.getAllServiceProviders();
			ProviderConfig<?> providerConfig = services.get(url);
			if (providerConfig != null) {
				Object service = providerConfig.getService();
				Method[] methodArray = service.getClass().getMethods();
				serviceMethodCache = new ServiceMethodCache(url, service);
				for (Method method : methodArray) {
					if (!ingoreMethods.contains(method.getName())) {
						method.setAccessible(true);
						serviceMethodCache.addMethod(method.getName(), new ServiceMethod(service, method));
					}
				}
				methods.put(url, serviceMethodCache);
			}
		}
		return serviceMethodCache;
	}

	public static void init(String url) {
		getServiceMethodCache(url);
	}

	public static Map<String, ServiceMethodCache> getAllMethods() {
		return methods;
	}

}
