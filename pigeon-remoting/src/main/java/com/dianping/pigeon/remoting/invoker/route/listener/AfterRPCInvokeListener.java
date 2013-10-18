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
 * 在RPC调用之后的监听器。
 * 
 * @author jianhuihuang
 * 
 */
public class AfterRPCInvokeListener extends AbstractRPCInvokeListener {

	public void handleEvent(RuntimeServiceEvent event) {

		RpcInvokeInfo rpcInvokeInfo = (RpcInvokeInfo) event.eventObj;
		String ip = rpcInvokeInfo.getAddressIp();
		String serviceName = rpcInvokeInfo.getServiceName();
		long duration = rpcInvokeInfo.getDuration();

		if (ip != null) {
			DpsfAddressStatPool addressStatPool = dpsfAddressStatPoolService.getAddressStatPool(serviceName);

			addressStatPool.release(ip, duration);
		}
		ServerStatBarrelsHolder.flowOut(rpcInvokeInfo.getRequest(), ip);
	}

	public boolean support(RuntimeServiceEvent event) {
		if (event.getEventType() == RuntimeServiceEvent.Type.RUNTIME_RPC_INVOKE_AFTER) {
			return true;
		}
		return false;
	}
}
