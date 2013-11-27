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

import com.dianping.dpsf.exception.NetException;
import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.extension.ExtensionLoader;
import com.dianping.pigeon.monitor.LoggerLoader;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.invoker.Client;
import com.dianping.pigeon.remoting.invoker.component.ConnectInfo;

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
				Set<String> toRemovedConnect = new HashSet<String>();
				if (logger.isDebugEnabled()) {
					logger.debug("[reconnect] clients:" + closedClients);
				}
				for (String connect : closedClients.keySet()) {
					Client client = closedClients.get(connect);
					if (!client.isConnected()) {
						try {
							client.connect();
						} catch (NetException e) {
							logger.error("Connect server[" + client.getAddress() + "] failed, detail[" + e.getMessage()
									+ "].", e);
						}
					}
					if (client.isConnected()) {
						// 加回去时active设置为true
						clusterListenerManager.addConnect(connect, client);
						toRemovedConnect.add(connect);
					}
				}
				for (String connect : toRemovedConnect) {
					closedClients.remove(connect);
				}
				sleepTime = interval - (System.currentTimeMillis() - now);
			} catch (Exception e) {
				logger.error("Do reconnect task failed, detail[" + e.getMessage() + "].", e);
			} finally {
				if (sleepTime < 1000) {
					sleepTime = 1000;
				}
			}
		}
	}

	@Override
	public void addConnect(ConnectInfo cmd) {
	}

	@Override
	public void addConnect(ConnectInfo cmd, Client client) {
	}

	@Override
	public void removeConnect(Client client) {
		if (logger.isInfoEnabled()) {
			logger.info("[reconnect] remove client:" + client);
		}
		String connect = client.getConnectInfo().getConnect();
		closedClients.putIfAbsent(connect, client);
	}

	@Override
	public void doNotUse(String serviceName, String host, int port) {
		String connect = host + ":" + port;
		if (logger.isInfoEnabled()) {
			logger.info("[reconnect] do not use client:" + connect + " for service:" + serviceName);
		}
		Client client = closedClients.get(connect);
		boolean isClientInUse = false;
		for (List<Client> clientList : workingClients.values()) {
			if (clientList.contains(client)) {
				isClientInUse = true;
			}
		}
		if (!isClientInUse) {
			closedClients.remove(connect);
		}
	}

	public Map<String, Client> getClosedClients() {
		return closedClients;
	}

	public void setWorkingClients(Map<String, List<Client>> workingClients) {
		this.workingClients = workingClients;
	}

}
