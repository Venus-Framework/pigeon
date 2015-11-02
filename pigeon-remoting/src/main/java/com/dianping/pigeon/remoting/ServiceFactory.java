/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.Logger;

import com.dianping.dpsf.async.ServiceCallback;
import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.config.ConfigManagerLoader;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.registry.exception.RegistryException;
import com.dianping.pigeon.remoting.common.codec.SerializerFactory;
import com.dianping.pigeon.remoting.common.exception.RpcException;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.invoker.InvokerBootStrap;
import com.dianping.pigeon.remoting.invoker.ClientManager;
import com.dianping.pigeon.remoting.invoker.config.InvokerConfig;
import com.dianping.pigeon.remoting.invoker.route.balance.LoadBalanceManager;
import com.dianping.pigeon.remoting.provider.ProviderBootStrap;
import com.dianping.pigeon.remoting.provider.config.ProviderConfig;
import com.dianping.pigeon.remoting.provider.config.ServerConfig;
import com.dianping.pigeon.remoting.provider.listener.ServiceWarmupListener;
import com.dianping.pigeon.remoting.provider.service.ServiceProviderFactory;

/**
 * @author xiangwu
 * @Sep 30, 2013
 * 
 */
public class ServiceFactory {

	static Logger logger = LoggerLoader.getLogger(ServiceFactory.class);
	static ConfigManager configManager = ConfigManagerLoader.getConfigManager();
	static Map<InvokerConfig<?>, Object> services = new ConcurrentHashMap<InvokerConfig<?>, Object>();

	static {
		try {
			ProviderBootStrap.init();
		} catch (Throwable t) {
			t.printStackTrace();
			logger.error("error while initializing service factory:", t);
			System.exit(1);
		}
	}

	public static Map<InvokerConfig<?>, Object> getAllServiceInvokers() {
		return services;
	}

	public static Map<String, ProviderConfig<?>> getAllServiceProviders() {
		return ServiceProviderFactory.getAllServiceProviders();
	}

	public static <T> String getServiceUrl(Class<T> serviceInterface) {
		String url = serviceInterface.getCanonicalName();
		return url;
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

	public static <T> T getService(String url, Class<T> serviceInterface, ServiceCallback callback) throws RpcException {
		return getService(url, serviceInterface, callback, Constants.DEFAULT_INVOKER_TIMEOUT);
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
		if (!StringUtils.isBlank(invokerConfig.getProtocol())
				&& !invokerConfig.getProtocol().equalsIgnoreCase(Constants.PROTOCOL_DEFAULT)) {
			String protocolPrefix = "@" + invokerConfig.getProtocol().toUpperCase() + "@";
			if (!invokerConfig.getUrl().startsWith(protocolPrefix)) {
				invokerConfig.setUrl(protocolPrefix + invokerConfig.getUrl());
			}
		}
		Object service = null;
		service = services.get(invokerConfig);
		if (service == null) {
			try {
				InvokerBootStrap.startup();
				service = SerializerFactory.getSerializer(invokerConfig.getSerialize()).proxyRequest(invokerConfig);
				if (StringUtils.isNotBlank(invokerConfig.getLoadbalance())) {
					LoadBalanceManager.register(invokerConfig.getUrl(), invokerConfig.getGroup(),
							invokerConfig.getLoadbalance());
				}
			} catch (Throwable t) {
				throw new RpcException("error while trying to get service:" + invokerConfig, t);
			}
			try {
				ClientManager.getInstance().registerClients(invokerConfig.getUrl(), invokerConfig.getGroup(),
						invokerConfig.getVip());
			} catch (Throwable t) {
//				try {
//					ClientManager.getInstance().registerClients(invokerConfig.getUrl(),
//							invokerConfig.getGroup(), invokerConfig.getVip());
//				} catch (Throwable t2) {
//					logger.warn("error while trying to setup service client:" + invokerConfig, t2);
//				}
				logger.warn("error while trying to setup service client:" + invokerConfig, t);
			}
			services.put(invokerConfig, service);
		}
		return (T) service;
	}

	public static void startupServer(ServerConfig serverConfig) throws RpcException {
		// ProviderBootStrap.setServerConfig(serverConfig);
		// ProviderBootStrap.startup(serverConfig);
	}

	public static void shutdownServer() throws RpcException {
		ProviderBootStrap.shutdown();
	}

	/**
	 * add the service to pigeon and publish the service to registry
	 * 
	 * @param serviceInterface
	 * @param service
	 * @throws RpcException
	 */
	public static <T> void addService(Class<T> serviceInterface, T service) throws RpcException {
		addService(null, serviceInterface, service, ServerConfig.DEFAULT_PORT);
	}

	/**
	 * add the service to pigeon and publish the service to registry
	 * 
	 * @param url
	 * @param serviceInterface
	 * @param service
	 * @throws RpcException
	 */
	public static <T> void addService(String url, Class<T> serviceInterface, T service) throws RpcException {
		addService(url, serviceInterface, service, ServerConfig.DEFAULT_PORT);
	}

	/**
	 * add the service to pigeon and publish the service to registry
	 * 
	 * @param url
	 * @param serviceInterface
	 * @param service
	 * @param port
	 * @throws RpcException
	 */
	public static <T> void addService(String url, Class<T> serviceInterface, T service, int port) throws RpcException {
		ProviderConfig<T> providerConfig = new ProviderConfig<T>(serviceInterface, service);
		providerConfig.setUrl(url);
		providerConfig.getServerConfig().setPort(port);
		addService(providerConfig);
	}

	/**
	 * add the service to pigeon and publish the service to registry
	 * 
	 * @param providerConfig
	 * @throws RpcException
	 */
	public static <T> void addService(ProviderConfig<T> providerConfig) throws RpcException {
		if (StringUtils.isBlank(providerConfig.getUrl())) {
			providerConfig.setUrl(getServiceUrl(providerConfig));
		}
		try {
			ServiceProviderFactory.addService(providerConfig);
			ServerConfig serverConfig = ProviderBootStrap.startup(providerConfig);
			providerConfig.setServerConfig(serverConfig);
			ServiceProviderFactory.publishService(providerConfig, false);
		} catch (RegistryException t) {
			throw new RpcException("error while adding service:" + providerConfig, t);
		} catch (Throwable t) {
			throw new RpcException("error while adding service:" + providerConfig, t);
		}
	}

	/**
	 * add the services to pigeon and publish these services to registry
	 * 
	 * @param providerConfigList
	 * @throws RpcException
	 */
	public static void addServices(List<ProviderConfig<?>> providerConfigList) throws RpcException {
		if (logger.isInfoEnabled()) {
			logger.info("add services:" + providerConfigList);
		}
		if (providerConfigList != null && !providerConfigList.isEmpty()) {
			try {
				for (ProviderConfig<?> providerConfig : providerConfigList) {
					if (StringUtils.isBlank(providerConfig.getUrl())) {
						providerConfig.setUrl(getServiceUrl(providerConfig));
					}
					ServiceProviderFactory.addService(providerConfig);
					ServerConfig serverConfig = ProviderBootStrap.startup(providerConfig);
					providerConfig.setServerConfig(serverConfig);
					ServiceProviderFactory.publishService(providerConfig, false);
				}
			} catch (RegistryException t) {
				throw new RpcException("error while adding services:" + providerConfigList, t);
			} catch (Throwable t) {
				throw new RpcException("error while adding services:" + providerConfigList, t);
			}
		}
	}

	/**
	 * publish the service to registry
	 * 
	 * @param providerConfig
	 * @throws RpcException
	 */
	public static <T> void publishService(ProviderConfig<T> providerConfig) throws RpcException {
		if (StringUtils.isBlank(providerConfig.getUrl())) {
			providerConfig.setUrl(getServiceUrl(providerConfig));
		}
		try {
			ServiceProviderFactory.publishService(providerConfig, true);
		} catch (RegistryException t) {
			throw new RpcException("error while publishing service:" + providerConfig, t);
		}
	}

	/**
	 * publish the service to registry
	 * 
	 * @param url
	 * @throws RpcException
	 */
	public static <T> void publishService(String url) throws RpcException {
		try {
			ServiceProviderFactory.publishService(url);
		} catch (RegistryException t) {
			throw new RpcException("error while publishing service:" + url, t);
		}
	}

	/**
	 * unpublish the service from registry
	 * 
	 * @param providerConfig
	 * @throws RpcException
	 */
	public static <T> void unpublishService(ProviderConfig<T> providerConfig) throws RpcException {
		try {
			ServiceProviderFactory.unpublishService(providerConfig);
		} catch (RegistryException e) {
			throw new RpcException("error while unpublishing service:" + providerConfig, e);
		}
	}

	/**
	 * unpublish the service from registry
	 * 
	 * @param url
	 * @throws RpcException
	 */
	public static <T> void unpublishService(String url) throws RpcException {
		try {
			ServiceProviderFactory.unpublishService(url);
		} catch (RegistryException e) {
			throw new RpcException("error while unpublishing service:" + url, e);
		}
	}

	/**
	 * unpublish all pigeon services from registry
	 * 
	 * @throws RpcException
	 */
	public static void unpublishAllServices() throws RpcException {
		try {
			ServiceProviderFactory.unpublishAllServices();
		} catch (RegistryException e) {
			throw new RpcException("error while unpublishing all services", e);
		}
	}

	/**
	 * publish all pigeon services to registry
	 * 
	 * @throws RpcException
	 */
	public static void publishAllServices() throws RpcException {
		try {
			ServiceProviderFactory.publishAllServices();
		} catch (RegistryException e) {
			throw new RpcException("error while publishing all services", e);
		}
	}

	/**
	 * remove all pigeon services, including unregister these services from
	 * registry
	 * 
	 * @throws RpcException
	 */
	public static void removeAllServices() throws RpcException {
		try {
			ServiceProviderFactory.removeAllServices();
		} catch (RegistryException e) {
			throw new RpcException("error while removing all services", e);
		}
	}

	/**
	 * remove the service from pigeon, including unregister this service from
	 * registry
	 * 
	 * @param url
	 * @throws RpcException
	 */
	public static void removeService(String url) throws RpcException {
		try {
			ServiceProviderFactory.removeService(url);
		} catch (RegistryException e) {
			throw new RpcException("error while removing service:" + url, e);
		}
	}

	/**
	 * remove the service from pigeon, including unregister this service from
	 * registry
	 * 
	 * @param providerConfig
	 * @throws RpcException
	 */
	public static <T> void removeService(ProviderConfig<T> providerConfig) throws RpcException {
		removeService(providerConfig.getUrl());
	}

	public static ProviderConfig<?> getServiceConfig(String url) {
		return ServiceProviderFactory.getServiceConfig(url);
	}

	public static void setServerWeight(int weight) throws RegistryException {
		logger.info("set weight:" + weight);
		ServiceProviderFactory.setServerWeight(weight);
		
		if(weight == 0) {
			ServiceProviderFactory.notifyServiceUnpublished();
			return ;
		}
		
		if(weight > 0 && weight <= 100) {
			ServiceProviderFactory.notifyServicePublished();
			return ;
		}
	}

	public static void online() throws RegistryException {
		logger.info("online");
		ServiceProviderFactory.setServerWeight(Constants.WEIGHT_DEFAULT);
		ServiceProviderFactory.notifyServicePublished();
	}

	public static void offline() throws RegistryException {
		logger.info("offline");
		ServiceWarmupListener.stop();
		ServiceProviderFactory.setServerWeight(0);
		ServiceProviderFactory.notifyServiceUnpublished();
	}

	public static boolean isAutoPublish() {
		return ServiceProviderFactory.isAutoPublish();
	}
}
