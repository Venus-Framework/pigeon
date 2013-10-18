/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.registry.listener;

import java.util.ArrayList;
import java.util.List;

/**
 * 将lion推送的动态服务信息发送到感兴趣的listener
 * 
 * @author marsqing
 * 
 */
public class RegistryEventListener {

	private static List<ServiceProviderChangeListener> listeners = new ArrayList<ServiceProviderChangeListener>();

	public synchronized static void addListener(ServiceProviderChangeListener listener) {
		listeners.add(listener);
	}

	public synchronized static void removeListener(ServiceProviderChangeListener listener) {
		listeners.remove(listener);
	}

	public static void providerRemoved(String serviceName, String host, int port) {
		for (ServiceProviderChangeListener listener : listeners) {
			listener.providerRemoved(new ServiceProviderChangeEvent(serviceName, host, port, -1));
		}
	}

	public static void providerAdded(String serviceName, String host, int port, int weight) {
		for (ServiceProviderChangeListener listener : listeners) {
			listener.providerAdded(new ServiceProviderChangeEvent(serviceName, host, port, weight));
		}
	}

	public static void hostWeightChanged(String host, int port, int weight) {
		for (ServiceProviderChangeListener listener : listeners) {
			listener.hostWeightChanged(new ServiceProviderChangeEvent(null, host, port, weight));
		}
	}

}
