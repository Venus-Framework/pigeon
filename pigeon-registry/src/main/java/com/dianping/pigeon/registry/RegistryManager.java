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
import com.dianping.pigeon.registry.util.Constants;
import com.dianping.pigeon.registry.util.Utils;

public class RegistryManager {

	private static final Logger logger = LoggerLoader.getLogger(RegistryManager.class);

	private Properties props = new Properties();

	private static volatile boolean isInit = false;

	private static Throwable initializeException = null;

	private static RegistryManager instance = new RegistryManager();

	private static RegistryConfigManager registryConfigManager = new DefaultRegistryConfigManager();

	private Registry registry = ExtensionLoader.getExtension(Registry.class);

	private Map<String, Set<HostInfo>> serviceNameToHostInfos = new ConcurrentHashMap<String, Set<HostInfo>>();

	private Map<String, HostInfo> serviceAddrToHostInfo = new ConcurrentHashMap<String, HostInfo>();

	ConfigManager configManager = ExtensionLoader.getExtension(ConfigManager.class);

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

	public String getServiceAddress(String serviceName) throws RegistryException {
		return RegistryManager.getInstance().getServiceAddress(serviceName, configManager.getGroup());
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
			return registry.getServiceAddress(serviceName, group);
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

	public int getServiceWeight(String serverAddress) {
		HostInfo hostInfo = serviceAddrToHostInfo.get(serverAddress);
		if (hostInfo != null) {
			return hostInfo.getWeight();
		}
		int weight = Constants.WEIGHT_DEFAULT;
		if (registry != null) {
			try {
				weight = registry.getServerWeight(serverAddress);
			} catch (Throwable e) {
				logger.error("Failed to get weight for " + serverAddress, e);
			}
		}
		return weight;
	}

	/*
	 * Update service weight in local cache. Will not update to registry center.
	 */
	public void setServiceWeight(String serviceAddress, int weight) {
		HostInfo hostInfo = serviceAddrToHostInfo.get(serviceAddress);
		if (hostInfo == null) {
			if (!serviceAddress.startsWith(configManager.getLocalIp())) {
				logger.warn("weight not found for address:" + serviceAddress);
			}
			return;
		}
		hostInfo.setWeight(weight);
		logger.info("Set " + serviceAddress + " weight to " + weight);
	}

	public void registerService(String serviceName, String group, String serviceAddress, int weight)
			throws RegistryException {
		if (registry != null) {
			registry.registerService(serviceName, group, serviceAddress, weight);
		}
	}

	public void setServerWeight(String serverAddress, int weight) throws RegistryException {
		if (registry != null) {
			registry.setServerWeight(serverAddress, weight);
		}
	}

	public void unregisterService(String serviceName, String serviceAddress) throws RegistryException {
		if (registry != null) {
			registry.unregisterService(serviceName, serviceAddress);
		}
	}

	public void unregisterService(String serviceName, String group, String serviceAddress) throws RegistryException {
		if (registry != null) {
			registry.unregisterService(serviceName, group, serviceAddress);
		}
	}

	// TODO multi thread support
	public void addServiceServer(String serviceName, String host, int port, int weight) {
		Utils.validateWeight(host, port, weight);

		HostInfo hostInfo = new HostInfo(host, port, weight);

		Set<HostInfo> hostInfos = serviceNameToHostInfos.get(serviceName);
		if (hostInfos == null) {
			hostInfos = new HashSet<HostInfo>();
			serviceNameToHostInfos.put(serviceName, hostInfos);
		}
		hostInfos.add(hostInfo);

		if (!serviceAddrToHostInfo.containsKey(hostInfo.getConnect())) {
			serviceAddrToHostInfo.put(hostInfo.getConnect(), hostInfo);
		}

		logger.info("Add server " + hostInfo + " to service " + serviceName);
	}

	public void removeServiceServer(String serviceName, HostInfo hostInfo) {
		Set<HostInfo> hostInfos = serviceNameToHostInfos.get(serviceName);
		if (hostInfos == null || !hostInfos.contains(hostInfo)) {
			logger.warn("Server " + hostInfo + " is not in server list of service " + serviceName);
			return;
		}
		hostInfos.remove(hostInfo);
		logger.info("Remove server " + hostInfo + " from service " + serviceName);

		// If server is not referencd any more, remove from server list
		if (!isServerReferenced(hostInfo)) {
			serviceAddrToHostInfo.remove(hostInfo);
			logger.info("Remove server from server list");
		}
	}

	private boolean isServerReferenced(HostInfo hostInfo) {
		for (Set<HostInfo> hostInfos : serviceNameToHostInfos.values()) {
			if (hostInfos.contains(hostInfo))
				return true;
		}
		return false;
	}

	public Set<HostInfo> getServiceServers(String serviceName) {
		Set<HostInfo> hostInfos = serviceNameToHostInfos.get(serviceName);
		if (hostInfos == null || hostInfos.size() == 0) {
			logger.warn("Server list of service " + serviceName + " is empty");
		}
		return hostInfos;
	}

	public Map<String, Set<HostInfo>> getAllServiceServers() {
		return serviceNameToHostInfos;
	}

}
