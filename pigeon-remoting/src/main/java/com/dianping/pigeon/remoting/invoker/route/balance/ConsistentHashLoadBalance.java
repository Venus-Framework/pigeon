package com.dianping.pigeon.remoting.invoker.route.balance;

import java.util.List;

import com.dianping.pigeon.component.invocation.InvocationRequest;
import com.dianping.pigeon.remoting.invoker.Client;

public class ConsistentHashLoadBalance extends AbstractLoadBalance {

	public static final String NAME = "consistentHash";
	public static final LoadBalance instance = new ConsistentHashLoadBalance();

	private ConsistentHash<Client> consistentHash;
	private List<Client> clientsList;

	private static final int NumberOfReplicas = 200;

	@Override
	protected synchronized Client doSelect(List<Client> clients, InvocationRequest request, int[] weights) {
		if (!clients.equals(clientsList)) {
			updateClients(clients);
		}
		Client client = consistentHash.get(request.getServiceName());
		System.err.println("Select: " + client.getAddress());
		return client;
	}

	private void updateClients(List<Client> clients) {
		this.clientsList = clients;
		consistentHash = new ConsistentHash<Client>(new ClientHashFunction(), NumberOfReplicas, clientsList);
	}
}
