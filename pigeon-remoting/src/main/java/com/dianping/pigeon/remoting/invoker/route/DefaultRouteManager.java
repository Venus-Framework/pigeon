/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.invoker.route;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.dianping.dpsf.exception.NoConnectionException;
import com.dianping.pigeon.component.invocation.InvocationRequest;
import com.dianping.pigeon.component.phase.Disposable;
import com.dianping.pigeon.registry.RegistryManager;
import com.dianping.pigeon.registry.listener.RegistryEventListener;
import com.dianping.pigeon.registry.listener.ServiceProviderChangeEvent;
import com.dianping.pigeon.registry.listener.ServiceProviderChangeListener;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.invoker.Client;
import com.dianping.pigeon.remoting.invoker.component.InvokerMetaData;
import com.dianping.pigeon.remoting.invoker.listener.ClusterListenerManager;
import com.dianping.pigeon.remoting.invoker.route.balance.ConsistentHashLoadBalance;
import com.dianping.pigeon.remoting.invoker.route.balance.LeastSuccessLoadBalance;
import com.dianping.pigeon.remoting.invoker.route.balance.LoadAutoawareLoadBalance;
import com.dianping.pigeon.remoting.invoker.route.balance.LoadBalance;
import com.dianping.pigeon.remoting.invoker.route.balance.LoadBalanceManager;
import com.dianping.pigeon.remoting.invoker.route.balance.RandomLoadBalance;
import com.dianping.pigeon.remoting.invoker.route.balance.RoundRobinLoadBalance;

public class DefaultRouteManager implements RouteManager, Disposable {

	private static final Logger logger = Logger.getLogger(DefaultRouteManager.class);

	private static final ClusterListenerManager clusterListenerManager = ClusterListenerManager.getInstance();

	private ServiceProviderChangeListener providerChangeListener = new InnerServiceProviderChangeListener();

	public DefaultRouteManager() {
		RegistryEventListener.addListener(providerChangeListener);
		LoadBalanceManager.register(RandomLoadBalance.NAME, null, RandomLoadBalance.instance);
		LoadBalanceManager.register(LoadAutoawareLoadBalance.NAME, null, LoadAutoawareLoadBalance.instance);
		LoadBalanceManager.register(LeastSuccessLoadBalance.NAME, null, LeastSuccessLoadBalance.instance);
		LoadBalanceManager.register(RoundRobinLoadBalance.NAME, null, RoundRobinLoadBalance.instance);
		LoadBalanceManager.register(ConsistentHashLoadBalance.NAME, null, ConsistentHashLoadBalance.instance);
	}

	public Client route(List<Client> clientList, InvokerMetaData metaData, InvocationRequest request) {
		if (logger.isDebugEnabled()) {
			logger.debug("Routing from: ");
			for (Client client : clientList) {
				logger.debug("当前可用的IP地址列表：\t" + client.getAddress());
			}
		}
		Boolean isWriteBufferLimit = (Boolean) request.getAttachment(Constants.REQ_ATTACH_WRITE_BUFF_LIMIT);

		isWriteBufferLimit = (isWriteBufferLimit != null ? isWriteBufferLimit : false)
				&& request.getCallType() == Constants.CALLTYPE_NOREPLY;

		List<Client> availableClients = filterWithGroupAndWeight(clientList, metaData, isWriteBufferLimit);

		Client selectedClient = select(availableClients, metaData, request);

		checkClientNotNull(selectedClient, metaData);

		while (!selectedClient.isConnected()) {
			clusterListenerManager.removeConnect(selectedClient);
			availableClients.remove(selectedClient);
			if (availableClients.isEmpty()) {
				break;
			}
			selectedClient = select(availableClients, metaData, request);
			checkClientNotNull(selectedClient, metaData);
		}

		if (!selectedClient.isConnected()) {
			throw new NoConnectionException("no available server exists for service metaData[" + metaData + "] .");
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
	public List<Client> filterWithGroupAndWeight(List<Client> clientList, InvokerMetaData metaData,
			Boolean isWriteBufferLimit) {
		List<Client> filteredClients = new ArrayList<Client>(clientList.size());
		boolean existClientBuffToLimit = false;
		for (Client client : clientList) {
			String address = client.getAddress();
			if (client.isActive() 
					&& RegistryManager.getInstance().getServiceWeight(address) > 0) {
				if (!isWriteBufferLimit || client.isWritable()) {
					filteredClients.add(client);
				} else {
					existClientBuffToLimit = true;
				}
			}
		}
		if (filteredClients.isEmpty()) {
			throw new NoConnectionException("no available server exists for service[" + metaData.getServiceName()
					+ "] and group[" + metaData.getGroup() + "]"
					+ (existClientBuffToLimit ? ", and exists some server's write buffer reach limit" : "") + ".");
		}
		return filteredClients;
	}

	private void checkClientNotNull(Client client, InvokerMetaData metaData) {
		if (client == null) {
			throw new NoConnectionException("no available server exists for service[" + metaData + "]");
		}
	}

	private Client select(List<Client> availableClients, InvokerMetaData metaData, InvocationRequest request) {
		LoadBalance loadBalance = null;
		if (request.getCallType() == Constants.CALLTYPE_NOREPLY) {
			loadBalance = RandomLoadBalance.instance;
		}
		if (loadBalance == null) {
			loadBalance = LoadBalanceManager.getLoadBalance(metaData, request.getCallType());
		}
		if (loadBalance == null) {
			loadBalance = RandomLoadBalance.instance;
		}

		return loadBalance.select(availableClients, request);
	}

	@Override
	public void destroy() {
		RegistryEventListener.removeListener(providerChangeListener);
	}

	class InnerServiceProviderChangeListener implements ServiceProviderChangeListener {
		@Override
		public void hostWeightChanged(ServiceProviderChangeEvent event) {
			RegistryManager.getInstance().setServiceWeight(event.getConnect(), event.getWeight());
		}

		@Override
		public void providerAdded(ServiceProviderChangeEvent event) {
			RegistryManager.getInstance().setServiceWeight(event.getConnect(), event.getWeight());
			// todo
		}

		@Override
		public void providerRemoved(ServiceProviderChangeEvent event) {
			RegistryManager.getInstance().setServiceWeight(event.getConnect(), 0);
			// todo
		}
	}

}
