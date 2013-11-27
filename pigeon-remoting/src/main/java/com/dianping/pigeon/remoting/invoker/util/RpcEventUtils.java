/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.invoker.util;

import com.dianping.pigeon.component.invocation.InvocationRequest;
import com.dianping.pigeon.event.EventManager;
import com.dianping.pigeon.event.RuntimeServiceEvent;
import com.dianping.pigeon.remoting.invoker.component.RpcInvokeInfo;

public final class RpcEventUtils {

	public static void clientTimeOutEvent(InvocationRequest request, String addressIp) {
		if (EventManager.IS_EVENT_ENABLED) {
			RpcInvokeInfo rpcInvokeInfo = new RpcInvokeInfo();
			rpcInvokeInfo.setServiceName(request.getServiceName());
			rpcInvokeInfo.setRequest(request);
			rpcInvokeInfo.setAddressIp(addressIp);
			RuntimeServiceEvent event = new RuntimeServiceEvent(
					RuntimeServiceEvent.Type.RUNTIME_RPC_INVOKE_CLIENT_TIMEOUT_EXCEPTION, rpcInvokeInfo);

			EventManager.getInstance().postEvent(event);
		}
	}

	/**
	 * TODO
	 * 
	 * @param request
	 * @param addressIp
	 */
	public static void clientReceiveResponse(InvocationRequest request, String addressIp) {
		if (EventManager.IS_EVENT_ENABLED) {
			RpcInvokeInfo rpcInvokeInfo = new RpcInvokeInfo();
			rpcInvokeInfo.setServiceName(request.getServiceName());
			rpcInvokeInfo.setRequest(request);
			rpcInvokeInfo.setAddressIp(addressIp);
			RuntimeServiceEvent event = new RuntimeServiceEvent(
					RuntimeServiceEvent.Type.RUNTIME_RPC_INVOKE_CLIENT_RECEIVE_RESPONSE, rpcInvokeInfo);

			EventManager.getInstance().postEvent(event);
		}
	}

	/**
	 * TODO
	 * 
	 * @param request
	 * @param addressIp
	 */
	public static void channelExceptionCaughtEvent(InvocationRequest request, String addressIp) {
		if (EventManager.IS_EVENT_ENABLED) {
			RpcInvokeInfo rpcInvokeInfo = new RpcInvokeInfo();
			rpcInvokeInfo.setServiceName(request.getServiceName());
			rpcInvokeInfo.setRequest(request);
			rpcInvokeInfo.setAddressIp(addressIp);
			RuntimeServiceEvent event = new RuntimeServiceEvent(
					RuntimeServiceEvent.Type.RUNTIME_RPC_INVOKE_CLIENT_CHANNEL_EXCEPTION, rpcInvokeInfo);

			EventManager.getInstance().postEvent(event);
		}
	}

	public static void channelOperationComplete(InvocationRequest request, String addressIp) {
		if (EventManager.IS_EVENT_ENABLED) {
			RpcInvokeInfo rpcInvokeInfo = new RpcInvokeInfo();
			rpcInvokeInfo.setServiceName(request.getServiceName());
			rpcInvokeInfo.setRequest(request);
			rpcInvokeInfo.setAddressIp(addressIp);
			RuntimeServiceEvent event = new RuntimeServiceEvent(
					RuntimeServiceEvent.Type.RUNTIME_RPC_INVOKE_CLIENT_CHANNEL_COMPLETE, rpcInvokeInfo);

			EventManager.getInstance().postEvent(event);
		}
	}

}
