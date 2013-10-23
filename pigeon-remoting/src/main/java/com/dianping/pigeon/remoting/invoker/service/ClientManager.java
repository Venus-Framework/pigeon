/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.invoker.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.dianping.dpsf.exception.ServiceException;
import com.dianping.pigeon.component.HostInfo;
import com.dianping.pigeon.component.invocation.InvocationRequest;
import com.dianping.pigeon.component.invocation.InvocationResponse;
import com.dianping.pigeon.component.phase.Disposable;
import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.extension.ExtensionLoader;
import com.dianping.pigeon.registry.cache.RegistryCache;
import com.dianping.pigeon.registry.cache.ServiceCache;
import com.dianping.pigeon.registry.cache.WeightCache;
import com.dianping.pigeon.registry.listener.RegistryEventListener;
import com.dianping.pigeon.registry.listener.ServiceProviderChangeEvent;
import com.dianping.pigeon.registry.listener.ServiceProviderChangeListener;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.invoker.Client;
import com.dianping.pigeon.remoting.invoker.component.ConnectInfo;
import com.dianping.pigeon.remoting.invoker.component.InvokerMetaData;
import com.dianping.pigeon.remoting.invoker.listener.ClusterListenerManager;
import com.dianping.pigeon.remoting.invoker.listener.DefaultClusterListener;
import com.dianping.pigeon.remoting.invoker.listener.HeartBeatListener;
import com.dianping.pigeon.remoting.invoker.listener.ReconnectListener;
import com.dianping.pigeon.remoting.invoker.route.RouteManager;
import com.dianping.pigeon.threadpool.DefaultThreadPool;
import com.dianping.pigeon.threadpool.ThreadPool;

public class ClientManager implements Disposable {

	private static final Logger logger = Logger.getLogger(ClientManager.class);

	private ClusterListenerManager clusterListenerManager = ClusterListenerManager.getInstance();

	private DefaultClusterListener clusterListener;

	private HeartBeatListener heartBeatTask;

	private ReconnectListener reconnectTask;

	private RouteManager routerManager = ExtensionLoader.getExtension(RouteManager.class);

	private ConfigManager configManager = ExtensionLoader.getExtension(ConfigManager.class);

	private ServiceProviderChangeListener providerChangeListener = new InnerServiceProviderChangeListener();

	private static ThreadPool heartBeatThreadPool = new DefaultThreadPool("Pigeon-Client-Heartbeat-ThreadPool");

	private static ThreadPool reconnectThreadPool = new DefaultThreadPool("Pigeon-Client-Reconnect-ThreadPool");

	private static ClientManager instance = new ClientManager();

	public static ClientManager getInstance() {
		return instance;
	}

	/**
	 * 
	 * @param invocationRepository
	 */
	private ClientManager() {
		this.heartBeatTask = new HeartBeatListener();
		this.reconnectTask = new ReconnectListener();
		this.clusterListener = new DefaultClusterListener(heartBeatTask, reconnectTask);
		this.clusterListenerManager.addListener(this.clusterListener);
		this.clusterListenerManager.addListener(this.heartBeatTask);
		this.clusterListenerManager.addListener(this.reconnectTask);
		heartBeatThreadPool.execute(this.heartBeatTask);
		reconnectThreadPool.execute(this.reconnectTask);

		// Disable thread renaming of Netty
		// ThreadRenamingRunnable.setThreadNameDeterminer(ThreadNameDeterminer.CURRENT);

		RegistryEventListener.addListener(providerChangeListener);

	}

	public synchronized void registerClient(String serviceName, String group, String connect, int weight) {

		this.clusterListenerManager.addConnect(new ConnectInfo(serviceName, connect, weight));

		// TODO
		ServiceCache.serviceNameAndWeights.put(serviceName, weight);

		WeightCache.getInstance().registerWeight(serviceName, group, connect, weight);
		ServiceCache.serviceNameToGroup.put(serviceName, group);
		Set<HostInfo> hpSet = ServiceCache.serviceNameToHostInfos.get(serviceName);
		HostInfo hostInfo = new HostInfo(connect, weight);
		if (hpSet == null) {
			hpSet = new HashSet<HostInfo>();
			hpSet.add(hostInfo);
			ServiceCache.serviceNameToHostInfos.put(serviceName, hpSet);
		} else {
			hpSet.add(hostInfo);
		}
	}

	public Client getClient(InvokerMetaData metaData, InvocationRequest request, List<Client> excludeClients) {

		List<Client> clientList = clusterListener.getClientList(metaData.getServiceName());
		List<Client> clientsToRoute = new ArrayList<Client>(clientList);
		if (excludeClients != null) {
			clientsToRoute.removeAll(excludeClients);
		}
		return routerManager.route(clientsToRoute, metaData, request);
	}

	public void processResponse(InvocationResponse response, Client client) throws ServiceException {
		if (response.getMessageType() == Constants.MESSAGE_TYPE_HEART) {
			this.heartBeatTask.processResponse(response, client);
		} else {
			ServiceInvocationRepository.getInstance().receiveResponse(response);
		}

	}

	@Override
	public void destroy() {
		if (clusterListenerManager instanceof Disposable) {
			((Disposable) clusterListenerManager).destroy();
		}
		if (routerManager instanceof Disposable) {
			((Disposable) routerManager).destroy();
		}
		RegistryEventListener.removeListener(providerChangeListener);
	}

	/**
	 * 用Lion从ZK中获取serviceName对应的服务地址，并注册这些服务地址
	 */
	public synchronized void findAndRegisterClientFor(String serviceName, String group, String vip) {
		if (logger.isInfoEnabled()) {
			logger.info("try to find the address form zookeeper，serviceName:" + serviceName + ",group:" + group);
		}
		String addressStr = null;
		try {
			// TODO, 想要删除....
			// ConfigCache.getInstance();
			if (!StringUtils.isBlank(vip) && "dev".equals(configManager.getEnv())) {
				addressStr = vip;
			} else {
				addressStr = RegistryCache.getInstance().getServiceAddress(serviceName);
				if (addressStr == null && !StringUtils.isBlank(vip)) {
					addressStr = vip;
				}
			}
		} catch (Exception e) {
			if (StringUtils.isBlank(vip)) {
				logger.error("cannot get service client info for serviceName=" + serviceName + " no failover vip");
				throw new RuntimeException(e);
			} else {
				logger.error("cannot get service client info for serviceName=" + serviceName + " use failover vip= "
						+ vip + " instead", e);
				addressStr = vip;
			}
		}
		if (StringUtils.isBlank(addressStr)) {
			throw new RuntimeException("no service address found for service:" + serviceName + ",group:" + group
					+ ",vip:" + vip);
		}
		if (logger.isInfoEnabled()) {
			logger.info("selected service address is:" + addressStr + " with service:" + serviceName + ",group:"
					+ group + ",vip:" + vip);
		}
		ServiceCache.serviceNameToGroup.put(serviceName, group);
		addressStr = addressStr.trim();
		String[] addressList = addressStr.split(",");
		for (int i = 0; i < addressList.length; i++) {
			if (StringUtils.isNotBlank(addressList[i])) {
				String[] parts = addressList[i].split(":");
				String host = parts[0];

				Integer weight = WeightCache.getInstance().getWeight(serviceName, addressList[i]);

				if (weight == null) {
					try {
						weight = RegistryCache.getInstance().getServiceWeigth(addressList[i]);
					} catch (Exception e) {
						throw new RuntimeException("error while getting service weight:" + addressList[i], e);
					}
				}
				int port = Integer.parseInt(parts[1]);
				RegistryEventListener.providerAdded(serviceName, host, port, weight);
			}
		}
	}

	public Map<String, Set<HostInfo>> getServiceHostInfos() {
		return ServiceCache.serviceNameToHostInfos;
	}

	/**
	 * @return the clusterListener
	 */
	public DefaultClusterListener getClusterListener() {
		return clusterListener;
	}

	/**
	 * @return the heartTask
	 */
	public HeartBeatListener getHeartTask() {
		return heartBeatTask;
	}

	public ReconnectListener getReconnectTask() {
		return reconnectTask;
	}

	class InnerServiceProviderChangeListener implements ServiceProviderChangeListener {
		@Override
		public void providerAdded(ServiceProviderChangeEvent event) {
			String group = ServiceCache.serviceNameToGroup.get(event.getServiceName());
			if (group == null) {
				logger.error("can not map serviceName=" + event.getServiceName() + " to group");
				return;
			}
			if (logger.isInfoEnabled()) {
				logger.info("add " + event.getHost() + ":" + event.getPort() + " to " + event.getServiceName());
			}
			registerClient(event.getServiceName(), group, event.getHost() + ":" + event.getPort(), event.getWeight());
		}

		@Override
		public void providerRemoved(ServiceProviderChangeEvent event) {
			Set<HostInfo> hostInfoSet = ServiceCache.serviceNameToHostInfos.get(event.getServiceName());
			if (hostInfoSet != null) {
				hostInfoSet.remove(new HostInfo(event.getHost(), event.getPort(), event.getWeight()));
			}
		}

		@Override
		public void hostWeightChanged(ServiceProviderChangeEvent event) {
		}
	}

	public void clear() {
		clusterListener.clear();
	}

}
