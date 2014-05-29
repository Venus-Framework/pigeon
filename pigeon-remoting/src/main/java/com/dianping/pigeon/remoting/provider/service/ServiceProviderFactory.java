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

import com.dianping.dpsf.exception.ServiceException;
import com.dianping.pigeon.config.ConfigConstants;
import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.extension.ExtensionLoader;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.registry.RegistryManager;
import com.dianping.pigeon.registry.exception.RegistryException;
import com.dianping.pigeon.remoting.common.exception.RpcException;
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

	private static volatile PublishStatus status = PublishStatus.TOPUBLISH;

	private static final int UNPUBLISH_WAITTIME = configManager.getIntValue(Constants.KEY_UNPUBLISH_WAITTIME,
			Constants.DEFAULT_UNPUBLISH_WAITTIME);

	private static final int WEIGHT_INITIAL = configManager.getIntValue(Constants.KEY_WEIGHT_INITIAL,
			Constants.DEFAULT_WEIGHT_INITIAL);

	public static String getServiceUrlWithVersion(String url, String version) {
		String newUrl = url;
		if (!StringUtils.isBlank(version)) {
			newUrl = url + "_" + version;
		}
		return newUrl;
	}

	public static <T> void addService(ProviderConfig<T> providerConfig) throws ServiceException {
		String group = configManager.getGroup();
		providerConfig.getServerConfig().setGroup(group);
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
		ServiceMethodFactory.init(url);
	}

	public static <T> void publishService(ProviderConfig<T> providerConfig) throws ServiceException {
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
			List<Server> servers = ProviderBootStrap.getServers(providerConfig);
			int registerCount = 0;
			for (Server server : servers) {
				try {
					server.addService(providerConfig);
				} catch (RpcException e) {
					throw new ServiceException("", e);
				}
				publishService(server.getRegistryUrl(url), server.getPort(), providerConfig.getServerConfig()
						.getGroup());
				registerCount++;
			}
			if (registerCount > 0) {
				boolean isNotify = configManager.getBooleanValue(Constants.KEY_NOTIFY_ENABLE, DEFAULT_NOTIFY_ENABLE);
				if (isNotify && serviceChangeListener != null) {
					serviceChangeListener.notifyServicePublished(providerConfig);
				}
				status = PublishStatus.PUBLISHING;
				ServiceWarmupListener.start();

				providerConfig.setPublished(true);
			}
		}
	}

	public static void publishService(String url) throws ServiceException {
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

	private synchronized static <T> void publishService(String url, int port, String group) throws ServiceException {
		try {
			String serverAddress = configManager.getLocalIp() + ":" + port;
			int weight = WEIGHT_INITIAL;
			if (logger.isInfoEnabled()) {
				logger.info("publish service to registry, url:" + url + ", port:" + port + ", group:" + group
						+ ", address:" + serverAddress + ", weight:" + weight);
			}
			RegistryManager.getInstance().registerService(url, group, serverAddress, weight);
			serverWeightCache.put(serverAddress, weight);
		} catch (Exception e) {
			throw new ServiceException("", e);
		}
	}

	public static Map<String, Integer> getServerWeight() {
		return serverWeightCache;
	}

	public synchronized static void setServerWeight(int weight) throws ServiceException {
		if (weight < 0 || weight > 100) {
			throw new IllegalArgumentException("The weight must be within the range of 0 to 100:" + weight);
		}
		try {
			for (String serverAddress : serverWeightCache.keySet()) {
				if (logger.isInfoEnabled()) {
					logger.info("set weight, address:" + serverAddress + ", weight:" + weight);
				}
				RegistryManager.getInstance().setServerWeight(serverAddress, weight);
				serverWeightCache.put(serverAddress, weight);
			}
			if (weight <= 0) {
				status = PublishStatus.OFFLINE;
			}
		} catch (Exception e) {
			throw new ServiceException("", e);
		}
	}

	public synchronized static <T> void unpublishService(ProviderConfig<T> providerConfig) throws ServiceException {
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
			status = PublishStatus.TOUNPUBLISH;
			List<Server> servers = ProviderBootStrap.getServers(providerConfig);
			for (Server server : servers) {
				String serverAddress = configManager.getLocalIp() + ":" + server.getPort();
				try {
					RegistryManager.getInstance().unregisterService(server.getRegistryUrl(providerConfig.getUrl()),
							providerConfig.getServerConfig().getGroup(), serverAddress);
					serverWeightCache.remove(serverAddress);
				} catch (RegistryException e) {
					throw new ServiceException("", e);
				}
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

	public static void unpublishService(String url) throws ServiceException {
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

	public static void removeService(String url) throws ServiceException {
		if (logger.isInfoEnabled()) {
			logger.info("remove service:" + url);
		}
		List<String> toRemovedUrls = new ArrayList<String>();
		for (String key : serviceCache.keySet()) {
			ProviderConfig<?> pc = serviceCache.get(key);
			if (pc.getUrl().equals(url)) {
				unpublishService(pc);
				toRemovedUrls.add(key);
			}
		}
		for (String key : toRemovedUrls) {
			serviceCache.remove(key);
		}
	}

	public static void removeAllServices() throws ServiceException {
		if (logger.isInfoEnabled()) {
			logger.info("remove all services");
		}
		unpublishAllServices();
		serviceCache.clear();
	}

	public static void unpublishAllServices() throws ServiceException {
		if (logger.isInfoEnabled()) {
			logger.info("unpublish all services");
		}
		status = PublishStatus.TOUNPUBLISH;
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
		status = PublishStatus.UNPUBLISHED;
	}

	public static void publishAllServices() throws ServiceException {
		if (logger.isInfoEnabled()) {
			logger.info("publish all services");
		}
		for (String url : serviceCache.keySet()) {
			ProviderConfig<?> providerConfig = serviceCache.get(url);
			if (providerConfig != null) {
				publishService(providerConfig);
			}
		}
		status = PublishStatus.PUBLISHED;
	}

	public static Map<String, ProviderConfig<?>> getAllServices() {
		return serviceCache;
	}

	public synchronized static void setPublishStatus(PublishStatus publishStatus) {
		status = publishStatus;
	}

	public static PublishStatus getPublishStatus() {
		return status;
	}

}
