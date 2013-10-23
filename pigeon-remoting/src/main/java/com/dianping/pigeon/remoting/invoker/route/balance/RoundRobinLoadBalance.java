package com.dianping.pigeon.remoting.invoker.route.balance;

import java.util.List;

import org.apache.log4j.Logger;

import com.dianping.pigeon.component.invocation.InvocationRequest;
import com.dianping.pigeon.remoting.invoker.Client;

public class RoundRobinLoadBalance extends AbstractLoadBalance {
	public static final String NAME = "roundRobin";

	public static final LoadBalance instance = new RoundRobinLoadBalance();

	private static int lastSelected = -1;
	private static int currentWeight = 0;

	private static Logger logger = Logger.getLogger(RoundRobinLoadBalance.class);

	/**
	 * 
	 */
	@Override
	protected Client doSelect(List<Client> clients, InvocationRequest request, int[] weights) {
		assert (clients != null && clients.size() > 1);

		int[] _weights = new int[weights.length - 1];
		for (int i = 0; i < weights.length - 1; i++) {
			_weights[i] = weights[i];
		}

		int clientId = roundRobin(_weights);
		Client client = clientId < 0 ? clients.get(random.nextInt(_weights.length)) : clients.get(clientId);
		System.err.println("################Select " + client.getAddress());
		logger.info("################Select " + client.getAddress());
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
