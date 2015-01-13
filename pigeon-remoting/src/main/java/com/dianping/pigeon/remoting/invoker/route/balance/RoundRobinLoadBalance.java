package com.dianping.pigeon.remoting.invoker.route.balance;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.invoker.Client;
import com.dianping.pigeon.remoting.invoker.config.InvokerConfig;

public class RoundRobinLoadBalance extends AbstractLoadBalance {

	private static final Logger logger = LoggerLoader.getLogger(RoundRobinLoadBalance.class);
	public static final String NAME = "roundRobin";
	public static final LoadBalance instance = new RoundRobinLoadBalance();
	private static final ConcurrentHashMap<String, RoundRobinBalancer> balancers = new ConcurrentHashMap<String, RoundRobinBalancer>();

	@Override
	protected Client doSelect(List<Client> clients, InvokerConfig<?> invokerConfig, InvocationRequest request,
			int[] weights) {
		assert (clients != null && clients.size() >= 1);
		if (clients.size() == 1) {
			return clients.get(0);
		}
		String serviceId = LoadBalanceManager.getServiceId(invokerConfig.getUrl(), invokerConfig.getGroup());
		RoundRobinBalancer balancer = balancers.get(serviceId);
		if (balancer == null) {
			balancer = new RoundRobinBalancer();
			balancers.putIfAbsent(serviceId, balancer);
		}

		int[] _weights = new int[weights.length - 1];
		for (int i = 0; i < weights.length - 1; i++) {
			_weights[i] = weights[i];
		}
		int clientId = roundRobin(balancer, _weights);
		Client client = clientId < 0 ? clients.get(random.nextInt(_weights.length)) : clients.get(clientId);
		if (logger.isDebugEnabled()) {
			logger.debug("select address:" + client.getAddress());
		}
		return client;
	}

	public static class RoundRobinBalancer {
		private int lastSelected = -1;
		private int currentWeight = 0;

		public int getLastSelected() {
			return lastSelected;
		}

		public void setLastSelected(int lastSelected) {
			this.lastSelected = lastSelected;
		}

		public int getCurrentWeight() {
			return currentWeight;
		}

		public void setCurrentWeight(int currentWeight) {
			this.currentWeight = currentWeight;
		}

	}

	public int roundRobin(RoundRobinBalancer counter, int[] weights) {
		int clientSize = weights.length;
		int gcdWeights = gcdWeights(weights);
		int maxWeight = maxWeight(weights);

		if (counter.getLastSelected() >= clientSize) {
			counter.setLastSelected(clientSize - 1);
		}
		while (true) {
			counter.setLastSelected((counter.getLastSelected() + 1) % clientSize);
			if (counter.getLastSelected() == 0) {
				counter.setCurrentWeight(counter.getCurrentWeight() - gcdWeights);
				if (counter.getCurrentWeight() <= 0) {
					counter.setCurrentWeight(maxWeight);
					if (counter.getCurrentWeight() == 0) {
						return -1;
					}
				}
			}
			if (weights[counter.getLastSelected()] >= counter.getCurrentWeight()) {
				return counter.getLastSelected();
			}
		}
	}

	private int maxWeight(int[] weights) {
		int max = weights[0];
		for (int it : weights) {
			if (it > max) {
				max = it;
			}
		}
		return max;
	}

	private int gcdWeights(int[] weights) {
		return gcdN(weights, weights.length);
	}

	private int gcd(int a, int b) {
		if (0 == b) {
			return a;
		} else {
			return gcd(b, a % b);
		}
	}

	public int gcdN(int[] digits, int length) {
		if (1 == length) {
			return digits[0];
		} else {
			return gcd(digits[length - 1], gcdN(digits, length - 1));
		}
	}

}
