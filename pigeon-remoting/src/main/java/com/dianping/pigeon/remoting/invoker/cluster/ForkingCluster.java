package com.dianping.pigeon.remoting.invoker.cluster;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.dianping.dpsf.exception.NetTimeoutException;
import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.extension.ExtensionLoader;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.common.process.ServiceInvocationHandler;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.common.util.InvocationUtils;
import com.dianping.pigeon.remoting.invoker.Client;
import com.dianping.pigeon.remoting.invoker.ClientManager;
import com.dianping.pigeon.remoting.invoker.config.InvokerConfig;
import com.dianping.pigeon.remoting.invoker.domain.InvokerContext;
import com.dianping.pigeon.remoting.invoker.util.InvokerUtils;
import com.dianping.pigeon.threadpool.DefaultThreadPool;
import com.dianping.pigeon.threadpool.ThreadPool;

public class ForkingCluster implements Cluster {

	private ClientManager clientManager = ClientManager.getInstance();
	private static final Logger logger = LoggerLoader.getLogger(FailsafeCluster.class);
	private static final InvocationResponse NO_RETURN_RESPONSE = InvokerUtils.createNoReturnResponse();
	private static final ConfigManager configManager = ExtensionLoader.getExtension(ConfigManager.class);
	private static final int corePoolSize = configManager.getIntValue("pigeon.fork.pool.coresize", 5);
	private static final int maxPoolSize = configManager.getIntValue("pigeon.fork.pool.maxsize", 500);
	private static final int queueSize = configManager.getIntValue("pigeon.fork.pool.queuesize", 500);
	private static ThreadPool forkProcessThreadPool = new DefaultThreadPool("Pigeon-Client-Fork-Processor",
			corePoolSize, maxPoolSize, new LinkedBlockingQueue<Runnable>(queueSize), new CallerRunsPolicy());

	@Override
	public InvocationResponse invoke(final ServiceInvocationHandler handler, final InvokerContext invocationContext)
			throws Throwable {
		InvokerConfig<?> invokerConfig = invocationContext.getInvokerConfig();
		InvocationRequest request = InvokerUtils.createRemoteCallRequest(invocationContext, invokerConfig);
		List<Client> clients = clientManager.getAvailableClients(invokerConfig, request);
		final int size = clients.size();
		final CountDownLatch latch = new CountDownLatch(size);
		List<Future<InvocationResponse>> futures = new ArrayList<Future<InvocationResponse>>(size);
		for (final Client client : clients) {
			futures.add(forkProcessThreadPool.submit(new Callable<InvocationResponse>() {

				@Override
				public InvocationResponse call() throws Exception {
					invocationContext.setClient(client);
					try {
						InvocationResponse resp = handler.handle(invocationContext);
						for (int i = 0; i < size - 1; i++) {
							latch.countDown();
						}
						return resp;
					} catch (Throwable t) {
						logger.error("", t);
						return null;
					} finally {
						latch.countDown();
					}
				}

			}));
		}
		if (request.getTimeout() > 0) {
			latch.await(request.getTimeout(), TimeUnit.MILLISECONDS);
		} else {
			latch.await();
		}
		for (Future<InvocationResponse> future : futures) {
			InvocationResponse result = future.get();
			if (result != null) {
				return result;
			}
		}
		StringBuilder sb = new StringBuilder();
		sb.append("request timeout, current time:").append(System.currentTimeMillis()).append("\r\nrequest:")
				.append(InvocationUtils.toJsonString(request));
		throw new NetTimeoutException(sb.toString());
	}

	@Override
	public String getName() {
		return Constants.CLUSTER_FAILFAST;
	}

}
