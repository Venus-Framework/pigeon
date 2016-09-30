package com.dianping.pigeon.remoting.invoker.cluster;

import java.util.ArrayList;
import java.util.List;

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
import com.dianping.pigeon.remoting.invoker.exception.RemoteInvocationException;
import com.dianping.pigeon.remoting.invoker.exception.RequestTimeoutException;
import com.dianping.pigeon.remoting.invoker.exception.ServiceUnavailableException;
import com.dianping.pigeon.remoting.invoker.util.InvokerUtils;

public class FailoverCluster implements Cluster {

	private ClientManager clientManager = ClientManager.getInstance();

	private static final Logger logger = LoggerLoader.getLogger(FailoverCluster.class);

	@Override
	public InvocationResponse invoke(final ServiceInvocationHandler handler, final InvokerContext invocationContext)
			throws Throwable {
		InvokerConfig<?> invokerConfig = invocationContext.getInvokerConfig();
		List<Client> selectedClients = new ArrayList<Client>();
		Throwable lastError = null;
		int retry = invokerConfig.getRetries(invocationContext.getMethodName());

		int maxInvokeTimes = retry + 1;
		boolean timeoutRetry = invokerConfig.isTimeoutRetry();

		int invokeTimes = 0;
		for (int index = 0; index < maxInvokeTimes; index++) {
			InvocationRequest request = InvokerUtils.createRemoteCallRequest(invocationContext, invokerConfig);
			Client clientSelected = null;
			try {
				clientSelected = clientManager.getClient(invokerConfig, request, selectedClients);
			} catch (ServiceUnavailableException e) {
				if (invokeTimes > 0) {
					logger.error("Invoke method[" + invocationContext.getMethodName() + "] on service["
							+ invokerConfig.getUrl() + "] failed with " + invokeTimes + " times");
					throw lastError;
				} else {
					throw e;
				}
			}
			selectedClients.add(clientSelected);
			try {
				invokeTimes++;
				invocationContext.setClient(clientSelected);
				InvocationResponse response = handler.handle(invocationContext);
				if (lastError != null) {
					logger.warn(
							"Retry method[" + invocationContext.getMethodName() + "] on service["
									+ invokerConfig.getUrl() + "] succeed after " + invokeTimes
									+ " times, last failed error: " + lastError.getMessage(), lastError);
				}
				return response;
			} catch (Throwable e) {
				lastError = e;
				if (e instanceof RequestTimeoutException) {
					if (!timeoutRetry) {
						throw e;
					}
				}
			}
		}
		if (lastError != null) {
			logger.error("Invoke method[" + invocationContext.getMethodName() + "] on service["
					+ invokerConfig.getUrl() + "] failed with " + invokeTimes + " times");
			throw lastError;
		} else {
			throw new RemoteInvocationException("Invoke method[" + invocationContext.getMethodName() + "] on service["
					+ invokerConfig.getUrl() + "] failed with " + invokeTimes + " times, last error: "
					+ (lastError != null ? lastError.getMessage() : ""), lastError != null
					&& lastError.getCause() != null ? lastError.getCause() : lastError);
		}
	}

	@Override
	public String getName() {
		return Constants.CLUSTER_FAILOVER;
	}

}
