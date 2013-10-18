/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.invoker.route.listener;

import java.util.HashSet;
import java.util.Set;

import com.dianping.pigeon.event.RuntimeServiceEvent;
import com.dianping.pigeon.event.RuntimeServiceEvent.Type;
import com.dianping.pigeon.remoting.invoker.component.RpcInvokeInfo;
import com.dianping.pigeon.remoting.invoker.route.stat.barrel.ServerStatBarrelsHolder;

/**
 * 在RPC调用之前的监听器。
 * 
 * @author jianhuihuang
 * 
 */
public class ClientFlowOutRPCInvokeListener extends AbstractRPCInvokeListener {

	// private static final Logger logger =
	// Logger.getLogger(ClientFlowOutRPCInvokeListener.class
	// .getName());

	private static Set<Type> eventNames = new HashSet<Type>();
	static {
		eventNames.add(RuntimeServiceEvent.Type.RUNTIME_RPC_INVOKE_CLIENT_TIMEOUT_EXCEPTION);
		eventNames.add(RuntimeServiceEvent.Type.RUNTIME_RPC_INVOKE_CLIENT_RECEIVE_RESPONSE);
		eventNames.add(RuntimeServiceEvent.Type.RUNTIME_RPC_INVOKE_CLIENT_CHANNEL_EXCEPTION);
		eventNames.add(RuntimeServiceEvent.Type.RUNTIME_RPC_INVOKE_CLIENT_CHANNEL_COMPLETE);

	}

	public void handleEvent(RuntimeServiceEvent event) {

		RpcInvokeInfo rpcInvokeInfo = (RpcInvokeInfo) event.eventObj;
		String ip = rpcInvokeInfo.getAddressIp();
		// String serviceName = rpcInvokeInfo.getServiceName();
		/**
		 * if (ip != null) { DpsfAddressStatPool addressStatPool =
		 * dpsfAddressStatPoolService.getAddressStatPool(serviceName);
		 * addressStatPool.use(ip); }
		 **/
		ServerStatBarrelsHolder.flowOut(rpcInvokeInfo.getRequest(), ip);

		// ServerStatBarrelsHolder.flowIn(rpcInvokeInfo.getRequest(), ip);

	}

	public boolean support(RuntimeServiceEvent event) {
		if (eventNames.contains(event.getEventType())) {
			return true;
		}
		return false;
	}
}
