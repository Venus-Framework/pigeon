package com.dianping.pigeon.remoting.invoker.route.balance;

import java.util.List;

import org.apache.logging.log4j.Logger;

import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.invoker.Client;
import com.dianping.pigeon.remoting.invoker.config.InvokerConfig;

public class RoundRobinLoadBalance extends AbstractLoadBalance {

	private static final Logger logger = LoggerLoader.getLogger(RoundRobinLoadBalance.class);
	public static final String NAME = "roundRobin";
	public static final LoadBalance instance = new RoundRobinLoadBalance();
	private static int lastSelected = -1;
	private static int currentWeight = 0;

	/**
	 * 
	 */
	@Override
	protected Client doSelect(List<Client> clients, InvokerConfig<?> invokerConfig, InvocationRequest request,
			int[] weights) {
		assert (clients != null && clients.size() > 1);

		int[] _weights = new int[weights.length - 1];
		for (int i = 0; i < weights.length - 1; i++) {
			_weights[i] = weights[i];
		}
		int clientId = roundRobin(_weights);
		Client client = clientId < 0 ? clients.get(random.nextInt(_weights.length)) : clients.get(clientId);
		if (logger.isDebugEnabled()) {
			logger.debug("select address:" + client.getAddress());
		}
		return client;
	}

	public int roundRobin(int[] weights) {
		int clientSize = weights.length;
		int gcdWeights = gcdWeights(weights);
		int maxWeight = maxWeight(weights);

		if (lastSelected >= clientSize) {
			lastSelected = clientSize - 1;
		}
		while (true) {
			lastSelected = (lastSelected + 1) % clientSize;
			if (lastSelected == 0) {
				currentWeight = currentWeight - gcdWeights;
				if (currentWeight <= 0) {
					currentWeight = maxWeight;
					if (currentWeight == 0) {
						return -1;
					}
				}
			}
			if (weights[lastSelected] >= currentWeight) {
				return lastSelected;
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
