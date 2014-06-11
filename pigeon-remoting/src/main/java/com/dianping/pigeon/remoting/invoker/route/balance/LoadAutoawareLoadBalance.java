/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.invoker.route.balance;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.invoker.Client;
import com.dianping.pigeon.remoting.invoker.route.statistics.ServiceStatisticsHolder;

/**
 * 感知服务端负载情况, 将请求路由到负载较低的服务端
 * 
 * @author danson.liu
 * 
 */
public class LoadAutoawareLoadBalance extends AbstractLoadBalance {

	private static final Logger logger = LoggerLoader.getLogger(LoadAutoawareLoadBalance.class);
	public static final String NAME = "autoaware";
	public static final LoadBalance instance = new LoadAutoawareLoadBalance();
	private static Map<String, AtomicInteger> clientCountMap = new HashMap<String, AtomicInteger>();
	private static int logCount = 0;

	@Override
	public Client doSelect(List<Client> clients, InvocationRequest request, int[] weights) {
		float minCapacity = Float.MAX_VALUE;
		int clientSize = clients.size();
		Client[] candidates = new Client[clientSize];
		int candidateIdx = 0;
		for (int i = 0; i < clientSize; i++) {
			Client client = clients.get(i);
			float capacity = ServiceStatisticsHolder.getCapacity(client.getAddress());
			if (capacity < minCapacity) {
				minCapacity = capacity;
				candidateIdx = 0;
				candidates[candidateIdx++] = client;
			} else if (capacity == minCapacity) {
				candidates[candidateIdx++] = client;
			}
		}
		Client client = candidateIdx == 1 ? candidates[0] : candidates[random.nextInt(candidateIdx)];
		if (logger.isDebugEnabled()) {
			logger.debug("select address:" + client.getAddress());
		}
		logClient(client);
		return client;
	}

	private void logClient(Client client) {
		AtomicInteger count = clientCountMap.get(client.getAddress());
		if (count == null) {
			count = new AtomicInteger(0);
			clientCountMap.put(client.getAddress(), count);
		}
		count.incrementAndGet();
		if (++logCount % 10000 == 0) {
			logger.info("select address:" + clientCountMap);
		}
	}

}
