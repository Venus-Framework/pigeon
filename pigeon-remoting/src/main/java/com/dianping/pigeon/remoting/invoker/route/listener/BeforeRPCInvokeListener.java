/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.invoker.route.listener;

import com.dianping.pigeon.event.RuntimeServiceEvent;
import com.dianping.pigeon.remoting.invoker.component.RpcInvokeInfo;
import com.dianping.pigeon.remoting.invoker.route.stat.DpsfAddressStatPool;
import com.dianping.pigeon.remoting.invoker.route.stat.barrel.ServerStatBarrelsHolder;

/**
 * 在RPC调用之前的监听器。
 * 
 * @author jianhuihuang
 * 
 */
public class BeforeRPCInvokeListener extends AbstractRPCInvokeListener {

	// private static final Logger logger =
	// Log4jLoader.getLogger(BeforeRPCInvokeListener.class.getName());

	public void handleEvent(RuntimeServiceEvent event) {

		RpcInvokeInfo rpcInvokeInfo = (RpcInvokeInfo) event.eventObj;
		String ip = rpcInvokeInfo.getAddressIp();
		String serviceName = rpcInvokeInfo.getServiceName();

		if (ip != null) {
			DpsfAddressStatPool addressStatPool = dpsfAddressStatPoolService.getAddressStatPool(serviceName);
			addressStatPool.use(ip);
		}

		ServerStatBarrelsHolder.flowIn(rpcInvokeInfo.getRequest(), ip);

	}

	public boolean support(RuntimeServiceEvent event) {
		if (event.getEventType() == RuntimeServiceEvent.Type.RUNTIME_RPC_INVOKE_BEFORE) {
			return true;
		}
		return false;
	}
}
