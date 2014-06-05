package com.dianping.pigeon.remoting.invoker.cluster;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.dianping.dpsf.exception.NetTimeoutException;
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
import com.dianping.pigeon.remoting.invoker.route.context.ClientContext;
import com.dianping.pigeon.remoting.invoker.util.InvokerUtils;

public class FailoverCluster implements Cluster {

	private ClientManager clientManager = ClientManager.getInstance();

	private static final Logger logger = LoggerLoader.getLogger(FailoverCluster.class);

	@Override
	public InvocationResponse invoke(ServiceInvocationHandler handler, InvokerContext invocationContext)
			throws Throwable {
		InvokerConfig<?> invokerConfig = invocationContext.getInvokerConfig();
		List<Client> selectedClients = new ArrayList<Client>();
		Throwable lastError = null;
		int retry = invokerConfig.getRetries();

		int maxInvokeTimes = retry;
		boolean timeoutRetry = invokerConfig.isTimeoutRetry();

		boolean nextInvokeErrorExit = false;
		int invokeTimes = 0;
		for (int index = 0; index < maxInvokeTimes; index++) {
			InvocationRequest request = InvokerUtils.createRemoteCallRequest(invocationContext, invokerConfig);
			Client clientSelected = clientManager.getClient(invokerConfig, request, selectedClients);
			selectedClients.add(clientSelected);
			try {
				invokeTimes++;
				invocationContext.setClient(clientSelected);
				InvocationResponse response = handler.handle(invocationContext);
				if (lastError != null) {
					logger.warn(
							"Retry method[" + invocationContext.getMethodName() + "] on service["
									+ invokerConfig.getUrl() + "] succeed after " + invokeTimes
									+ " times, last failed invoke's error: " + lastError.getMessage(), lastError);
				}
				return response;
			} catch (Throwable e) {
				// 若指定强制调用某机器，则不再重试
				if (ClientContext.getUseClientAddress() != null) {
					throw e;
				}

				lastError = e;
				if (nextInvokeErrorExit) {
					break;
				}
				if (e instanceof NetTimeoutException) {
					if (!timeoutRetry) {
						throw e;
					} else {
						nextInvokeErrorExit = true; // 超时最多重试一次
					}
				}
			}
		}
		throw new RemoteInvocationException("Invoke method[" + invocationContext.getMethodName() + "] on service["
				+ invokerConfig.getUrl() + "] failed with " + invokeTimes + " times, last error: "
				+ (lastError != null ? lastError.getMessage() : ""),
				lastError != null && lastError.getCause() != null ? lastError.getCause() : lastError);
	}

	@Override
	public String getName() {
		return Constants.CLUSTER_FAILOVER;
	}

}
