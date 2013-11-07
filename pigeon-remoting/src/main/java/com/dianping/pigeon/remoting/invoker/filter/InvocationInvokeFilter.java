/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.invoker.filter;

import com.dianping.pigeon.component.invocation.InvocationRequest;
import com.dianping.pigeon.event.EventManager;
import com.dianping.pigeon.event.RuntimeServiceEvent;
import com.dianping.pigeon.remoting.common.filter.ServiceInvocationFilter;
import com.dianping.pigeon.remoting.invoker.Client;
import com.dianping.pigeon.remoting.invoker.component.RpcInvokeInfo;
import com.dianping.pigeon.remoting.invoker.component.context.InvokerContext;

public abstract class InvocationInvokeFilter implements ServiceInvocationFilter<InvokerContext> {

	public static enum InvokePhase {
		Call, Before_Call, Cluster, Before_Cluster, Error_Handle, Finalize;
	}

	public void beforeInvoke(InvocationRequest request, String addressIp) {
		RpcInvokeInfo rpcInvokeInfo = new RpcInvokeInfo();
		rpcInvokeInfo.setServiceName(request.getServiceName());
		rpcInvokeInfo.setAddressIp(addressIp);
		rpcInvokeInfo.setRequest(request);
		RuntimeServiceEvent event = new RuntimeServiceEvent(RuntimeServiceEvent.Type.RUNTIME_RPC_INVOKE_BEFORE,
				rpcInvokeInfo);

		EventManager.getInstance().publishEvent(event);
	}

	public void afterInvoke(InvocationRequest request, Client client) {
		RpcInvokeInfo rpcInvokeInfo = new RpcInvokeInfo();
		rpcInvokeInfo.setServiceName(request.getServiceName());
		long duration = System.currentTimeMillis() - request.getCreateMillisTime();
		rpcInvokeInfo.setDuration(duration);
		rpcInvokeInfo.setAddressIp(client.getAddress());
		RuntimeServiceEvent event = new RuntimeServiceEvent(RuntimeServiceEvent.Type.RUNTIME_RPC_INVOKE_AFTER,
				rpcInvokeInfo);

		EventManager.getInstance().publishEvent(event);
	}

}
