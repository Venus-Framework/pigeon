/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.extension;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

/**
 * 
 * @author xiangwu
 * @Sep 11, 2013
 * 
 */
public final class ExtensionLoader {

	private static final Logger logger = Logger.getLogger(ExtensionLoader.class);

	private static Map<Class<?>, Object> extensionMap = new ConcurrentHashMap<Class<?>, Object>();

	private static Map<Class<?>, List<?>> extensionListMap = new ConcurrentHashMap<Class<?>, List<?>>();

	public static <T> T getExtension(Class<T> clazz) {
		T extension = (T) extensionMap.get(clazz);
		if (extension == null) {
			ServiceLoader<T> serviceLoader = ServiceLoader.load(clazz);
			for (T service : serviceLoader) {
				extensionMap.put(clazz, service);
				return service;
			}
			logger.warn("no extension found for class:" + clazz.getName());
		}
		return extension;
	}

	public static <T> List<T> getExtensionList(Class<T> clazz) {
		List<T> extensions = (List<T>) extensionListMap.get(clazz);
		if (extensions == null) {
			ServiceLoader<T> serviceLoader = ServiceLoader.load(clazz);
			extensions = new ArrayList<T>();
			for (T service : serviceLoader) {
				extensions.add(service);
			}
			if (!extensions.isEmpty()) {
				extensionListMap.put(clazz, extensions);
			} else {
				logger.warn("no extension found for class:" + clazz.getName());
			}
		}
		return extensions;
	}
	//
	// public static final ThreadManager threadManager = new
	// ThreadManagerImpl();
	//
	// private static ServiceLoader<RouteManager> routeManagerLoader =
	// ServiceLoader.load(RouteManager.class);
	//
	// private static ServiceLoader<WeightManager> weightManagerLoader =
	// ServiceLoader.load(WeightManager.class);
	//
	// private static ServiceLoader<ClusterConfigure> clusterConfigureLoader =
	// ServiceLoader.load(ClusterConfigure.class);
	//
	// private static ServiceLoader<RegistryManager> registryManagerLoader =
	// ServiceLoader.load(RegistryManager.class);
	//
	// private static ServiceLoader<ConfigManager> configManagerLoader =
	// ServiceLoader.load(ConfigManager.class);
	// private static ServiceLoader<InvocationRepository>
	// invocationRepositoryLoader = ServiceLoader
	// .load(InvocationRepository.class);
	//
	// private static ServiceLoader<ClientManager> clientManagerLoader =
	// ServiceLoader.load(ClientManager.class);
	//
	// private static ServiceLoader<ServiceLogMonitor> serviceLogMonitorLoader =
	// ServiceLoader
	// .load(ServiceLogMonitor.class);
	//
	// public static final WeightManager weightManager = getWeightManager();
	//
	// public static final ClusterConfigure clusterConfigure =
	// getClusterConfigure();
	//
	// public static RouteManager routeManager = getRouteManager();
	//
	// public static final RegistryManager registryManager =
	// getRegistryManager();
	//
	// public static final ConfigManager configManager = getConfigManager();
	//
	// public static final InvocationRepository invocationRepository =
	// getInvocationRepository();
	//
	// public static final ClientManager clientManager = getClientManager();
	//
	// public static final ServiceManager serviceManager = new
	// ServiceManagerImpl();
	//
	// public static final EventManager eventManager = new EventManagerImpl();
	//
	// public static final ServiceLogMonitor serviceLogMonitor =
	// getServiceLogMonitor();
	//
	// private static ServiceLogMonitor getServiceLogMonitor() {
	//
	// for (ServiceLogMonitor serviceLogMonitor : serviceLogMonitorLoader) {
	//
	// return serviceLogMonitor;
	// }
	// return null;
	// }
	//
	// private static ClientManager getClientManager() {
	//
	// for (ClientManager clientManager : clientManagerLoader) {
	//
	// return clientManager;
	// }
	// return null;
	// }
	//
	// private static InvocationRepository getInvocationRepository() {
	//
	// for (InvocationRepository invocationRepository :
	// invocationRepositoryLoader) {
	//
	// return invocationRepository;
	// }
	// return null;
	// }
	//
	// private static ConfigManager getConfigManager() {
	//
	// for (ConfigManager configManager : configManagerLoader) {
	//
	// return configManager;
	// }
	// return null;
	// }
	//
	// private static RegistryManager getRegistryManager() {
	//
	// for (RegistryManager registryManager : registryManagerLoader) {
	//
	// return registryManager;
	// }
	// return null;
	// }
	//
	// private static RouteManager getRouteManager() {
	//
	// for (RouteManager routeManager : routeManagerLoader) {
	//
	// return routeManager;
	// }
	// return null;
	// }
	//
	// private static ClusterConfigure getClusterConfigure() {
	//
	// for (ClusterConfigure clusterConfigure : clusterConfigureLoader) {
	//
	// return clusterConfigure;
	// }
	// return null;
	// }
	//
	// private static WeightManager getWeightManager() {
	//
	// for (WeightManager weightManager : weightManagerLoader) {
	//
	// return weightManager;
	// }
	//
	// return null;
	// }
}
