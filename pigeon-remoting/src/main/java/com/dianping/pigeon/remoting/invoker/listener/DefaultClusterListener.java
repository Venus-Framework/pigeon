/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.invoker.listener;

import com.dianping.pigeon.config.ConfigManagerLoader;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.remoting.invoker.Client;
import com.dianping.pigeon.remoting.invoker.ClientSelector;
import com.dianping.pigeon.remoting.invoker.config.InvokerConfig;
import com.dianping.pigeon.remoting.invoker.domain.ConnectInfo;
import com.dianping.pigeon.remoting.invoker.exception.ServiceUnavailableException;
import com.dianping.pigeon.threadpool.DefaultThreadFactory;
import com.dianping.pigeon.util.CollectionUtils;
import com.dianping.pigeon.util.ThreadPoolUtils;
import org.apache.logging.log4j.Logger;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class DefaultClusterListener implements ClusterListener {

	private static final Logger logger = LoggerLoader.getLogger(DefaultClusterListener.class);

	private ConcurrentHashMap<String, List<Client>> serviceClients = new ConcurrentHashMap<String, List<Client>>();

	private ConcurrentHashMap<String, Client> allClients = new ConcurrentHashMap<String, Client>();

	private HeartBeatListener heartbeatListener;

	private ReconnectListener reconnectListener;

	private ScheduledThreadPoolExecutor closeExecutor = new ScheduledThreadPoolExecutor(3, new DefaultThreadFactory(
			"Pigeon-Client-Cache-Close-ThreadPool"));

	private ClusterListenerManager clusterListenerManager = ClusterListenerManager.getInstance();

	public DefaultClusterListener(HeartBeatListener heartbeatListener, ReconnectListener reconnectListener,
			ProviderAvailableListener providerAvailableListener) {
		this.heartbeatListener = heartbeatListener;
		this.reconnectListener = reconnectListener;
		this.reconnectListener.setWorkingClients(serviceClients);
		this.heartbeatListener.setWorkingClients(serviceClients);
		providerAvailableListener.setWorkingClients(serviceClients);
	}

	public void clear() {
		serviceClients = new ConcurrentHashMap<String, List<Client>>();
		allClients = new ConcurrentHashMap<String, Client>();
	}

	public ConcurrentHashMap<String, List<Client>> getServiceClients() {
		return serviceClients;
	}

	public List<Client> getClientList(InvokerConfig<?> invokerConfig) {
		List<Client> clientList = this.serviceClients.get(invokerConfig.getUrl());
		if (CollectionUtils.isEmpty(clientList)) {
			throw new ServiceUnavailableException("no available provider for service:" + invokerConfig.getUrl()
					+ ", group:" + invokerConfig.getGroup() + ", env:"
					+ ConfigManagerLoader.getConfigManager().getEnv());
		}
		return clientList;
	}

	@Override
	public void addConnect(ConnectInfo connectInfo) {
		if (logger.isInfoEnabled()) {
			logger.info("[cluster-listener] add service provider:" + connectInfo);
		}
		Client client = this.allClients.get(connectInfo.getConnect());
		if (clientExisted(connectInfo)) {
			if (client != null) {
				for (List<Client> clientList : serviceClients.values()) {
					int idx = clientList.indexOf(client);
					if (idx >= 0 && clientList.get(idx) != client) {
						closeClientInFuture(client);
					}
				}
			} else {
				return;
			}
		}
		if (client == null) {
			client = ClientSelector.selectClient(connectInfo);
		}
		if (!this.allClients.containsKey(connectInfo.getConnect())) {
			Client oldClient = this.allClients.putIfAbsent(connectInfo.getConnect(), client);
			if (oldClient != null) {
				client = oldClient;
			}
		}
		try {
			if (!client.isConnected()) {
				client.connect();
			} else {
				logger.info("client already connected:" + client);
			}
			if (client.isConnected()) {

				for (Entry<String, Integer> sw : connectInfo.getServiceNames().entrySet()) {
					String serviceName = sw.getKey();
					List<Client> clientList = this.serviceClients.get(serviceName);
					if (clientList == null) {
						clientList = new CopyOnWriteArrayList<Client>();
						List<Client> oldClientList = this.serviceClients.putIfAbsent(serviceName, clientList);
						if (oldClientList != null) {
							clientList = oldClientList;
						}
					}
					if (!clientList.contains(client)) {
						clientList.add(client);
					}
				}
			} else {
				logger.info("[cluster-listener] remove client:" + client);
				clusterListenerManager.removeConnect(client);
			}
		} catch (Throwable e) {
			logger.error("", e);
		}
	}

	private boolean clientExisted(ConnectInfo connectInfo) {
		for (String serviceName : connectInfo.getServiceNames().keySet()) {
			List<Client> clientList = serviceClients.get(serviceName);
			if (clientList != null) {
				for (Client client : clientList) {
					if (client != null && client.getAddress().equals(connectInfo.getConnect())) {
						return true;
					}
				}
			}
		}
		return false;
	}

	@Override
	public void removeConnect(Client client) {
		if (logger.isInfoEnabled()) {
			logger.info("[cluster-listener] remove service provider:" + client);
		}
		for (String serviceName : this.serviceClients.keySet()) {
			List<Client> clientList = this.serviceClients.get(serviceName);
			if (clientList != null && clientList.contains(client)) {
				clientList.remove(client);
			}
		}
	}

	@Override
	public void doNotUse(String serviceName, String host, int port) {
		if (logger.isInfoEnabled()) {
			logger.info("[cluster-listener] do not use service provider:" + serviceName + ":" + host + ":" + port);
		}
		List<Client> cs = serviceClients.get(serviceName);
		List<Client> newCS = new CopyOnWriteArrayList<Client>();
		if (cs != null && !cs.isEmpty()) {
			newCS.addAll(cs);
		}
		Client clientFound = null;
		for (Client client : cs) {
			if (client != null && client.getHost() != null && client.getHost().equals(host) && client.getPort() == port) {
				newCS.remove(client);
				clientFound = client;
			}
		}
		serviceClients.put(serviceName, newCS);

		// 一个client可能对应多个serviceName，仅当client不被任何serviceName使用时才关闭
		if (clientFound != null) {
			if (!isClientInUse(clientFound)) {
				removeClientFromReconnectTask(clientFound);
				allClients.remove(clientFound.getAddress());
				closeClientInFuture(clientFound);
			}
		}
	}

	// move to HeartTask?
	private void removeClientFromReconnectTask(Client clientToRemove) {
		Map<String, Client> closedClients = reconnectListener.getClosedClients();
		Set<String> keySet = closedClients.keySet();
		Iterator<String> iterator = keySet.iterator();
		while (iterator.hasNext()) {
			String connect = iterator.next();
			Client client = closedClients.get(connect);
			if (client != null && client.equals(clientToRemove)) {
				iterator.remove();
			}
		}
	}

	private boolean isClientInUse(Client clientToFind) {
		for (List<Client> clientList : serviceClients.values()) {
			if (clientList.contains(clientToFind)) {
				return true;
			}
		}
		return false;
	}

	private void closeClientInFuture(final Client client) {
		Runnable command = new Runnable() {

			@Override
			public void run() {
				client.close();
				logger.info("close client:" + client.getAddress());
			}

		};
		try {
			closeExecutor.schedule(command, 3000, TimeUnit.MILLISECONDS);
		} catch (Throwable e) {
			logger.error("error schedule task to close client", e);
		}
	}

	public void destroy() throws Exception {
		ThreadPoolUtils.shutdown(closeExecutor);
	}

	public ConcurrentHashMap<String, Client> getAllClients() {
		return allClients;
	}
}
