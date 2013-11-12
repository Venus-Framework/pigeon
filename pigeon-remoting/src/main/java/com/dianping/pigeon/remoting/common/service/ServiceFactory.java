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
	static Map<InvokerConfig, Object> services = new ConcurrentHashMap<InvokerConfig, Object>();

	public static boolean isCacheService() {
		return isCacheService;
	}

	public static synchronized void setCacheService(boolean isCacheService) {
		ServiceFactory.isCacheService = isCacheService;
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

	public static <T> T getService(String serviceName, Class<T> serviceInterface) throws RpcException {
		InvokerConfig<T> invokerConfig = new InvokerConfig<T>(serviceName, serviceInterface);
		return getService(invokerConfig);
	}

	public static <T> T getService(String serviceName, Class<T> serviceInterface, int timeout) throws RpcException {
		InvokerConfig<T> invokerConfig = new InvokerConfig<T>(serviceName, serviceInterface);
		invokerConfig.setTimeout(timeout);
		return getService(invokerConfig);
	}

	public static <T> T getService(String serviceName, Class<T> serviceInterface, ServiceCallback callback, int timeout)
			throws RpcException {
		InvokerConfig<T> invokerConfig = new InvokerConfig<T>(serviceName, serviceInterface);
		invokerConfig.setTimeout(timeout);
		invokerConfig.setCallback(callback);
		return getService(invokerConfig);
	}

	public static <T> T getService(InvokerConfig<T> invokerConfig) throws RpcException {
		if (invokerConfig.getServiceInterface() == null) {
			throw new IllegalArgumentException("service interface is required");
		}
		String name = invokerConfig.getServiceName();
		if (StringUtils.isBlank(name)) {
			name = invokerConfig.getServiceInterface().getCanonicalName();
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
				ClientManager.getInstance().findAndRegisterClientFor(invokerConfig.getServiceName(),
						invokerConfig.getGroup(), invokerConfig.getVip());
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

	public static <T> void publishService(String serviceName, Class<T> serviceInterface, T service) throws RpcException {
		publishService(serviceName, serviceInterface, service, ServerFactory.DEFAULT_PORT);
	}

	public static <T> void publishService(String serviceName, Class<T> serviceInterface, T service, int port)
			throws RpcException {
		ProviderConfig<T> providerConfig = new ProviderConfig<T>();
		providerConfig.setPort(port);
		providerConfig.setServiceName(serviceName);
		providerConfig.setService(service);
		providerConfig.setServiceInterface(serviceInterface);
		publishService(providerConfig);
	}

	public static <T> void publishService(ProviderConfig<T> providerConfig) throws RpcException {
		if (StringUtils.isBlank(providerConfig.getServiceName())) {
			providerConfig.setServiceName(providerConfig.getServiceInterface().getCanonicalName());
		}
		try {
			ProviderBootStrapLoader.startup(providerConfig.getPort());
			ServiceProviderFactory.addService(providerConfig.getServiceName(), providerConfig.getService(),
					providerConfig.getPort());
		} catch (Throwable t) {
			throw new RpcException("error while publishing service:" + providerConfig, t);
		}
	}
}
