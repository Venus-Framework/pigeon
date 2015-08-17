/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.invoker.route.balance;

import java.util.List;

import org.apache.logging.log4j.Logger;

import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.invoker.Client;
import com.dianping.pigeon.remoting.invoker.config.InvokerConfig;
import com.dianping.pigeon.remoting.invoker.route.statistics.ServiceStatisticsHolder;
import com.dianping.pigeon.util.LangUtils;

/**
 * 感知服务端负载情况, 将请求路由到负载较低的服务端
 * 
 * @author danson.liu
 * 
 */
public class AutoawareLoadBalance extends AbstractLoadBalance {

	private static final Logger logger = LoggerLoader.getLogger(AutoawareLoadBalance.class);
	public static final String NAME = "autoaware";
	public static final LoadBalance instance = new AutoawareLoadBalance();

	@Override
	public Client doSelect(List<Client> clients, InvokerConfig<?> invokerConfig, InvocationRequest request,
			int[] weights) {
		assert (clients != null && clients.size() >= 1);
		if (clients.size() == 1) {
			return clients.get(0);
		}
		float minCapacity = Float.MAX_VALUE;
		int clientSize = clients.size();
		Client[] candidates = new Client[clientSize];
		int candidateIdx = 0;
		for (int i = 0; i < clientSize; i++) {
			Client client = clients.get(i);
			float capacity = ServiceStatisticsHolder.getCapacity(client.getAddress());
			if (logger.isDebugEnabled()) {
				logger.debug("capacity:" + LangUtils.toString(capacity, 4) + " for address:" + client.getAddress());
			}
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
		return client;
	}

}
