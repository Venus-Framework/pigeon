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

import org.apache.log4j.Logger;

import com.dianping.pigeon.component.HostInfo;
import com.dianping.pigeon.extension.ExtensionLoader;
import com.dianping.pigeon.registry.config.DefaultRegistryConfigManager;
import com.dianping.pigeon.registry.config.RegistryConfigManager;
import com.dianping.pigeon.registry.exception.RegistryException;
import com.dianping.pigeon.registry.util.Constants;
import com.dianping.pigeon.registry.util.Utils;


public class RegistryManager {

	private static final Logger logger = Logger.getLogger(RegistryManager.class);

	private static Properties defaultPts = new Properties();

	private static boolean isInit = false;

	private static Registry registry = ExtensionLoader.getExtension(Registry.class);

	private static RegistryConfigManager registryConfigManager = new DefaultRegistryConfigManager();

	private static RegistryManager instance = new RegistryManager();

	private static Map<String, Set<HostInfo>> serviceNameToHostInfos = new ConcurrentHashMap<String, Set<HostInfo>>();
	
	private static Map<String, HostInfo> serviceAddrToHostInfo = new ConcurrentHashMap<String, HostInfo>();
	
	private RegistryManager() {
	}

	public static RegistryManager getInstance() {
		if (!isInit) {
			instance.init(registryConfigManager.getRegistryConfig());
			isInit = true;
		}
		return instance;
	}

	public void init(Properties properties) {
		instance.setPts(properties);
		String registryType = properties.getProperty(Constants.KEY_REGISTRY_TYPE);
		if (!"local".equalsIgnoreCase(registryType)) {
			if (registry != null) {
				registry.init(properties);
			}
		} else {
		}
	}

	public static String getProperty(String key) throws RegistryException {
		String v = defaultPts.getProperty(key);
		return v;
	}

	/**
	 * @param pts
	 *            the pts to set
	 */
	public static void update(String key, String value) {
		// 如果是dev环境，可以把当前配置加载进去
		defaultPts.put(key, value);
	}

	/**
	 * @param pts
	 *            the pts to set
	 */
	public void setPts(Properties pts) {
		defaultPts.putAll(pts);
	}

	public String getServiceAddress(String serviceName, String group) throws RegistryException {
		String serviceKey = getServiceKey(serviceName, group);
		if (defaultPts.containsKey(serviceKey)) {
			if (logger.isInfoEnabled()) {
				logger.info("get service address from local properties, service name:" + serviceName + "  address:"
						+ defaultPts.getProperty(serviceKey));
			}
			return defaultPts.getProperty(serviceKey);
		}
		if (registry != null) {
			return registry.getServiceAddress(serviceName, group);
		}
		
		return null;
	}

	private String getServiceKey(String serviceName, String group) {
		return serviceName + "?group=" + group;
	}
	
	public int getServiceWeight(String serviceAddress) {
		HostInfo hostInfo = serviceAddrToHostInfo.get(serviceAddress);
		if(hostInfo != null) {
			return hostInfo.getWeight();
		}
		int weight = Constants.DEFAULT_WEIGHT_INT;
		if (registry != null) {
			try {
				weight = registry.getServiceWeigth(serviceAddress);
			} catch (RegistryException e) {
				logger.error("Failed to get weight for " + serviceAddress, e);
			}
		}
		return weight;
	}
	

	public void setServiceWeight(String serviceAddress, int weight) {
		HostInfo hostInfo = serviceAddrToHostInfo.get(serviceAddress);
		if(hostInfo == null) {
			logger.warn("Server " + serviceAddress + " does not exist");
			return;
		}
		hostInfo.setWeight(weight);
		logger.info("Set " + serviceAddress + " weight to " + weight);
	}

	public void publishService(String serviceName, String serviceAddress) throws RegistryException {
		if (registry != null) {
			registry.publishService(serviceName, serviceAddress);
		}
	}
	
	public void addServiceServer(String serviceName, String connect, int weight) {
		Utils.validateWeight(weight);
		
		HostInfo hostInfo = new HostInfo(connect, weight);
		
		Set<HostInfo> hostInfos = serviceNameToHostInfos.get(serviceName);
		if(hostInfos == null) {
			hostInfos = new HashSet<HostInfo>();
			serviceNameToHostInfos.put(serviceName, hostInfos);
		}
		hostInfos.add(hostInfo);

		if(!serviceAddrToHostInfo.containsKey(connect)) {
			serviceAddrToHostInfo.put(connect, hostInfo);
		}
		
		logger.info("Add server " + hostInfo + " to service " + serviceName);
	}
	
	public void removeServiceServer(String serviceName, HostInfo hostInfo) {
		Set<HostInfo> hostInfos = serviceNameToHostInfos.get(serviceName);
		if(hostInfos == null || !hostInfos.contains(hostInfo)) {
			logger.warn("Server " + hostInfo + " is not in server list of service " + serviceName);
			return;
		}
		hostInfos.remove(hostInfo);
		
		// TODO May need to remove server from serviceAddrToHostInfo
		logger.info("Remove server " + hostInfo + " from service " + serviceName);
	}
	
	public Set<HostInfo> getServiceServers(String serviceName) {
		Set<HostInfo> hostInfos = serviceNameToHostInfos.get(serviceName);
		if(hostInfos == null || hostInfos.size()==0) {
			logger.warn("Server list of service " + serviceName + " is empty");
		}
		return hostInfos;
	}
	
	public Map<String, Set<HostInfo>> getAllServiceServers() {
		return serviceNameToHostInfos;
	}

}
