/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.common.service;

import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;

import com.dianping.dpsf.spring.ProxyBeanFactory;
import com.dianping.pigeon.remoting.common.exception.RpcException;
import com.dianping.pigeon.remoting.invoker.ClientManager;
import com.dianping.pigeon.remoting.invoker.component.InvokerConfig;
import com.dianping.pigeon.remoting.invoker.component.async.ServiceCallback;
import com.dianping.pigeon.remoting.invoker.loader.InvocationHandlerLoader;
import com.dianping.pigeon.remoting.invoker.loader.InvokerBootStrapLoader;
import com.dianping.pigeon.remoting.invoker.service.ServiceInvocationProxy;
import com.dianping.pigeon.remoting.provider.ServerFactory;
import com.dianping.pigeon.remoting.provider.component.ProviderConfig;
import com.dianping.pigeon.remoting.provider.loader.ProviderBootStrapLoader;
import com.dianping.pigeon.remoting.provider.service.ServiceProviderFactory;

/**
 * @author xiangwu
 * @Sep 30, 2013
 * 
 */
public class ServiceFactory {

	static boolean isCacheService = true;
	static Map<InvokerConfig<?>, Object> services = new ConcurrentHashMap<InvokerConfig<?>, Object>();

	public static boolean isCacheService() {
		return isCacheService;
	}

	public static synchronized void setCacheService(boolean isCacheService) {
		ServiceFactory.isCacheService = isCacheService;
	}

	public static <T> String getServiceUrl(InvokerConfig<T> invokerConfig) {
		String url = invokerConfig.getServiceInterface().getCanonicalName();
		return url;
	}

	public static <T> String getServiceUrl(ProviderConfig<T> providerConfig) {
		String url = providerConfig.getServiceInterface().getCanonicalName();
		return url;
	}

	public static <T> T getService(Class<T> serviceInterface) throws RpcException {
		return getService(null, serviceInterface);
	}

	public static <T> T getService(Class<T> serviceInterface, int timeout) throws RpcException {
		InvokerConfig<T> invokerConfig = new InvokerConfig<T>(serviceInterface);
		invokerConfig.setTimeout(timeout);
		return getService(invokerConfig);
	}

	public static <T> T getService(Class<T> serviceInterface, ServiceCallback callback) throws RpcException {
		InvokerConfig<T> invokerConfig = new InvokerConfig<T>(serviceInterface);
		invokerConfig.setCallback(callback);
		return getService(invokerConfig);
	}

	public static <T> T getService(Class<T> serviceInterface, ServiceCallback callback, int timeout)
			throws RpcException {
		InvokerConfig<T> invokerConfig = new InvokerConfig<T>(serviceInterface);
		invokerConfig.setCallback(callback);
		invokerConfig.setTimeout(timeout);
		return getService(invokerConfig);
	}

	public static <T> T getService(String url, Class<T> serviceInterface) throws RpcException {
		InvokerConfig<T> invokerConfig = new InvokerConfig<T>(url, serviceInterface);
		return getService(invokerConfig);
	}

	public static <T> T getService(String url, Class<T> serviceInterface, int timeout) throws RpcException {
		InvokerConfig<T> invokerConfig = new InvokerConfig<T>(url, serviceInterface);
		invokerConfig.setTimeout(timeout);
		return getService(invokerConfig);
	}

	public static <T> T getService(String url, Class<T> serviceInterface, ServiceCallback callback, int timeout)
			throws RpcException {
		InvokerConfig<T> invokerConfig = new InvokerConfig<T>(url, serviceInterface);
		invokerConfig.setTimeout(timeout);
		invokerConfig.setCallback(callback);
		return getService(invokerConfig);
	}

	public static <T> T getService(InvokerConfig<T> invokerConfig) throws RpcException {
		if (invokerConfig.getServiceInterface() == null) {
			throw new IllegalArgumentException("service interface is required");
		}
		if (StringUtils.isBlank(invokerConfig.getUrl())) {
			invokerConfig.setUrl(getServiceUrl(invokerConfig));
		}
		Object service = null;
		if (isCacheService) {
			service = services.get(invokerConfig);
		}
		if (service == null) {
			try {
				InvokerBootStrapLoader.startup();
				service = Proxy.newProxyInstance(
						ProxyBeanFactory.class.getClassLoader(),
						new Class[] { invokerConfig.getServiceInterface() },
						new ServiceInvocationProxy(invokerConfig, InvocationHandlerLoader
								.createInvokeHandler(invokerConfig)));
				ClientManager.getInstance().findAndRegisterClientFor(invokerConfig.getUrl(), invokerConfig.getGroup(),
						invokerConfig.getVip());
			} catch (Throwable t) {
				throw new RpcException("error while trying to get service:" + invokerConfig, t);
			}
			if (isCacheService) {
				services.put(invokerConfig, service);
			}
		}
		return (T) service;
	}

	public static <T> void publishService(Class<T> serviceInterface, T service) throws RpcException {
		publishService(null, serviceInterface, service, ServerFactory.DEFAULT_PORT);
	}

	public static <T> void publishService(String url, Class<T> serviceInterface, T service) throws RpcException {
		publishService(url, serviceInterface, service, ServerFactory.DEFAULT_PORT);
	}

	public static <T> void publishService(String url, Class<T> serviceInterface, T service, int port)
			throws RpcException {
		ProviderConfig<T> providerConfig = new ProviderConfig<T>(serviceInterface, service);
		providerConfig.setPort(port);
		providerConfig.setUrl(url);
		publishService(providerConfig);
	}

	public static <T> void publishService(ProviderConfig<T> providerConfig) throws RpcException {
		if (StringUtils.isBlank(providerConfig.getUrl())) {
			providerConfig.setUrl(getServiceUrl(providerConfig));
		}
		try {
			ProviderBootStrapLoader.startup(providerConfig.getPort());
			ServiceProviderFactory.addService(providerConfig);
		} catch (Throwable t) {
			throw new RpcException("error while publishing service:" + providerConfig, t);
		}
	}
}
