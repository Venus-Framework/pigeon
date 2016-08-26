package com.dianping.pigeon.remoting.invoker.cluster;

import com.dianping.pigeon.log.Logger;

import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.common.process.ServiceInvocationHandler;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.invoker.Client;
import com.dianping.pigeon.remoting.invoker.ClientManager;
import com.dianping.pigeon.remoting.invoker.config.InvokerConfig;
import com.dianping.pigeon.remoting.invoker.domain.InvokerContext;
import com.dianping.pigeon.remoting.invoker.util.InvokerUtils;

public class FailsafeCluster implements Cluster {

	private ClientManager clientManager = ClientManager.getInstance();

	private static final Logger logger = LoggerLoader.getLogger(FailsafeCluster.class);

	private static final InvocationResponse NO_RETURN_RESPONSE = InvokerUtils.createNoReturnResponse();

	@Override
	public InvocationResponse invoke(final ServiceInvocationHandler handler, final InvokerContext invocationContext)
			throws Throwable {
		InvokerConfig<?> invokerConfig = invocationContext.getInvokerConfig();
		InvocationRequest request = InvokerUtils.createRemoteCallRequest(invocationContext, invokerConfig);
		try {
			Client remoteClient = clientManager.getClient(invokerConfig, request, null);
			invocationContext.setClient(remoteClient);
			return handler.handle(invocationContext);
		} catch (Throwable t) {
			logger.error("", t);
			return NO_RETURN_RESPONSE;
		}
	}

	@Override
	public String getName() {
		return Constants.CLUSTER_FAILSAFE;
	}

}
