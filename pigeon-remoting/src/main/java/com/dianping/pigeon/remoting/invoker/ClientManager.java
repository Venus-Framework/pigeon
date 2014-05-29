/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.invoker;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.dianping.dpsf.exception.DPSFException;
import com.dianping.dpsf.exception.NoConnectionException;
import com.dianping.pigeon.config.ConfigConstants;
import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.domain.HostInfo;
import com.dianping.pigeon.domain.phase.Disposable;
import com.dianping.pigeon.extension.ExtensionLoader;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.registry.RegistryManager;
import com.dianping.pigeon.registry.listener.RegistryEventListener;
import com.dianping.pigeon.registry.listener.ServiceProviderChangeEvent;
import com.dianping.pigeon.registry.listener.ServiceProviderChangeListener;
import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.invoker.config.InvokerConfig;
import com.dianping.pigeon.remoting.invoker.domain.ConnectInfo;
import com.dianping.pigeon.remoting.invoker.listener.ClusterListenerManager;
import com.dianping.pigeon.remoting.invoker.listener.DefaultClusterListener;
import com.dianping.pigeon.remoting.invoker.listener.HeartBeatListener;
import com.dianping.pigeon.remoting.invoker.listener.ReconnectListener;
import com.dianping.pigeon.remoting.invoker.route.RouteManager;
import com.dianping.pigeon.threadpool.DefaultThreadPool;
import com.dianping.pigeon.threadpool.ThreadPool;

public class ClientManager implements Disposable {

	private static final Logger logger = LoggerLoader.getLogger(ClientManager.class);

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
		RegistryEventListener.addListener(providerChangeListener);
	}

	public synchronized void registerClient(String serviceName, String host, int port, int weight) {
		ConnectInfo connectInfo = new ConnectInfo(serviceName, host, port, weight);
		this.clusterListenerManager.addConnect(connectInfo);
		RegistryManager.getInstance().addServiceServer(serviceName, host, port, weight);
	}

	public Client getClient(InvokerConfig<?> invokerConfig, InvocationRequest request, List<Client> excludeClients) {
		List<Client> clientList = clusterListener.getClientList(invokerConfig);
		List<Client> clientsToRoute = new ArrayList<Client>(clientList);
		if (excludeClients != null) {
			clientsToRoute.removeAll(excludeClients);
		}
		return routerManager.route(clientsToRoute, invokerConfig, request);
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

	public void registerServiceInvokers(String serviceName, String group, String vip) {
		String serviceAddress = null;
		try {
			if (!StringUtils.isBlank(vip) && ConfigConstants.ENV_DEV.equalsIgnoreCase(configManager.getEnv())) {
				serviceAddress = vip;
			} else {
				serviceAddress = RegistryManager.getInstance().getServiceAddress(serviceName, group);
			}
		} catch (Exception e) {
			logger.error("cannot get service provider for service:" + serviceName);
			throw new DPSFException(e);
		}

		if (StringUtils.isBlank(serviceAddress)) {
			throw new NoConnectionException("no service provider found for service:" + serviceName + ",group:" + group
					+ ",vip:" + vip);
		}

		if (logger.isInfoEnabled()) {
			logger.info("selected service provider address is:" + serviceAddress + " with service:" + serviceName
					+ ",group:" + group + ",vip:" + vip);
		}

		serviceAddress = serviceAddress.trim();
		String[] addressArray = serviceAddress.split(",");
		// List<String> addressList = new ArrayList<String>();
		for (int i = 0; i < addressArray.length; i++) {
			if (StringUtils.isNotBlank(addressArray[i])) {
				// addressList.add(addressArray[i]);
				String address = addressArray[i];
				String[] parts = address.split(":");
				try {
					String host = parts[0];
					int port = Integer.parseInt(parts[1]);
					int weight = RegistryManager.getInstance().getServiceWeight(address);
					RegistryEventListener.providerAdded(serviceName, host, port, weight);
				} catch (Exception e) {
					throw new DPSFException("error while registering service invoker:" + serviceName + ", address:"
							+ address, e);
				}
			}
		}

		// final CountDownLatch latch = new CountDownLatch(addressList.size());
		// for (final String address : addressList) {
		// final String url = serviceName;
		// Runnable r = new Runnable() {
		//
		// @Override
		// public void run() {
		// String[] parts = address.split(":");
		// try {
		// String host = parts[0];
		// int port = Integer.parseInt(parts[1]);
		// int weight = RegistryManager.getInstance().getServiceWeight(address);
		// RegistryEventListener.providerAdded(url, host, port, weight);
		// latch.countDown();
		// } catch (Exception e) {
		// throw new RuntimeException("error while registering service invoker:"
		// + url + ", address:"
		// + address, e);
		// }
		// }
		//
		// };
		// registerServiceInvokerThreadPool.submit(r);
		// }
		// try {
		// latch.await(1000, TimeUnit.MILLISECONDS);
		// } catch (InterruptedException e) {
		// throw new
		// RuntimeException("error while registering service invokers:" +
		// serviceName, e);
		// }
	}

	public Map<String, Set<HostInfo>> getServiceHostInfos() {
		return RegistryManager.getInstance().getAllServiceServers();
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
			if (logger.isInfoEnabled()) {
				logger.info("add " + event.getHost() + ":" + event.getPort() + " to " + event.getServiceName());
			}
			registerClient(event.getServiceName(), event.getHost(), event.getPort(), event.getWeight());
		}

		@Override
		public void providerRemoved(ServiceProviderChangeEvent event) {
			HostInfo hostInfo = new HostInfo(event.getHost(), event.getPort(), event.getWeight());
			RegistryManager.getInstance().removeServiceServer(event.getServiceName(), hostInfo);
		}

		@Override
		public void hostWeightChanged(ServiceProviderChangeEvent event) {
		}
	}

	public void clear() {
		clusterListener.clear();
	}

}
