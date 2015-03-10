/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.registry;

import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.dianping.pigeon.config.ConfigConstants;
import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.domain.HostInfo;
import com.dianping.pigeon.extension.ExtensionLoader;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.registry.config.DefaultRegistryConfigManager;
import com.dianping.pigeon.registry.config.RegistryConfigManager;
import com.dianping.pigeon.registry.exception.RegistryException;
import com.dianping.pigeon.registry.listener.RegistryEventListener;
import com.dianping.pigeon.registry.listener.ServerInfoListener;
import com.dianping.pigeon.registry.util.Constants;
import com.dianping.pigeon.registry.util.Utils;

public class RegistryManager {

	private static final Logger logger = LoggerLoader.getLogger(RegistryManager.class);

	private Properties props = new Properties();

	private static volatile boolean isInit = false;

	private static Throwable initializeException = null;

	private static RegistryManager instance = new RegistryManager();

	private static RegistryConfigManager registryConfigManager = new DefaultRegistryConfigManager();

	private static Registry registry = ExtensionLoader.getExtension(Registry.class);

	private static Map<String, Set<HostInfo>> referencedServiceAddresses = new ConcurrentHashMap<String, Set<HostInfo>>();

	private static Map<String, HostInfo> referencedAddresses = new ConcurrentHashMap<String, HostInfo>();

	private static ConfigManager configManager = ExtensionLoader.getExtension(ConfigManager.class);

	private static ConcurrentHashMap<String, Object> registeredServices = new ConcurrentHashMap<String, Object>();

	private RegistryManager() {
	}

	public static boolean isInitialized() {
		return isInit;
	}

	public static Throwable getInitializeException() {
		return initializeException;
	}

	public static RegistryManager getInstance() {
		if (!isInit) {
			synchronized (RegistryManager.class) {
				if (!isInit) {
					instance.init(registryConfigManager.getRegistryConfig());
					initializeException = null;
					RegistryEventListener.addListener(new InnerServerInfoListener());
					isInit = true;
				}
			}
		}
		return instance;
	}

	private void init(Properties properties) {
		instance.setProperties(properties);
		String registryType = properties.getProperty(Constants.KEY_REGISTRY_TYPE);
		if (!Constants.REGISTRY_TYPE_LOCAL.equalsIgnoreCase(registryType)) {
			if (registry != null) {
				try {
					registry.init(properties);
				} catch (Throwable t) {
					initializeException = t;
					throw new RuntimeException(t);
				}
			}
		} else {
		}
	}

	public Registry getRegistry() {
		return registry;
	}

	public void setProperty(String key, String value) {
		// 如果是dev环境，可以把当前配置加载进去
		props.put(key, value);
	}

	public void setProperties(Properties props) {
		this.props.putAll(props);
	}

	public Set<String> getReferencedServices() {
		return referencedServiceAddresses.keySet();
	}

	public Set<String> getRegisteredServices() {
		return registeredServices.keySet();
	}

	public boolean isReferencedService(String serviceName, String group) {
		return referencedServiceAddresses.containsKey(serviceName);
	}

	public String getServiceAddress(String serviceName, String group) throws RegistryException {
		String serviceKey = getServiceKey(serviceName, group);
		if (props.containsKey(serviceKey)) {
			if (logger.isInfoEnabled()) {
				logger.info("get service address from local properties, service name:" + serviceName + "  address:"
						+ props.getProperty(serviceKey));
			}
			return props.getProperty(serviceKey);
		}
		if (ConfigConstants.ENV_DEV.equalsIgnoreCase(configManager.getEnv())
				|| ConfigConstants.ENV_ALPHA.equalsIgnoreCase(configManager.getEnv())) {
			String addr = configManager.getLocalStringValue(Utils.escapeServiceName(serviceKey));
			if (addr == null) {
				try {
					addr = configManager.getLocalStringValue(serviceKey);
				} catch (Throwable e) {
				}
			}
			if (!StringUtils.isBlank(addr)) {
				if (logger.isInfoEnabled()) {
					logger.info("get service address from local properties, service name:" + serviceName + "  address:"
							+ addr);
				}
				return addr;
			}
		}
		if (registry != null) {
			String addr = registry.getServiceAddress(serviceName, group);
			return addr;
		}

		return null;
	}

	private String getServiceKey(String serviceName, String group) {
		if (StringUtils.isBlank(group)) {
			return serviceName;
		} else {
			return serviceName + "?group=" + group;
		}
	}

	public int getServiceWeight(String serverAddress, boolean readCache) {
		if (readCache) {
			HostInfo hostInfo = referencedAddresses.get(serverAddress);
			if (hostInfo != null) {
				return hostInfo.getWeight();
			}
		}
		int weight = Constants.WEIGHT_DEFAULT;
		if (registry != null) {
			try {
				weight = registry.getServerWeight(serverAddress);
				HostInfo hostInfo = referencedAddresses.get(serverAddress);
				if (hostInfo != null) {
					hostInfo.setWeight(weight);
				}
			} catch (Throwable e) {
				logger.error("failed to get weight for " + serverAddress, e);
			}
		}
		return weight;
	}

	public int getServiceWeight(String serverAddress) {
		return getServiceWeight(serverAddress, true);
	}

	/*
	 * Update service weight in local cache. Will not update to registry center.
	 */
	public void setServiceWeight(String serviceAddress, int weight) {
		HostInfo hostInfo = referencedAddresses.get(serviceAddress);
		if (hostInfo == null) {
			if (!serviceAddress.startsWith(configManager.getLocalIp())) {
				logger.warn("weight not found for address:" + serviceAddress);
			}
			return;
		}
		hostInfo.setWeight(weight);
		logger.info("set " + serviceAddress + " weight to " + weight);
	}

	public void registerService(String serviceName, String group, String serviceAddress, int weight)
			throws RegistryException {
		if (registry != null) {
			registry.registerService(serviceName, group, serviceAddress, weight);
			registeredServices.putIfAbsent(serviceName, serviceAddress);
		}
	}

	public void setServerWeight(String serverAddress, int weight) throws RegistryException {
		if (registry != null) {
			registry.setServerWeight(serverAddress, weight);
		}
	}

	public void unregisterService(String serviceName, String serviceAddress) throws RegistryException {
		unregisterService(serviceName, Constants.DEFAULT_GROUP, serviceAddress);
	}

	public void unregisterService(String serviceName, String group, String serviceAddress) throws RegistryException {
		if (registry != null) {
			registry.unregisterService(serviceName, group, serviceAddress);
			registeredServices.remove(serviceName);
		}
	}

	public void addServiceAddress(String serviceName, String host, int port, int weight) {
		Utils.validateWeight(host, port, weight);

		HostInfo hostInfo = new HostInfo(host, port, weight);

		Set<HostInfo> hostInfos = referencedServiceAddresses.get(serviceName);
		if (hostInfos == null) {
			hostInfos = new HashSet<HostInfo>();
			referencedServiceAddresses.put(serviceName, hostInfos);
		}
		hostInfos.add(hostInfo);

		if (!referencedAddresses.containsKey(hostInfo.getConnect())) {
			referencedAddresses.put(hostInfo.getConnect(), hostInfo);
			if (registry != null) {
				String app = registry.getServerApp(hostInfo.getConnect());
				hostInfo.setApp(app);
			}
		}
	}

	public void removeServiceAddress(String serviceName, HostInfo hostInfo) {
		Set<HostInfo> hostInfos = referencedServiceAddresses.get(serviceName);
		if (hostInfos == null || !hostInfos.contains(hostInfo)) {
			logger.warn("address:" + hostInfo + " is not in address list of service " + serviceName);
			return;
		}
		hostInfos.remove(hostInfo);
		logger.info("removed address:" + hostInfo + " from service:" + serviceName);

		// If server is not referencd any more, remove from server list
		if (!isAddressReferenced(hostInfo)) {
			referencedAddresses.remove(hostInfo.getConnect());
		}
	}

	private boolean isAddressReferenced(HostInfo hostInfo) {
		for (Set<HostInfo> hostInfos : referencedServiceAddresses.values()) {
			if (hostInfos.contains(hostInfo))
				return true;
		}
		return false;
	}

	public Set<HostInfo> getReferencedServiceAddresses(String serviceName) {
		Set<HostInfo> hostInfos = referencedServiceAddresses.get(serviceName);
		if (hostInfos == null || hostInfos.size() == 0) {
			logger.warn("empty address list for service:" + serviceName);
		}
		return hostInfos;
	}

	public Map<String, Set<HostInfo>> getAllReferencedServiceAddresses() {
		return referencedServiceAddresses;
	}

	public String getServerApp(String serverAddress) {
		HostInfo hostInfo = referencedAddresses.get(serverAddress);
		String app = null;
		if (hostInfo != null) {
			app = hostInfo.getApp();
			if (app == null && registry != null) {
				app = registry.getServerApp(serverAddress);
				hostInfo.setApp(app);
			}
			return app;
		}
		return "";
	}

	public void setServerApp(String serverAddress, String app) {
		if (registry != null) {
			registry.setServerApp(serverAddress, app);
		}
	}

	public void unregisterServerApp(String serverAddress) {
		if (registry != null) {
			registry.unregisterServerApp(serverAddress);
		}
	}

	public void setServerVersion(String serverAddress, String version) {
		if (registry != null) {
			registry.setServerVersion(serverAddress, version);
		}
	}

	public void unregisterServerVersion(String serverAddress) {
		if (registry != null) {
			registry.unregisterServerVersion(serverAddress);
		}
	}

	static class InnerServerInfoListener implements ServerInfoListener {

		@Override
		public void onServerAppChange(String serverAddress, String app) {
			HostInfo hostInfo = referencedAddresses.get(serverAddress);
			if (hostInfo != null) {
				hostInfo.setApp(app);
			}
		}
	}
}
