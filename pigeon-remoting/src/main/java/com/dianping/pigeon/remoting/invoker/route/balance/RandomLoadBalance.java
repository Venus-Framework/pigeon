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

/**
 * 随机负载均衡策略
 * 
 * @author jianhuihuang
 * @version $Id: RandomLoadBalance.java, v 0.1 2013-7-5 上午8:30:36 jianhuihuang
 *          Exp $
 */
public class RandomLoadBalance extends AbstractLoadBalance {

	private static final Logger logger = LoggerLoader.getLogger(RandomLoadBalance.class);
	public static final String NAME = "random";
	public static final LoadBalance instance = new RandomLoadBalance();

	@Override
	public Client doSelect(List<Client> clients, InvokerConfig<?> invokerConfig, InvocationRequest request,
			int[] weights) {
		assert (clients != null && clients.size() >= 1);
		if (clients.size() == 1) {
			return clients.get(0);
		}
		int clientSize = clients.size();
		int totalWeight = 0;
		boolean weightAllSame = true;
		for (int i = 0; i < clientSize; i++) {
			totalWeight += weights[i];
			if (weightAllSame && i > 0 && weights[i] != weights[i - 1]) {
				weightAllSame = false;
			}
		}
		if (!weightAllSame) {
			int weightPoint = random.nextInt(totalWeight);
			for (int i = 0; i < clientSize; i++) {
				Client client = clients.get(i);
				weightPoint -= weights[i];
				if (weightPoint < 0) {
					return client;
				}
			}
		}
		Client client = clients.get(random.nextInt(clientSize));
		if (logger.isDebugEnabled()) {
			logger.debug("select address:" + client.getAddress());
		}
		return client;
	}

}
