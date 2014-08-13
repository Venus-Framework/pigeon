/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.provider.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.dianping.pigeon.config.ConfigConstants;
import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.config.ConfigManagerLoader;
import com.dianping.pigeon.extension.ExtensionLoader;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.registry.RegistryManager;
import com.dianping.pigeon.registry.exception.RegistryException;
import com.dianping.pigeon.remoting.common.status.Phase;
import com.dianping.pigeon.remoting.common.status.StatusContainer;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.provider.ProviderBootStrap;
import com.dianping.pigeon.remoting.provider.Server;
import com.dianping.pigeon.remoting.provider.config.ProviderConfig;
import com.dianping.pigeon.remoting.provider.listener.ServiceChangeListener;
import com.dianping.pigeon.remoting.provider.listener.ServiceWarmupListener;
import com.dianping.pigeon.remoting.provider.service.method.ServiceMethodFactory;
import com.dianping.pigeon.util.VersionUtils;

/**
 * @author xiangwu
 * @Sep 30, 2013
 * 
 */
public final class ServiceProviderFactory {

	private static Logger logger = LoggerLoader.getLogger(ServiceProviderFactory.class);

	private static ConcurrentHashMap<String, ProviderConfig<?>> serviceCache = new ConcurrentHashMap<String, ProviderConfig<?>>();

	private static ConfigManager configManager = ExtensionLoader.getExtension(ConfigManager.class);

	private static ServiceChangeListener serviceChangeListener = ExtensionLoader
			.getExtension(ServiceChangeListener.class);

	private static boolean DEFAULT_NOTIFY_ENABLE = ConfigConstants.ENV_DEV.equalsIgnoreCase(configManager.getEnv()) ? false
			: Constants.DEFAULT_NOTIFY_ENABLE;

	private static ConcurrentHashMap<String, Integer> serverWeightCache = new ConcurrentHashMap<String, Integer>();

	private static final int UNPUBLISH_WAITTIME = configManager.getIntValue(Constants.KEY_UNPUBLISH_WAITTIME,
			Constants.DEFAULT_UNPUBLISH_WAITTIME);

	public static String getServiceUrlWithVersion(String url, String version) {
		String newUrl = url;
		if (!StringUtils.isBlank(version)) {
			newUrl = url + "_" + version;
		}
		return newUrl;
	}

	public static <T> void addService(ProviderConfig<T> providerConfig) throws Exception {
		if (logger.isInfoEnabled()) {
			logger.info("add service:" + providerConfig);
		}
		String version = providerConfig.getVersion();
		String url = providerConfig.getUrl();
		if (StringUtils.isBlank(version)) {// default version
			serviceCache.put(url, providerConfig);
		} else {
			String urlWithVersion = getServiceUrlWithVersion(url, version);
			if (serviceCache.containsKey(url)) {
				serviceCache.put(urlWithVersion, providerConfig);
				ProviderConfig<?> providerConfigDefault = serviceCache.get(url);
				String defaultVersion = providerConfigDefault.getVersion();
				if (!StringUtils.isBlank(defaultVersion)) {
					if (VersionUtils.compareVersion(defaultVersion, providerConfig.getVersion()) < 0) {
						// replace existing service with this newer service as
						// the default provider
						serviceCache.put(url, providerConfig);
					}
				}
			} else {
				serviceCache.put(urlWithVersion, providerConfig);
				// use this service as the default provider
				serviceCache.put(url, providerConfig);
			}
		}
		T service = providerConfig.getService();
		if (service instanceof InitializingService) {
			((InitializingService) service).initialize();
		}
		ServiceMethodFactory.init(url);
	}

	public static <T> void publishService(ProviderConfig<T> providerConfig) throws RegistryException {
		String url = providerConfig.getUrl();
		boolean existingService = false;
		for (String key : serviceCache.keySet()) {
			ProviderConfig<?> pc = serviceCache.get(key);
			if (pc.getUrl().equals(url)) {
				existingService = true;
				break;
			}
		}
		if (logger.isInfoEnabled()) {
			logger.info("try to publish service to registry:" + providerConfig + ", existing service:"
					+ existingService);
		}
		if (existingService) {
			boolean autoPublishEnable = ConfigManagerLoader.getConfigManager().getBooleanValue(
					Constants.KEY_AUTOPUBLISH_ENABLE, true);
			if (autoPublishEnable) {
				List<Server> servers = ProviderBootStrap.getServers(providerConfig);
				int registerCount = 0;
				for (Server server : servers) {
					publishService(server.getRegistryUrl(url), server.getPort(), providerConfig.getServerConfig()
							.getGroup());
					registerCount++;
				}
				if (registerCount > 0) {
					boolean isNotify = configManager
							.getBooleanValue(Constants.KEY_NOTIFY_ENABLE, DEFAULT_NOTIFY_ENABLE);
					if (isNotify && serviceChangeListener != null) {
						serviceChangeListener.notifyServicePublished(providerConfig);
					}
					StatusContainer.setPhase(Phase.PUBLISHING);

					boolean autoRegisterEnable = ConfigManagerLoader.getConfigManager().getBooleanValue(
							Constants.KEY_AUTOREGISTER_ENABLE, true);
					if (autoRegisterEnable) {
						ServiceWarmupListener.start();
					}

					providerConfig.setPublished(true);
				}
			} else {
				logger.info("auto publish is disabled");
			}
		}
	}

	public static void publishService(String url) throws RegistryException {
		if (logger.isInfoEnabled()) {
			logger.info("publish service:" + url);
		}
		ProviderConfig<?> providerConfig = serviceCache.get(url);
		if (providerConfig != null) {
			for (String key : serviceCache.keySet()) {
				ProviderConfig<?> pc = serviceCache.get(key);
				if (pc.getUrl().equals(url)) {
					publishService(pc);
				}
			}
		}
	}

	private synchronized static <T> void publishService(String url, int port, String group) throws RegistryException {
		String serverAddress = configManager.getLocalIp() + ":" + port;
		int weight = Constants.WEIGHT_INITIAL;
		boolean autoRegisterEnable = ConfigManagerLoader.getConfigManager().getBooleanValue(
				Constants.KEY_AUTOREGISTER_ENABLE, true);
		if (!autoRegisterEnable) {
			weight = 0;
		}
		boolean warmupEnable = ConfigManagerLoader.getConfigManager().getBooleanValue(
				Constants.KEY_SERVICEWARMUP_ENABLE, true);
		if (!warmupEnable) {
			weight = Constants.WEIGHT_DEFAULT;
		}
		if (serverWeightCache.containsKey(serverAddress)) {
			weight = -1;
		}
		if (logger.isInfoEnabled()) {
			logger.info("publish service to registry, url:" + url + ", port:" + port + ", group:" + group
					+ ", address:" + serverAddress + ", weight:" + weight);
		}
		RegistryManager.getInstance().registerService(url, group, serverAddress, weight);
		if (weight >= 0) {
			serverWeightCache.put(serverAddress, weight);
		}
	}

	public static Map<String, Integer> getServerWeight() {
		return serverWeightCache;
	}

	public synchronized static void setServerWeight(int weight) throws RegistryException {
		if (weight < 0 || weight > 100) {
			throw new IllegalArgumentException("The weight must be within the range of 0 to 100:" + weight);
		}
		for (String serverAddress : serverWeightCache.keySet()) {
			if (logger.isInfoEnabled()) {
				logger.info("set weight, address:" + serverAddress + ", weight:" + weight);
			}
			RegistryManager.getInstance().setServerWeight(serverAddress, weight);
			serverWeightCache.put(serverAddress, weight);
		}
		if (weight <= 0) {
			StatusContainer.setPhase(Phase.OFFLINE);
		} else {
			StatusContainer.setPhase(Phase.ONLINE);
		}
	}

	public synchronized static <T> void unpublishService(ProviderConfig<T> providerConfig) throws RegistryException {
		String url = providerConfig.getUrl();
		boolean existingService = false;
		for (String key : serviceCache.keySet()) {
			ProviderConfig<?> pc = serviceCache.get(key);
			if (pc.getUrl().equals(url)) {
				existingService = true;
				break;
			}
		}
		if (logger.isInfoEnabled()) {
			logger.info("try to unpublish service from registry:" + providerConfig + ", existing service:"
					+ existingService);
		}
		if (existingService) {
			StatusContainer.setPhase(Phase.TOUNPUBLISH);
			List<Server> servers = ProviderBootStrap.getServers(providerConfig);
			for (Server server : servers) {
				String serverAddress = configManager.getLocalIp() + ":" + server.getPort();
				RegistryManager.getInstance().unregisterService(server.getRegistryUrl(providerConfig.getUrl()),
						providerConfig.getServerConfig().getGroup(), serverAddress);
				serverWeightCache.remove(serverAddress);
			}
			boolean isNotify = configManager.getBooleanValue(Constants.KEY_NOTIFY_ENABLE, DEFAULT_NOTIFY_ENABLE);
			if (isNotify && serviceChangeListener != null) {
				serviceChangeListener.notifyServiceUnpublished(providerConfig);
			}
			providerConfig.setPublished(false);
			if (logger.isInfoEnabled()) {
				logger.info("unpublished service from registry:" + providerConfig);
			}
		}
	}

	public static void unpublishService(String url) throws RegistryException {
		if (logger.isInfoEnabled()) {
			logger.info("unpublish service:" + url);
		}
		ProviderConfig<?> providerConfig = serviceCache.get(url);
		if (providerConfig != null) {
			for (String key : serviceCache.keySet()) {
				ProviderConfig<?> pc = serviceCache.get(key);
				if (pc.getUrl().equals(url)) {
					unpublishService(pc);
				}
			}
		}
	}

	public static ProviderConfig<?> getServiceConfig(String url) {
		ProviderConfig<?> providerConfig = serviceCache.get(url);
		return providerConfig;
	}

	public static void removeService(String url) throws RegistryException {
		if (logger.isInfoEnabled()) {
			logger.info("remove service:" + url);
		}
		List<String> toRemovedUrls = new ArrayList<String>();
		for (String key : serviceCache.keySet()) {
			ProviderConfig<?> pc = serviceCache.get(key);
			if (pc.getUrl().equals(url)) {
				unpublishService(pc);
				toRemovedUrls.add(key);
				Object service = pc.getService();
				if (service instanceof DisposableService) {
					try {
						((DisposableService) service).destroy();
					} catch (Throwable e) {
						logger.warn("error while destroy service:" + url + ", caused by " + e.getMessage());
					}
				}
			}
		}
		for (String key : toRemovedUrls) {
			serviceCache.remove(key);
		}
	}

	public static void removeAllServices() throws RegistryException {
		if (logger.isInfoEnabled()) {
			logger.info("remove all services");
		}
		unpublishAllServices();
		serviceCache.clear();
	}

	public static void unpublishAllServices() throws RegistryException {
		if (logger.isInfoEnabled()) {
			logger.info("unpublish all services");
		}
		ServiceWarmupListener.stop();
		StatusContainer.setPhase(Phase.TOUNPUBLISH);
		setServerWeight(0);
		try {
			Thread.sleep(UNPUBLISH_WAITTIME);
		} catch (InterruptedException e) {
		}
		for (String url : serviceCache.keySet()) {
			ProviderConfig<?> providerConfig = serviceCache.get(url);
			if (providerConfig != null) {
				unpublishService(providerConfig);
			}
		}
		StatusContainer.setPhase(Phase.UNPUBLISHED);
	}

	public static void publishAllServices() throws RegistryException {
		if (logger.isInfoEnabled()) {
			logger.info("publish all services");
		}
		for (String url : serviceCache.keySet()) {
			ProviderConfig<?> providerConfig = serviceCache.get(url);
			if (providerConfig != null) {
				publishService(providerConfig);
			}
		}
		StatusContainer.setPhase(Phase.PUBLISHED);
	}

	public static Map<String, ProviderConfig<?>> getAllServiceProviders() {
		return serviceCache;
	}

}
