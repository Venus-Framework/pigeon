/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.invoker.listener;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.log4j.Logger;

import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.extension.ExtensionLoader;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.registry.RegistryManager;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.invoker.Client;
import com.dianping.pigeon.remoting.invoker.domain.ConnectInfo;

public class ReconnectListener implements Runnable, ClusterListener {

	private static Logger logger = LoggerLoader.getLogger(ReconnectListener.class);

	private static ConcurrentMap<String, Client> closedClients = new ConcurrentHashMap<String, Client>();

	private final ClusterListenerManager clusterListenerManager = ClusterListenerManager.getInstance();

	private ConfigManager configManager = ExtensionLoader.getExtension(ConfigManager.class);

	private Map<String, List<Client>> workingClients;

	@Override
	public void run() {
		long interval = configManager.getLongValue(Constants.KEY_RECONNECT_INTERVAL,
				Constants.DEFAULT_RECONNECT_INTERVAL);
		long sleepTime = interval;
		while (!Thread.currentThread().isInterrupted()) {
			try {
				Thread.sleep(sleepTime);
				long now = System.currentTimeMillis();
				Set<String> toRemovedClients = new HashSet<String>();
				for (String providerUrl : closedClients.keySet()) {
					Client client = closedClients.get(providerUrl);
					if (logger.isDebugEnabled()) {
						logger.debug("[reconnect] checking service provider:" + client);
					}
					if (RegistryManager.getInstance().getServiceWeight(client.getAddress()) > 0) {
						if (!client.isConnected()) {
							try {
								client.connect();
							} catch (Throwable e) {
								logger.error("[reconnect] connect server[" + providerUrl + "] failed", e);
							}
						}
						if (client.isConnected()) {
							// 加回去时active设置为true
							clusterListenerManager.addConnect(client.getConnectInfo());
							client.setActive(true);
							toRemovedClients.add(providerUrl);
						}
					}
				}
				for (String providerUrl : toRemovedClients) {
					closedClients.remove(providerUrl);
				}
				sleepTime = interval - (System.currentTimeMillis() - now);
			} catch (Throwable e) {
				logger.error("[reconnect] task failed", e);
			} finally {
				if (sleepTime < 1000) {
					sleepTime = 1000;
				}
			}
		}
	}

	private String makeProviderUrl(String host, int port) {
		return host + ":" + port;
	}

	@Override
	public void addConnect(ConnectInfo cmd) {
	}

	@Override
	public void removeConnect(Client client) {
		if (logger.isInfoEnabled()) {
			logger.info("[reconnect] add service provider to reconnect listener:" + client);
		}
		closedClients.putIfAbsent(client.getConnectInfo().getConnect(), client);
	}

	public Map<String, List<Client>> getWorkingClients() {
		return workingClients;
	}

	public void setWorkingClients(Map<String, List<Client>> workingClients) {
		this.workingClients = workingClients;
	}

	@Override
	public void doNotUse(String serviceName, String host, int port) {
		String providerUrl = makeProviderUrl(host, port);
		if (logger.isInfoEnabled()) {
			logger.info("[reconnect] do not use service provider:" + providerUrl);
		}
		Client client = closedClients.get(providerUrl);
		boolean isClientInUse = false;
		for (List<Client> clientList : getWorkingClients().values()) {
			if (clientList.contains(client)) {
				isClientInUse = true;
				break;
			}
		}
		if (!isClientInUse) {
			closedClients.remove(providerUrl);
		}
	}

	public Map<String, Client> getClosedClients() {
		return closedClients;
	}

}
