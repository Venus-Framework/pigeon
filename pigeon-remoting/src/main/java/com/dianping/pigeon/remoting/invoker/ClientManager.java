/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.invoker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.dianping.pigeon.config.ConfigConstants;
import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.config.ConfigManagerLoader;
import com.dianping.pigeon.domain.HostInfo;
import com.dianping.pigeon.domain.phase.Disposable;
import com.dianping.pigeon.extension.ExtensionLoader;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.registry.RegistryManager;
import com.dianping.pigeon.registry.listener.RegistryConnectionListener;
import com.dianping.pigeon.registry.listener.RegistryEventListener;
import com.dianping.pigeon.registry.listener.ServiceProviderChangeEvent;
import com.dianping.pigeon.registry.listener.ServiceProviderChangeListener;
import com.dianping.pigeon.remoting.ServiceFactory;
import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.invoker.config.InvokerConfig;
import com.dianping.pigeon.remoting.invoker.domain.ConnectInfo;
import com.dianping.pigeon.remoting.invoker.exception.ServiceUnavailableException;
import com.dianping.pigeon.remoting.invoker.listener.ClusterListenerManager;
import com.dianping.pigeon.remoting.invoker.listener.DefaultClusterListener;
import com.dianping.pigeon.remoting.invoker.listener.HeartBeatListener;
import com.dianping.pigeon.remoting.invoker.listener.ProviderAvailableListener;
import com.dianping.pigeon.remoting.invoker.listener.ReconnectListener;
import com.dianping.pigeon.remoting.invoker.route.RouteManager;
import com.dianping.pigeon.threadpool.DefaultThreadPool;
import com.dianping.pigeon.threadpool.ThreadPool;
import com.dianping.pigeon.util.NetUtils;
import com.dianping.pigeon.util.ThreadPoolUtils;

public class ClientManager {

	private static final Logger logger = LoggerLoader.getLogger(ClientManager.class);

	private ClusterListenerManager clusterListenerManager = ClusterListenerManager.getInstance();

	private DefaultClusterListener clusterListener;

	private HeartBeatListener heartBeatTask;

	private ReconnectListener reconnectTask;

	private ProviderAvailableListener providerAvailableListener;

	private RouteManager routerManager = ExtensionLoader.getExtension(RouteManager.class);

	private ConfigManager configManager = ExtensionLoader.getExtension(ConfigManager.class);

	private ServiceProviderChangeListener providerChangeListener = new InnerServiceProviderChangeListener();

	private static ThreadPool heartBeatThreadPool = new DefaultThreadPool("Pigeon-Client-HeartBeat-ThreadPool");

	private static ThreadPool reconnectThreadPool = new DefaultThreadPool("Pigeon-Client-Reconnect-ThreadPool");

	private static ThreadPool providerAvailableThreadPool = new DefaultThreadPool(
			"Pigeon-Client-ProviderAvailable-ThreadPool");

	private static ClientManager instance = new ClientManager();

	private RegistryConnectionListener registryConnectionListener = new InnerRegistryConnectionListener();

	private static boolean reloadWeight = ConfigManagerLoader.getConfigManager().getBooleanValue(
			"pigeon.register.weight.reload", true);

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
		this.providerAvailableListener = new ProviderAvailableListener();
		this.clusterListener = new DefaultClusterListener(heartBeatTask, reconnectTask, providerAvailableListener);
		this.clusterListenerManager.addListener(this.clusterListener);
		this.clusterListenerManager.addListener(this.heartBeatTask);
		this.clusterListenerManager.addListener(this.reconnectTask);
		heartBeatThreadPool.execute(this.heartBeatTask);
		reconnectThreadPool.execute(this.reconnectTask);
		providerAvailableThreadPool.execute(this.providerAvailableListener);
		RegistryEventListener.addListener(providerChangeListener);
		RegistryEventListener.addListener(registryConnectionListener);
	}

	public synchronized void registerClient(String serviceName, String host, int port, int weight) {
		ConnectInfo connectInfo = new ConnectInfo(serviceName, host, port, weight);
		this.clusterListenerManager.addConnect(connectInfo);
		RegistryManager.getInstance().addServiceAddress(serviceName, host, port, weight);
	}

	public Client getClient(InvokerConfig<?> invokerConfig, InvocationRequest request, List<Client> excludeClients) {
		List<Client> clientList = clusterListener.getClientList(invokerConfig);
		List<Client> clientsToRoute = new ArrayList<Client>(clientList);
		if (excludeClients != null) {
			clientsToRoute.removeAll(excludeClients);
		}
		return routerManager.route(clientsToRoute, invokerConfig, request);
	}

	public void destroy() throws Exception {
		if (clusterListenerManager instanceof Disposable) {
			((Disposable) clusterListenerManager).destroy();
		}
		if (routerManager instanceof Disposable) {
			((Disposable) routerManager).destroy();
		}
		RegistryEventListener.removeListener(providerChangeListener);
		ThreadPoolUtils.shutdown(providerAvailableThreadPool.getExecutor());
		ThreadPoolUtils.shutdown(heartBeatThreadPool.getExecutor());
		ThreadPoolUtils.shutdown(reconnectThreadPool.getExecutor());
	}

	public String getServiceAddress(String serviceName, String group, String vip) {
		if (!RegistryManager.isInitialized()) {
			logger.info("waitting for registry initialized");
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
		}
		String serviceAddress = null;
		boolean useVip = false;
		if (StringUtils.isNotBlank(vip)) {
			if ((ConfigConstants.ENV_DEV.equalsIgnoreCase(configManager.getEnv()) || ConfigConstants.ENV_ALPHA
					.equalsIgnoreCase(configManager.getEnv()))) {
				useVip = true;
			} else if (vip.startsWith("console:")) {
				useVip = true;
				vip = vip.replaceAll("console", NetUtils.getFirstLocalIp());
			}
		}
		try {
			if (useVip) {
				serviceAddress = vip;
			} else {
				serviceAddress = RegistryManager.getInstance().getServiceAddress(serviceName, group);
			}
		} catch (Throwable e) {
			logger.error("cannot get service provider for service:" + serviceName, e);
			throw new ServiceUnavailableException("cannot get service provider for service:" + serviceName + ", env:"
					+ configManager.getEnv(), e);
		}

		if (StringUtils.isBlank(serviceAddress)) {
			throw new ServiceUnavailableException("empty service address from registry for service:" + serviceName
					+ ", group:" + group + ", env:" + configManager.getEnv());
		}

		if (logger.isInfoEnabled()) {
			logger.info("selected service provider address is:" + serviceAddress + " with service:" + serviceName
					+ ",group:" + group);
		}
		serviceAddress = serviceAddress.trim();
		return serviceAddress;
	}

	public void registerServiceInvokers(String serviceName, String group, String vip) {
		String serviceAddress = getServiceAddress(serviceName, group, vip);
		String[] addressArray = serviceAddress.split(",");
		// List<String> addressList = new ArrayList<String>();
		for (int i = 0; i < addressArray.length; i++) {
			if (StringUtils.isNotBlank(addressArray[i])) {
				// addressList.add(addressArray[i]);
				String address = addressArray[i];
				int idx = address.lastIndexOf(":");
				if (idx != -1) {
					String host = null;
					int port = -1;
					try {
						host = address.substring(0, idx);
						port = Integer.parseInt(address.substring(idx + 1));
					} catch (RuntimeException e) {
						logger.warn("invalid address:" + address + " for service:" + serviceName);
					}
					if (host != null && port > 0) {
						try {
							int weight = RegistryManager.getInstance().getServiceWeight(address, !reloadWeight);
							RegistryEventListener.providerAdded(serviceName, host, port, weight);
						} catch (Throwable e) {
							throw new ServiceUnavailableException("error while registering service invoker:"
									+ serviceName + ", address:" + address + ", env:" + configManager.getEnv(), e);
						}
					}
				} else {
					logger.warn("invalid address:" + address + " for service:" + serviceName);
				}
			}
		}
	}

	public Map<String, Set<HostInfo>> getServiceHosts() {
		return RegistryManager.getInstance().getAllReferencedServiceAddresses();
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
			RegistryManager.getInstance().removeServiceAddress(event.getServiceName(), hostInfo);
		}

		@Override
		public void hostWeightChanged(ServiceProviderChangeEvent event) {
		}
	}

	class InnerRegistryConnectionListener implements RegistryConnectionListener {

		@Override
		public void reconnected() {
			Set<InvokerConfig<?>> services = ServiceFactory.getAllServiceInvokers().keySet();
			Map<String, String> serviceGroupMap = new HashMap<String, String>();
			for (InvokerConfig<?> invokerConfig : services) {
				serviceGroupMap.put(invokerConfig.getUrl(), invokerConfig.getGroup());
			}
			for (String url : serviceGroupMap.keySet()) {
				try {
					registerServiceInvokers(url, serviceGroupMap.get(url), null);
				} catch (Throwable t) {
					logger.warn("error while trying to register service client:" + url + ", caused by:"
							+ t.getMessage());
				}
			}
		}

	}

	public void clear() {
		clusterListener.clear();
	}

}
