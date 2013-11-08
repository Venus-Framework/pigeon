/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.invoker.listener;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.dianping.pigeon.component.phase.Disposable;
import com.dianping.pigeon.monitor.Log4jLoader;
import com.dianping.pigeon.registry.listener.RegistryEventListener;
import com.dianping.pigeon.registry.listener.ServiceProviderChangeEvent;
import com.dianping.pigeon.registry.listener.ServiceProviderChangeListener;
import com.dianping.pigeon.remoting.invoker.Client;
import com.dianping.pigeon.remoting.invoker.component.ConnectInfo;

public class ClusterListenerManager implements Disposable {

	private static final Logger logger = Log4jLoader.getLogger(ClusterListenerManager.class);

	public static final String PLACEHOLDER = ":";

	private List<ClusterListener> listeners = new ArrayList<ClusterListener>();

	private ServiceProviderChangeListener providerChangeListener = new InnerServiceProviderChangeListener();

	private static ClusterListenerManager instance = new ClusterListenerManager();

	public static ClusterListenerManager getInstance() {
		return instance;
	}

	private ClusterListenerManager() {
		RegistryEventListener.addListener(providerChangeListener);
	}

	public synchronized void addConnect(String connect, Client client) {
		for (ClusterListener listener : listeners) {
			listener.addConnect(client.getConnectInfo(), client);
		}
	}

	public synchronized void addConnect(ConnectInfo cmd) {
		/**
		 * else { metaData.addServiceNames(cmd.getServiceNames()); }
		 **/
		for (ClusterListener listener : listeners) {
			listener.addConnect(cmd);
		}
	}

	public synchronized void removeConnect(Client client) {
		for (ClusterListener listener : listeners) {
			listener.removeConnect(client);
		}
	}

	public void addListener(ClusterListener listener) {
		this.listeners.add(listener);
	}

	@Override
	public void destroy() {
		RegistryEventListener.removeListener(providerChangeListener);
	}

	class InnerServiceProviderChangeListener implements ServiceProviderChangeListener {
		@Override
		public void hostWeightChanged(ServiceProviderChangeEvent event) {
		}

		@Override
		public void providerAdded(ServiceProviderChangeEvent event) {
		}

		@Override
		public void providerRemoved(ServiceProviderChangeEvent event) {
			// addConnect的逆操作
			String connect = event.getHost() + ":" + event.getPort();
			if (logger.isInfoEnabled()) {
				logger.info("remove " + connect + " from " + event.getServiceName());
			}
			
			for (ClusterListener listener : listeners) {
				listener.doNotUse(event.getServiceName(), event.getHost(), event.getPort());
			}
		}
	}
}
