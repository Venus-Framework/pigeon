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
import com.dianping.pigeon.monitor.LoggerLoader;
import com.dianping.pigeon.registry.RegistryManager;
import com.dianping.pigeon.registry.exception.RegistryException;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.common.util.VersionUtils;
import com.dianping.pigeon.remoting.provider.component.ProviderConfig;
import com.dianping.pigeon.util.NetUtils;

/**
 * @author xiangwu
 * @Sep 30, 2013
 * 
 */
public final class ServiceProviderFactory {

	private static Logger logger = LoggerLoader.getLogger(ServiceProviderFactory.class);

	private static ConcurrentHashMap<String, ProviderConfig> serviceCache = new ConcurrentHashMap<String, ProviderConfig>();

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
				ProviderConfig providerConfigDefault = serviceCache.get(url);
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
		publishServiceToRegistry(providerConfig);
	}

	private static <T> void publishServiceToRegistry(ProviderConfig<T> providerConfig) throws ServiceException {
		if (logger.isInfoEnabled()) {
			logger.info("publish service:" + providerConfig);
		}
		if (!registerStatusCache.contains(providerConfig.getUrl())) {
			try {
				String autoRegister = configManager.getProperty(Constants.KEY_AUTO_REGISTER,
						Constants.DEFAULT_AUTO_REGISTER);
				if ("true".equalsIgnoreCase(autoRegister)) {
					String localip = RegistryManager.getInstance().getProperty(Constants.KEY_LOCAL_IP);
					if (localip == null || localip.length() == 0) {
						localip = NetUtils.getFirstLocalIp();
					}
					String serviceAddress = localip + ":" + providerConfig.getPort();
					String group = configManager.getProperty(Constants.KEY_GROUP, Constants.DEFAULT_GROUP);
					providerConfig.setGroup(group);
					String weight = configManager.getProperty(Constants.KEY_WEIGHT, Constants.DEFAULT_WEIGHT);
					int intWeight = Integer.parseInt(weight);
					RegistryManager.getInstance().registerService(providerConfig.getUrl(), group, serviceAddress,
							intWeight);
					registerStatusCache.put(providerConfig.getUrl(), true);
				} else {
					registerStatusCache.put(providerConfig.getUrl(), false);
				}
			} catch (Exception e) {
				throw new ServiceException("", e);
			}
		}
	}

	public static void removeService(String url) throws ServiceException {
		if (logger.isInfoEnabled()) {
			logger.info("remove service:" + url);
		}
		ProviderConfig providerConfig = serviceCache.get(url);
		if (providerConfig != null) {
			String localip = RegistryManager.getInstance().getProperty(Constants.KEY_LOCAL_IP);
			if (localip == null || localip.length() == 0) {
				localip = NetUtils.getFirstLocalIp();
			}
			String serviceAddress = localip + ":" + providerConfig.getPort();
			try {
				RegistryManager.getInstance().unregisterService(url, providerConfig.getGroup(), serviceAddress);
			} catch (RegistryException e) {
				throw new ServiceException("", e);
			}
			List<String> toRemovedUrls = new ArrayList<String>();
			for (String key : serviceCache.keySet()) {
				ProviderConfig pc = serviceCache.get(key);
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

	public static Map<String, ProviderConfig> getAllServices() {
		return serviceCache;
	}

}
