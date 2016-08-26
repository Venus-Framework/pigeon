/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.invoker.listener;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import com.dianping.pigeon.log.Logger;
import org.springframework.util.CollectionUtils;

import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.registry.listener.RegistryEventListener;
import com.dianping.pigeon.registry.listener.ServiceProviderChangeEvent;
import com.dianping.pigeon.registry.listener.ServiceProviderChangeListener;
import com.dianping.pigeon.remoting.common.domain.Disposable;
import com.dianping.pigeon.remoting.invoker.Client;
import com.dianping.pigeon.remoting.invoker.domain.ConnectInfo;

public class ClusterListenerManager implements Disposable {

	private static final Logger logger = LoggerLoader.getLogger(ClusterListenerManager.class);

	public static final String PLACEHOLDER = ":";

	private List<ClusterListener> listeners = new CopyOnWriteArrayList<ClusterListener>();

	private ServiceProviderChangeListener providerChangeListener = new InnerServiceProviderChangeListener();

	private ConcurrentHashMap<String, ConnectInfo> connectInfoMap = new ConcurrentHashMap<String, ConnectInfo>();

	private static ClusterListenerManager instance = new ClusterListenerManager();

	public static ClusterListenerManager getInstance() {
		return instance;
	}

	private ClusterListenerManager() {
		RegistryEventListener.addListener(providerChangeListener);
	}

	public void addConnect(ConnectInfo cmd) {
		ConnectInfo connectInfo = this.connectInfoMap.get(cmd.getConnect());
		if (connectInfo == null) {
			ConnectInfo oldConnectInfo = this.connectInfoMap.putIfAbsent(cmd.getConnect(), cmd);
			if (oldConnectInfo != null) {
				connectInfo = oldConnectInfo;
			}
		}
		if (connectInfo != null) {
			connectInfo.addServiceNames(cmd.getServiceNames());
			if (CollectionUtils.isEmpty(cmd.getServiceNames())) {
				if (logger.isInfoEnabled()) {
					logger.info("[cluster-listener-mgr] add services from:" + connectInfo);
				}
				cmd.addServiceNames(connectInfo.getServiceNames());
			}
		}
		for (ClusterListener listener : listeners) {
			listener.addConnect(cmd);
		}
	}

	public void removeConnect(Client client) {
		String connect = client.getConnectInfo().getConnect();
		ConnectInfo cmd = this.connectInfoMap.get(connect);
		if (cmd != null) {
			for (ClusterListener listener : listeners) {
				listener.removeConnect(client);
			}
		}
	}

	public void addListener(ClusterListener listener) {
		this.listeners.add(listener);
	}

	@Override
	public void destroy() throws Exception {
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
				logger.info("[cluster-listener-mgr] remove:" + connect + " from " + event.getServiceName());
			}
			ConnectInfo cmd = connectInfoMap.get(connect);
			if (cmd != null) {
				cmd.getServiceNames().remove(event.getServiceName());
				if (cmd.getServiceNames().size() == 0) {
					connectInfoMap.remove(connect);
				}
			}
			for (ClusterListener listener : listeners) {
				listener.doNotUse(event.getServiceName(), event.getHost(), event.getPort());
			}
		}
	}
}
