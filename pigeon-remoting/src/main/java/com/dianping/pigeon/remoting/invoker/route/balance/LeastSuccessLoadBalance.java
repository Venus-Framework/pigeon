/**
 * Dianping.com Inc.
 * Copyright (c) 2005-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.invoker.route.balance;

import java.util.List;

import com.dianping.pigeon.component.invocation.InvocationRequest;
import com.dianping.pigeon.remoting.invoker.Client;
import com.dianping.pigeon.remoting.invoker.route.stat.AddressStatPoolServiceImpl;
import com.dianping.pigeon.remoting.invoker.route.stat.DpsfAddressStatPool;
import com.dianping.pigeon.remoting.invoker.route.stat.DpsfAddressStatPoolService;
import com.dianping.pigeon.remoting.invoker.route.stat.barrel.ServerStatBarrelsHolder;
import com.dianping.pigeon.remoting.invoker.route.stat.support.AddressConstant;

/**
 * 动态客户端路由规则，把当前不稳定的IP地址做隔离。
 * 
 * @author jianhuihuang
 * 
 */
public class LeastSuccessLoadBalance extends AbstractLoadBalance {

	public static final String NAME = "leastSuccess";

	public static final LoadBalance instance = new LeastSuccessLoadBalance();

	DpsfAddressStatPoolService dpsfAddressStatPoolService = AddressStatPoolServiceImpl.getInstance();

	@Override
	protected Client doSelect(List<Client> clients, InvocationRequest request, int[] weights) {

		String serviceName = request.getServiceName();
		DpsfAddressStatPool addressStatPool = dpsfAddressStatPoolService.getAddressStatPool(serviceName);
		int clientSize = clients.size();
		List<String> insulateIps = addressStatPool.getInsulateIps(AddressConstant.ISOLATION_EXCEPTION);
		// 如果是在insulateIps的列表里面就开始删除掉。在这个insulateIps比例会少于整体client的20%。
		List<String> needToRemoveIps = null;
		if (insulateIps.size() > clientSize / 5) {
			needToRemoveIps = insulateIps.subList(0, clientSize / 5);
		} else {
			needToRemoveIps = insulateIps;
		}

		float minCapacity = Float.MAX_VALUE;

		Client[] candidates = new Client[clientSize];
		int candidateIdx = 0;
		for (int i = 0; i < clientSize; i++) {
			Client client = clients.get(i);
			if (needToRemoveIps.contains(client.getAddress())) {
				continue;
			}
			float capacity = ServerStatBarrelsHolder.getCapacity(client.getAddress());
			if (capacity < minCapacity) {
				minCapacity = capacity;
				candidateIdx = 0;
				candidates[candidateIdx++] = client;
			} else if (capacity == minCapacity) {
				candidates[candidateIdx++] = client;
			}
		}
		return candidateIdx == 1 ? candidates[0] : candidates[random.nextInt(candidateIdx)];

	}

}
