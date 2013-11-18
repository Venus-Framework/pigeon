/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.provider.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;

import com.dianping.dpsf.exception.ServiceException;
import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.extension.ExtensionLoader;
import com.dianping.pigeon.registry.RegistryManager;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.common.util.VersionUtils;
import com.dianping.pigeon.remoting.provider.component.ProviderConfig;
import com.dianping.pigeon.util.IpUtils;

/**
 * @author xiangwu
 * @Sep 30, 2013
 * 
 */
public final class ServiceProviderFactory {

	private static ConcurrentHashMap<String, ProviderConfig> serviceCache = new ConcurrentHashMap<String, ProviderConfig>();

	private static ConcurrentHashMap<String, Boolean> serviceRegisterStatus = new ConcurrentHashMap<String, Boolean>();

	private static ConfigManager configManager = ExtensionLoader.getExtension(ConfigManager.class);

	public ServiceProviderFactory() {
	}

	public static void addServices(Map<String, Object> services, int port) throws ServiceException {
		for (String url : services.keySet()) {
			ProviderConfig providerConfig = new ProviderConfig(services.get(url));
			providerConfig.setUrl(url);
			providerConfig.setPort(port);
			addService(providerConfig);
		}
	}

	public static String getServiceUrlWithVersion(String url, String version) {
		String newUrl = url;
		if (!StringUtils.isBlank(version)) {
			newUrl = url + "_" + version;
		}
		return newUrl;
	}

	public static <T> void addService(ProviderConfig<T> providerConfig) throws ServiceException {
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
		if (!serviceRegisterStatus.contains(providerConfig.getUrl())) {
			try {
				String autoRegister = configManager.getProperty(Constants.KEY_AUTO_REGISTER,
						Constants.DEFAULT_AUTO_REGISTER);
				if ("true".equalsIgnoreCase(autoRegister)) {
					String localip = RegistryManager.getInstance().getProperty(Constants.KEY_LOCAL_IP);
					if (localip == null || localip.length() == 0) {
						localip = IpUtils.getFirstLocalIp();
					}
					String serviceAddress = localip + ":" + providerConfig.getPort();
					String group = configManager.getProperty(Constants.KEY_GROUP, Constants.DEFAULT_GROUP);
					String weight = configManager.getProperty(Constants.KEY_WEIGHT, Constants.DEFAULT_WEIGHT);
					int intWeight = Integer.parseInt(weight);
					RegistryManager.getInstance().publishService(providerConfig.getUrl(), group, serviceAddress,
							intWeight);
					serviceRegisterStatus.put(providerConfig.getUrl(), true);
				} else {
					serviceRegisterStatus.put(providerConfig.getUrl(), false);
				}
			} catch (Exception e) {
				throw new ServiceException("", e);
			}
		}
	}

	public static Map<String, ProviderConfig> getAllServices() {
		return serviceCache;
	}

}
