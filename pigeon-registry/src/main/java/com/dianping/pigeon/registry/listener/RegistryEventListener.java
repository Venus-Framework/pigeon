/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.registry.listener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.dianping.pigeon.log.Logger;

import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.registry.RegistryManager;

/**
 * 将lion推送的动态服务信息发送到感兴趣的listener
 * 
 * @author marsqing
 * 
 */
public class RegistryEventListener {

	private static final Logger logger = LoggerLoader.getLogger(RegistryEventListener.class);

	private static List<ServiceProviderChangeListener> serviceProviderChangeListeners = new ArrayList<ServiceProviderChangeListener>();

	private static List<RegistryConnectionListener> registryConnectionListeners = new ArrayList<RegistryConnectionListener>();

	private static List<ServerInfoListener> serverInfoListeners = new ArrayList<ServerInfoListener>();

	public synchronized static void addListener(ServiceProviderChangeListener listener) {
		serviceProviderChangeListeners.add(listener);
	}

	public synchronized static void removeListener(ServiceProviderChangeListener listener) {
		serviceProviderChangeListeners.remove(listener);
	}

	public synchronized static void addListener(RegistryConnectionListener listener) {
		registryConnectionListeners.add(listener);
	}

	public synchronized static void addListener(ServerInfoListener listener) {
		serverInfoListeners.add(listener);
	}

	public static void providerRemoved(String serviceName, String host, int port) {
		List<ServiceProviderChangeListener> listeners = new ArrayList<ServiceProviderChangeListener>();
		listeners.addAll(serviceProviderChangeListeners);
		for (ServiceProviderChangeListener listener : listeners) {
			listener.providerRemoved(new ServiceProviderChangeEvent(serviceName, host, port, -1));
		}
	}

	public static void providerAdded(String serviceName, String host, int port, int weight) {
		List<ServiceProviderChangeListener> listeners = new ArrayList<ServiceProviderChangeListener>();
		listeners.addAll(serviceProviderChangeListeners);
		for (ServiceProviderChangeListener listener : listeners) {
			ServiceProviderChangeEvent event = new ServiceProviderChangeEvent(serviceName, host, port, weight);
			listener.providerAdded(event);
		}
	}

	public static void hostWeightChanged(String host, int port, int weight) {
		List<ServiceProviderChangeListener> listeners = new ArrayList<ServiceProviderChangeListener>();
		listeners.addAll(serviceProviderChangeListeners);
		for (ServiceProviderChangeListener listener : listeners) {
			listener.hostWeightChanged(new ServiceProviderChangeEvent(null, host, port, weight));
		}
	}

	public static void connectionReconnected() {
		for (RegistryConnectionListener listener : registryConnectionListeners) {
			listener.reconnected();
		}
	}

	public static void serverAppChanged(String serverAddress, String app) {
		for (ServerInfoListener listener : serverInfoListeners) {
			listener.onServerAppChange(serverAddress, app);
		}
	}

	public static void serverVersionChanged(String serverAddress, String version) {
		for (ServerInfoListener listener : serverInfoListeners) {
			listener.onServerVersionChange(serverAddress, version);
		}
	}

	public static void serverInfoChanged(String serviceName, String serverAddress) {
		RegistryManager.getInstance().getReferencedApp(serverAddress);
		RegistryManager.getInstance().getReferencedVersion(serverAddress);
		RegistryManager.getInstance().getReferencedProtocol(serverAddress, serviceName);
	}

	public static void serverProtocolChanged(String serverAddress, Map<String, Boolean> protocolInfoMap) {
		for (ServerInfoListener listener : serverInfoListeners) {
			listener.onServerProtocolChange(serverAddress, protocolInfoMap);
		}
	}
}
