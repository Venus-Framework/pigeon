/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.registry;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import com.dianping.pigeon.config.ConfigChangeListener;
import com.dianping.pigeon.util.VersionUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.Logger;

import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.config.ConfigManagerLoader;
import com.dianping.pigeon.domain.HostInfo;
import com.dianping.pigeon.extension.ExtensionLoader;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.monitor.Monitor;
import com.dianping.pigeon.monitor.MonitorLoader;
import com.dianping.pigeon.registry.config.MultiRegistryConfigManager;
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

	private static RegistryConfigManager registryConfigManager = new MultiRegistryConfigManager();

	private volatile static List<Registry> registryList;

	private static final String KEY_PIGEON_REGISTRY_PREFER = "pigeon.registry.prefer";

	private static ConcurrentHashMap<String, Set<HostInfo>> referencedServiceAddresses = new ConcurrentHashMap<String, Set<HostInfo>>();

	private static ConcurrentHashMap<String, HostInfo> referencedAddresses = new ConcurrentHashMap<String, HostInfo>();

	private static ConfigManager configManager = ConfigManagerLoader.getConfigManager();

	private static ConcurrentHashMap<String, Object> registeredServices = new ConcurrentHashMap<String, Object>();

	// host --> (service --> support)
	private static ConcurrentHashMap<String, Map<String, Boolean>> referencedServiceProtocols
			= new ConcurrentHashMap<String, Map<String, Boolean>>();

	Monitor monitor = MonitorLoader.getMonitor();

	public static final boolean fallbackDefaultGroup = configManager.getBooleanValue("pigeon.registry.group.fallback",
			true);

	private static boolean enableLocalConfig = ConfigManagerLoader.getConfigManager().getBooleanValue(
			"pigeon.registry.config.local", false);

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
			List<Registry> _registryList = ExtensionLoader.getExtensionList(Registry.class);

			if (_registryList.size() > 0) {
				try {
					parseRegistryConfig(_registryList,
							configManager.getStringValue(KEY_PIGEON_REGISTRY_PREFER, "curator"));
					configManager.registerConfigChangeListener(new InnerConfigChangeListener());

					for (Registry registry : registryList) {
						registry.init(properties);
					}

				} catch (Throwable t) {
					initializeException = t;
					throw new RuntimeException(t);
				}
			}

		} else {
		}

	}

	private void parseRegistryConfig(List<Registry> _registryList, String registryPreferConfig) {
		Map<String, Registry> registryMapByName = Maps.newHashMap();
		for (Registry registry : _registryList) {
			registryMapByName.put(registry.getName(), registry);
		}

		List<String> registryPrefer = Arrays.asList(registryPreferConfig.split(","));
		List<Registry> orderedRegistryList = Lists.newArrayList();

		for (String registryName : registryPrefer) {
			if(registryMapByName.containsKey(registryName)) {
				orderedRegistryList.add(registryMapByName.get(registryName));
			} else {
				throw new RuntimeException("pigeon.registry.prefer config error! no registry: "+ registryName);
			}
		}

		registryList = orderedRegistryList;
	}

	public static List<Registry> getRegistryList() {
		return registryList;
	}

	@Deprecated
	public Registry getRegistry() {
		return registryList.get(0);
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

	public List<String> getServiceAddressList(String serviceName, String group) throws RegistryException {
		String serviceAddress = getServiceAddress(serviceName, group);
		return Utils.getAddressList(serviceName, serviceAddress);
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
		if (enableLocalConfig) {
			String addr = configManager.getLocalStringValue(Utils.escapeServiceName(serviceKey));
			if (addr == null) {
				try {
					addr = configManager.getLocalStringValue(serviceKey);
				} catch (Throwable e) {
				}
			}
			if (!StringUtils.isBlank(addr)) {
				if (logger.isDebugEnabled()) {
					logger.debug("get service address from local properties, service name:" + serviceName
							+ "  address:" + addr);
				}
				return addr;
			}
		}

		String addr = "";
		for (Registry registry : registryList) { // merge registry addr
			// Todo 两个注册中心获取到本地内存 目前采取合并地址方式
			addr = mergeAddress(addr,
					registry.getServiceAddress(serviceName, group, fallbackDefaultGroup));
		}

		return null;
	}

	private String getServiceKey(String serviceName, String group) {
		if (StringUtils.isBlank(group)) {
			return serviceName;
		} else {
			return serviceName + "?" + group;
		}
	}

	public int getServiceWeightFromCache(String serverAddress) {
		HostInfo hostInfo = referencedAddresses.get(serverAddress);
		if (hostInfo != null) {
			return hostInfo.getWeight();
		}
		return Constants.DEFAULT_WEIGHT;
	}

	public int getServiceWeight(String serverAddress, boolean readCache) {
		if (readCache) {
			HostInfo hostInfo = referencedAddresses.get(serverAddress);
			if (hostInfo != null) {
				return hostInfo.getWeight();
			}
		}
		int weight = Constants.DEFAULT_WEIGHT;

		List<Integer> checkList = Lists.newArrayList();
		for (Registry registry : registryList) {
			// Todo 两个注册中心获取到本地内存
			try {
				checkList.add(registry.getServerWeight(serverAddress));
			} catch (Throwable e) {
				logger.error("failed to get weight for " + serverAddress, e);
			}
		}
		if(checkList.size() > 0) {
			weight = checkValueConsistency(checkList);
		}

		HostInfo hostInfo = referencedAddresses.get(serverAddress);
		if (hostInfo != null) {
			hostInfo.setWeight(weight);
		}

		return weight;
	}

	private <T> T checkValueConsistency(List<T> checkList) {
		T result = checkList.get(0);

		for (T t : checkList) {
			if (!t.equals(result)) {
				String errorMsg = "result not same in different registries! value1: " + result + ", value2: " + t;

				if(configManager.getBooleanValue("pigeon.registry.check.value.consistency.exception", false)) {
					throw new RuntimeException(errorMsg);
				}

				logger.warn(errorMsg);
				break;
			}
		}

		return result;
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
		for (Registry registry : registryList) {
			registry.registerService(serviceName, group, serviceAddress, weight);
		}

		registeredServices.putIfAbsent(serviceName, serviceAddress);
		monitor.logEvent("PigeonService.register", serviceName, "weight=" + weight + "&group=" + group);
	}

	public void setServerWeight(String serverAddress, int weight) throws RegistryException {
		for (Registry registry : registryList) {
			registry.setServerWeight(serverAddress, weight);
		}

		monitor.logEvent("PigeonService.weight", weight + "", "");
	}

	public void unregisterService(String serviceName, String serviceAddress) throws RegistryException {
		unregisterService(serviceName, Constants.DEFAULT_GROUP, serviceAddress);
	}

	public void unregisterService(String serviceName, String group, String serviceAddress) throws RegistryException {
		for (Registry registry : registryList) {
			registry.unregisterService(serviceName, group, serviceAddress);
		}

		registeredServices.remove(serviceName);
		monitor.logEvent("PigeonService.unregister", serviceName, "group=" + group);
	}

	public void addServiceAddress(String serviceName, String host, int port, int weight) {
		Utils.validateWeight(host, port, weight);

		HostInfo hostInfo = new HostInfo(host, port, weight);
		String serviceAddress = hostInfo.getConnect();

		Set<HostInfo> hostInfos = referencedServiceAddresses.get(serviceName);
		if (hostInfos == null) {
			hostInfos = Collections.newSetFromMap(new ConcurrentHashMap<HostInfo, Boolean>());
			Set<HostInfo> oldHostInfos = referencedServiceAddresses.putIfAbsent(serviceName, hostInfos);
			if (oldHostInfos != null) {
				hostInfos = oldHostInfos;
			}
		}
		hostInfos.add(hostInfo);

		// 添加服务端是否支持新协议的缓存
		Map<String, Boolean> protocolInfoMap = referencedServiceProtocols.get(serviceAddress);
		if ( protocolInfoMap == null ) {
			protocolInfoMap = new ConcurrentHashMap<String, Boolean>();
			Map<String, Boolean> oldProtocolInfoMap
					= referencedServiceProtocols.putIfAbsent(serviceAddress, protocolInfoMap);
			if(oldProtocolInfoMap != null) {
				protocolInfoMap = oldProtocolInfoMap;
			}
		}
		// invoker读取注册中心的协议信息并且put进去
		boolean support = false;
		try {
			support = isSupportNewProtocol(serviceAddress, serviceName, false);
		} catch (RegistryException e) {
			logger.error(e.getMessage(), e);
		}
		protocolInfoMap.put(serviceName, support);

		if (!referencedAddresses.containsKey(serviceAddress)) {
			referencedAddresses.put(serviceAddress, hostInfo);
			String app = null;
			String version = null;

			List<String> checkAppList = Lists.newArrayList();
			List<String> checkVersionList = Lists.newArrayList();

			for (Registry registry : registryList) {
				// Todo 两个注册中心获取到本地内存
				try {
					checkAppList.add(registry.getServerApp(serviceAddress));
				} catch (Throwable e) {
					logger.error("failed to get appname for " + serviceAddress, e);
				}

				try {
					checkVersionList.add(registry.getServerVersion(serviceAddress));
				} catch (Throwable e) {
					logger.error("failed to get version for " + serviceAddress, e);
				}
			}

			if(checkAppList.size() > 0) {
				app = checkValueConsistency(checkAppList);
			}
			if(checkVersionList.size() > 0) {
				version = checkValueConsistency(checkVersionList);
			}

			hostInfo.setVersion(version);
			hostInfo.setApp(app);
		}
	}

	public void removeServiceAddress(String serviceName, HostInfo hostInfo) {
		Set<HostInfo> hostInfos = referencedServiceAddresses.get(serviceName);
		if (hostInfos == null || !hostInfos.contains(hostInfo)) {
			logger.info("address:" + hostInfo + " is not in address list of service " + serviceName);
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
		for (String key : referencedServiceAddresses.keySet()) {
			Set<HostInfo> hostInfos = referencedServiceAddresses.get(key);
			if (hostInfos.contains(hostInfo)) {
				logger.info("address:" + hostInfo + " still been referenced for service:" + key);
				return true;
			}
		}
		return false;
	}

	public Set<HostInfo> getReferencedServiceAddresses(String serviceName) {
		Set<HostInfo> hostInfos = referencedServiceAddresses.get(serviceName);
		if (hostInfos == null || hostInfos.size() == 0) {
			logger.info("empty address list for service:" + serviceName);
		}
		return hostInfos;
	}

	public Map<String, Set<HostInfo>> getAllReferencedServiceAddresses() {
		return referencedServiceAddresses;
	}

	public String getReferencedAppFromCache(String serverAddress) {
		HostInfo hostInfo = referencedAddresses.get(serverAddress);
		String app = null;
		if (hostInfo != null) {
			app = hostInfo.getApp();
			return app;
		}
		return "";
	}

	public String getReferencedApp(String serverAddress) {
		String app = null;

		List<String> checkList = Lists.newArrayList();
		for (Registry registry : registryList) {
			// Todo 两个注册中心获取到本地内存
			try {
				checkList.add(registry.getServerApp(serverAddress));
			} catch (Throwable e) {
				logger.error("failed to get appname for " + serverAddress, e);
			}
		}
		if(checkList.size() > 0) {
			app = checkValueConsistency(checkList);
		}

		setReferencedApp(serverAddress, app);
		return app;
	}

	public void setReferencedApp(String serverAddress, String app) {
		HostInfo hostInfo = referencedAddresses.get(serverAddress);
		if (hostInfo != null) {
			hostInfo.setApp(app);
		}
	}

	public void setServerApp(String serverAddress, String app) {
		for (Registry registry : registryList) {
			registry.setServerApp(serverAddress, app);
		}
	}

	public void unregisterServerApp(String serverAddress) {
		for (Registry registry : registryList) {
			registry.unregisterServerApp(serverAddress);
		}
	}

	public String getReferencedVersionFromCache(String serverAddress) {
		HostInfo hostInfo = referencedAddresses.get(serverAddress);
		String version = null;
		if (hostInfo != null) {
			version = hostInfo.getVersion();
			// if (version == null && registry != null) {
			// version = registry.getServerVersion(serverAddress);
			// hostInfo.setVersion(version);
			// }
			return version;
		}
		return null;
	}

	public String getReferencedVersion(String serverAddress) {
		String version = null;


		List<String> checkList = Lists.newArrayList();
		for (Registry registry : registryList) {
			// Todo 两个注册中心获取到本地内存
			try {
				checkList.add(registry.getServerVersion(serverAddress));
			} catch (Throwable e) {
				logger.error("failed to get version for " + serverAddress, e);
			}
		}
		if(checkList.size() > 0) {
			version = checkValueConsistency(checkList);
		}

		setReferencedVersion(serverAddress, version);
		return version;
	}

	public void setReferencedVersion(String serverAddress, String version) {
		HostInfo hostInfo = referencedAddresses.get(serverAddress);
		if (hostInfo != null) {
			hostInfo.setVersion(version);
		}
	}

	public void setServerVersion(String serverAddress, String version) {
		for (Registry registry : registryList) {
			registry.setServerVersion(serverAddress, version);
		}
	}

	public void unregisterServerVersion(String serverAddress) {
		for (Registry registry : registryList) {
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

		@Override
		public void onServerVersionChange(String serverAddress, String version) {
			HostInfo hostInfo = referencedAddresses.get(serverAddress);
			if (hostInfo != null) {
				hostInfo.setVersion(version);
			}
		}

		@Override
		public void onServerProtocolChange(String serverAddress, Map<String, Boolean> protocolInfoMap) {
			// 更新invoker缓存的服务端协议详情
			referencedServiceProtocols.put(serverAddress, protocolInfoMap);
		}

	}

	/**
	 * 
	 * @author chenchongze
	 * @param serviceName
	 * @param group
	 * @param hosts
	 */
	public void setServerService(String serviceName, String group, String hosts) throws RegistryException {
		//TODO 待验证
		for (Registry registry : registryList) {
			registry.setServerService(serviceName, group, hosts);
		}

		monitor.logEvent("PigeonService.setHosts", serviceName, "swimlane=" + group + "&hosts=" + hosts);
	}

	public void delServerService(String serviceName, String group) throws RegistryException {
		//TODO 待验证
		for (Registry registry : registryList) {
			registry.delServerService(serviceName, group);
		}

		monitor.logEvent("PigeonService.delService", serviceName, "swimlane=" + group);
	}

	public void updateHeartBeat(String serviceAddress, Long heartBeatTimeMillis) {
		for (Registry registry : registryList) {
			registry.updateHeartBeat(serviceAddress, heartBeatTimeMillis);
		}
	}

	public void deleteHeartBeat(String serviceAddress) {
		for (Registry registry : registryList) {
			registry.deleteHeartBeat(serviceAddress);
		}
	}

	private String mergeAddress(String address, String anotherAddress) {
		Set<String> result = new HashSet<String>(Arrays.asList(address.split(",")));
		result.addAll(Arrays.asList(anotherAddress.split(",")));

		return StringUtils.join(result,",");
	}

	public boolean isSupportNewProtocol(String serverAddress) {
		return VersionUtils.compareVersion(getReferencedVersion(serverAddress), "2.7.8") >=0;
	}

	public boolean isSupportNewProtocol(String serviceAddress, String serviceName, boolean readCache)
			throws RegistryException {
		if(readCache) {
			Map<String, Boolean> protocolInfoMap = referencedServiceProtocols.get(serviceAddress);
			if (protocolInfoMap != null && protocolInfoMap.containsKey(serviceName)) {
				return protocolInfoMap.get(serviceName);
			}
		}

		boolean support = false;

		for (Registry registry : registryList) {
			// Todo 两个注册中心获取到本地内存
			try {
				if (registry.getName().equals(Constants.REGISTRY_CURATOR_NAME)) {
					support = registry.isSupportNewProtocol(serviceAddress, serviceName);
				}
				if (registry.getName().equals(Constants.REGISTRY_MNS_NAME)) {
					support = registry.isSupportNewProtocol(serviceAddress, serviceName);
					break; // 有mns weight时，以mns为主
				}
			} catch (Throwable e) {
				logger.error("failed to get protocol for " + serviceAddress + "#" + serviceName, e);
			}
		}

		Map<String, Boolean> protocolInfoMap = referencedServiceProtocols.get(serviceAddress);
		if (protocolInfoMap != null) {
			protocolInfoMap.put(serviceName, support);
		}

		return support;
	}

	/**
	 * For provider to register protocol to registry center.
	 * @param serviceAddress
	 * @param serviceName
	 * @param support
	 * @throws RegistryException
	 */
	public void registerSupportNewProtocol(String serviceAddress, String serviceName, boolean support)
			throws RegistryException {
		for (Registry registry : registryList) {
			registry.setSupportNewProtocol(serviceAddress, serviceName, support);
		}
		monitor.logEvent("PigeonService.protocol", serviceName, "support=" + support);
	}

	public void unregisterSupportNewProtocol(String serviceAddress, String serviceName) throws RegistryException {
		for (Registry registry : registryList) {
			registry.unregisterSupportNewProtocol(serviceAddress, serviceName);
		}
		monitor.logEvent("PigeonService.protocol", serviceName, "unregister");
	}

	public boolean getReferencedProtocol(String serverAddress, String serviceName) {
		boolean support = false;

		List<Boolean> checkList = Lists.newArrayList();
		for (Registry registry : registryList) {
			// Todo 两个注册中心获取到本地内存
			try {
				checkList.add(registry.isSupportNewProtocol(serverAddress, serviceName));
			} catch (Throwable e) {
				logger.error("failed to get protocol for " + serverAddress + "#" + serviceName, e);
			}
		}
		if(checkList.size() > 0) {
			support = checkValueConsistency(checkList);
		}

		setReferencedProtocol(serverAddress, serviceName, support);
		return support;
	}

	private void setReferencedProtocol(String serverAddress, String serviceName, boolean support) {
		Map<String, Boolean> infoMap = referencedServiceProtocols.get(serverAddress);
		if(infoMap != null) {
			infoMap.put(serviceName, support);
		}
	}

	private class InnerConfigChangeListener implements ConfigChangeListener {

		@Override
		public void onKeyUpdated(String key, String value) {
			if (key.endsWith(KEY_PIGEON_REGISTRY_PREFER)) {
				try {
					parseRegistryConfig(ExtensionLoader.getExtensionList(Registry.class), value);
				} catch (Throwable t) {
					logger.error(t);
				}
			}
		}

		@Override
		public void onKeyAdded(String key, String value) {

		}

		@Override
		public void onKeyRemoved(String key) {

		}
	}
}
