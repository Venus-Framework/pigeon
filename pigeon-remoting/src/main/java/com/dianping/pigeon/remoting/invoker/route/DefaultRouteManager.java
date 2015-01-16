/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.invoker.route;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.dianping.pigeon.config.ConfigManagerLoader;
import com.dianping.pigeon.domain.phase.Disposable;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.registry.RegistryManager;
import com.dianping.pigeon.registry.listener.RegistryEventListener;
import com.dianping.pigeon.registry.listener.ServiceProviderChangeEvent;
import com.dianping.pigeon.registry.listener.ServiceProviderChangeListener;
import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.invoker.Client;
import com.dianping.pigeon.remoting.invoker.config.InvokerConfig;
import com.dianping.pigeon.remoting.invoker.exception.ServiceUnavailableException;
import com.dianping.pigeon.remoting.invoker.listener.ClusterListenerManager;
import com.dianping.pigeon.remoting.invoker.route.balance.AutoawareLoadBalance;
import com.dianping.pigeon.remoting.invoker.route.balance.LoadBalance;
import com.dianping.pigeon.remoting.invoker.route.balance.LoadBalanceManager;
import com.dianping.pigeon.remoting.invoker.route.balance.RandomLoadBalance;
import com.dianping.pigeon.remoting.invoker.route.balance.RoundRobinLoadBalance;
import com.dianping.pigeon.remoting.invoker.route.balance.WeightedAutoawareLoadBalance;

public class DefaultRouteManager implements RouteManager, Disposable {

	private static final Logger logger = LoggerLoader.getLogger(DefaultRouteManager.class);

	private static final ClusterListenerManager clusterListenerManager = ClusterListenerManager.getInstance();

	private ServiceProviderChangeListener providerChangeListener = new InnerServiceProviderChangeListener();

	private static List<String> preferAddresses = new ArrayList<String>();

	private static boolean enablePreferAddresses = ConfigManagerLoader.getConfigManager().getBooleanValue(
			"pigeon.route.preferaddresses.enable", false);

	public DefaultRouteManager() {
		RegistryEventListener.addListener(providerChangeListener);
		LoadBalanceManager.register(RandomLoadBalance.NAME, null, RandomLoadBalance.instance);
		LoadBalanceManager.register(AutoawareLoadBalance.NAME, null, AutoawareLoadBalance.instance);
		LoadBalanceManager.register(RoundRobinLoadBalance.NAME, null, RoundRobinLoadBalance.instance);
		LoadBalanceManager.register(WeightedAutoawareLoadBalance.NAME, null, WeightedAutoawareLoadBalance.instance);
		if (enablePreferAddresses) {
			String preferAddressesConfig = ConfigManagerLoader.getConfigManager().getStringValue(
					"pigeon.route.preferaddresses", "");
			String[] preferAddressesArray = preferAddressesConfig.split(",");
			for (String addr : preferAddressesArray) {
				if (StringUtils.isNotBlank(addr)) {
					preferAddresses.add(addr.trim());
				}
			}
		}
	}

	public Client route(List<Client> clientList, InvokerConfig<?> invokerConfig, InvocationRequest request) {
		if (logger.isDebugEnabled()) {
			for (Client client : clientList) {
				logger.debug("available service provider：\t" + client.getAddress());
			}
		}
		List<Client> availableClients = getAvailableClients(clientList, invokerConfig, request);
		Client selectedClient = select(availableClients, invokerConfig, request);
		if (!selectedClient.isConnected()) {
			selectedClient.connect();
		}
		while (!selectedClient.isConnected()) {
			clusterListenerManager.removeConnect(selectedClient);
			availableClients.remove(selectedClient);
			if (availableClients.isEmpty()) {
				break;
			}
			selectedClient = select(availableClients, invokerConfig, request);
		}

		if (!selectedClient.isConnected()) {
			throw new ServiceUnavailableException("no available server exists for service[" + invokerConfig + "], env:"
					+ ConfigManagerLoader.getConfigManager().getEnv());
		}
		return selectedClient;
	}

	/**
	 * 按照权重和分组过滤客户端选择
	 * 
	 * @param clientList
	 * @param serviceName
	 * @param group
	 * @param isWriteBufferLimit
	 * @return
	 */
	public List<Client> getAvailableClients(List<Client> clientList, InvokerConfig<?> invokerConfig,
			InvocationRequest request) {
		Boolean isWriteBufferLimit = (Boolean) request.getAttachment(Constants.REQ_ATTACH_WRITE_BUFF_LIMIT);
		isWriteBufferLimit = (isWriteBufferLimit != null ? isWriteBufferLimit : false)
				&& request.getCallType() == Constants.CALLTYPE_NOREPLY;
		List<Client> filteredClients = new ArrayList<Client>(clientList.size());
		boolean existClientBuffToLimit = false;
		for (Client client : clientList) {
			String address = client.getAddress();
			if (client.isActive() && RegistryManager.getInstance().getServiceWeight(address) > 0) {
				if (!isWriteBufferLimit || client.isWritable()) {
					filteredClients.add(client);
				} else {
					existClientBuffToLimit = true;
				}
			}
		}
		if (filteredClients.isEmpty()) {
			throw new ServiceUnavailableException("no available server exists for service[" + invokerConfig.getUrl()
					+ "] and group[" + invokerConfig.getGroup() + "]"
					+ (existClientBuffToLimit ? ", and exists some server's write buffer reach limit" : "") + ".");
		}
		return filteredClients;
	}

	private void checkClientNotNull(Client client, InvokerConfig<?> invokerConfig) {
		if (client == null) {
			throw new ServiceUnavailableException("no available server exists for service[" + invokerConfig + "], env:"
					+ ConfigManagerLoader.getConfigManager().getEnv());
		}
	}

	private Client select(List<Client> availableClients, InvokerConfig<?> invokerConfig, InvocationRequest request) {
		LoadBalance loadBalance = null;
		if (loadBalance == null) {
			loadBalance = LoadBalanceManager.getLoadBalance(invokerConfig, request.getCallType());
		}
		if (loadBalance == null) {
			loadBalance = WeightedAutoawareLoadBalance.instance;
			if (request.getCallType() == Constants.CALLTYPE_NOREPLY) {
				loadBalance = RandomLoadBalance.instance;
			}
		}
		List<Client> preferClients = null;
		if (enablePreferAddresses) {
			if (availableClients != null && availableClients.size() > 1) {
				preferClients = new ArrayList<Client>();
				for (String addr : preferAddresses) {
					for (Client client : availableClients) {
						if (client.getHost().startsWith(addr)) {
							preferClients.add(client);
						}
					}
					if (preferClients.size() > 0) {
						break;
					}
				}
			}
		}
		if (preferClients == null || preferClients.size() == 0) {
			preferClients = availableClients;
		}
		Client selectedClient = loadBalance.select(preferClients, invokerConfig, request);
		checkClientNotNull(selectedClient, invokerConfig);

		return selectedClient;
	}

	@Override
	public void destroy() throws Exception {
		RegistryEventListener.removeListener(providerChangeListener);
	}

	class InnerServiceProviderChangeListener implements ServiceProviderChangeListener {
		@Override
		public void hostWeightChanged(ServiceProviderChangeEvent event) {
			RegistryManager.getInstance().setServiceWeight(event.getConnect(), event.getWeight());
		}

		@Override
		public void providerAdded(ServiceProviderChangeEvent event) {
		}

		@Override
		public void providerRemoved(ServiceProviderChangeEvent event) {
		}
	}

}
