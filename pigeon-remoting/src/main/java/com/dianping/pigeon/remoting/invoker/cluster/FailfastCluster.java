package com.dianping.pigeon.remoting.invoker.cluster;

import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.common.process.ServiceInvocationHandler;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.invoker.Client;
import com.dianping.pigeon.remoting.invoker.ClientManager;
import com.dianping.pigeon.remoting.invoker.config.InvokerConfig;
import com.dianping.pigeon.remoting.invoker.domain.InvokerContext;
import com.dianping.pigeon.remoting.invoker.util.InvokerUtils;

public class FailfastCluster implements Cluster {

	private ClientManager clientManager = ClientManager.getInstance();

	@Override
	public InvocationResponse invoke(ServiceInvocationHandler handler, InvokerContext invocationContext)
			throws Throwable {
		InvokerConfig<?> invokerConfig = invocationContext.getInvokerConfig();
		InvocationRequest request = InvokerUtils.createRemoteCallRequest(invocationContext, invokerConfig);
		Client remoteClient = clientManager.getClient(invokerConfig, request, null);
		invocationContext.setClient(remoteClient);
		return handler.handle(invocationContext);
	}

	@Override
	public String getName() {
		return Constants.CLUSTER_FAILFAST;
	}

}
