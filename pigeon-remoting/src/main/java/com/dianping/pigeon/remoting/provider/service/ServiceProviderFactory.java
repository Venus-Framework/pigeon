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
import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.extension.ExtensionLoader;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.registry.RegistryManager;
import com.dianping.pigeon.registry.exception.RegistryException;
import com.dianping.pigeon.remoting.common.exception.RpcException;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.provider.Server;
import com.dianping.pigeon.remoting.provider.config.ProviderConfig;
import com.dianping.pigeon.util.VersionUtils;

/**
 * @author xiangwu
 * @Sep 30, 2013
 * 
 */
public final class ServiceProviderFactory {

	private static Logger logger = LoggerLoader.getLogger(ServiceProviderFactory.class);

	private static ConcurrentHashMap<String, ProviderConfig<?>> serviceCache = new ConcurrentHashMap<String, ProviderConfig<?>>();

	private static ConcurrentHashMap<String, Boolean> registerStatusCache = new ConcurrentHashMap<String, Boolean>();

	private static ConfigManager configManager = ExtensionLoader.getExtension(ConfigManager.class);

	public ServiceProviderFactory() {
	}

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
		if (!registerStatusCache.contains(url)) {
			String autoRegister = configManager.getStringValue(Constants.KEY_AUTO_REGISTER,
					Constants.DEFAULT_AUTO_REGISTER);
			List<Server> servers = ExtensionLoader.getExtensionList(Server.class);
			for (Server server : servers) {
				if (server.support(providerConfig.getServerConfig())) {
					try {
						server.addService(providerConfig);
					} catch (RpcException e) {
						throw new ServiceException("", e);
					}
					if ("true".equalsIgnoreCase(autoRegister)) {
						publishServiceToRegistry(server.getRegistryUrl(url), server.getPort(), providerConfig
								.getServerConfig().getGroup());
						registerStatusCache.put(url, true);
					} else {
						registerStatusCache.put(url, false);
					}
				}
			}
		}
	}

	private static <T> void publishServiceToRegistry(String url, int port, String group) throws ServiceException {
		if (logger.isInfoEnabled()) {
			logger.info("publish service to registry, url:" + url + ", port:" + port + ", group:" + group);
		}
		try {
			String serviceAddress = configManager.getLocalIp() + ":" + port;
			int weight = configManager.getIntValue(Constants.KEY_WEIGHT, Constants.DEFAULT_WEIGHT);
			RegistryManager.getInstance().registerService(url, group, serviceAddress, weight);
		} catch (Exception e) {
			throw new ServiceException("", e);
		}
	}

	public static void removeService(String url) throws ServiceException {
		if (logger.isInfoEnabled()) {
			logger.info("remove service:" + url);
		}
		ProviderConfig<?> providerConfig = serviceCache.get(url);
		if (providerConfig != null) {
			List<Server> servers = ExtensionLoader.getExtensionList(Server.class);
			for (Server server : servers) {
				if (server.support(providerConfig.getServerConfig())) {
					String serviceAddress = configManager.getLocalIp() + ":" + server.getPort();
					try {
						RegistryManager.getInstance().unregisterService(server.getRegistryUrl(providerConfig.getUrl()),
								providerConfig.getServerConfig().getGroup(), serviceAddress);
					} catch (RegistryException e) {
						throw new ServiceException("", e);
					}
				}
			}
			List<String> toRemovedUrls = new ArrayList<String>();
			for (String key : serviceCache.keySet()) {
				ProviderConfig<?> pc = serviceCache.get(key);
				if (pc.getUrl().equals(url)) {
					toRemovedUrls.add(key);
				}
			}
			for (String key : toRemovedUrls) {
				serviceCache.remove(key);
			}
			registerStatusCache.remove(url);
		}
	}

	public static void removeAllServices() throws ServiceException {
		if (logger.isInfoEnabled()) {
			logger.info("remove all services");
		}
		for (String url : serviceCache.keySet()) {
			removeService(url);
		}
	}

	public static Map<String, ProviderConfig<?>> getAllServices() {
		return serviceCache;
	}

}
