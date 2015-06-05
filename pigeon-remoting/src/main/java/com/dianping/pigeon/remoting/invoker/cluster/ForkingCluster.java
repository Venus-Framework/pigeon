package com.dianping.pigeon.remoting.invoker.cluster;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.dianping.pigeon.log.LoggerLoader;
import org.apache.logging.log4j.Logger;

import com.dianping.dpsf.async.ServiceFutureFactory;
import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.common.process.ServiceInvocationHandler;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.invoker.Client;
import com.dianping.pigeon.remoting.invoker.ClientManager;
import com.dianping.pigeon.remoting.invoker.config.InvokerConfig;
import com.dianping.pigeon.remoting.invoker.domain.DefaultInvokerContext;
import com.dianping.pigeon.remoting.invoker.domain.InvokerContext;
import com.dianping.pigeon.remoting.invoker.util.InvokerUtils;
import com.dianping.pigeon.remoting.invoker.util.InvokerUtils.FutureResponse;
import com.dianping.pigeon.threadpool.NamedThreadFactory;

public class ForkingCluster implements Cluster {

	private ClientManager clientManager = ClientManager.getInstance();
	private static final Logger logger = LoggerLoader.getLogger(ForkingCluster.class);
	private final ExecutorService executor = Executors.newCachedThreadPool(new NamedThreadFactory(
			"Pigeon-Client-Fork-Processor", true));

	@Override
	public InvocationResponse invoke(final ServiceInvocationHandler handler, final InvokerContext invocationContext)
			throws Throwable {
		final InvokerConfig<?> invokerConfig = invocationContext.getInvokerConfig();
		InvocationRequest request = InvokerUtils.createRemoteCallRequest(invocationContext, invokerConfig);
		final List<Client> clients = clientManager.getAvailableClients(invokerConfig, request);
		final AtomicInteger count = new AtomicInteger();
		final BlockingQueue<Object> ref = new LinkedBlockingQueue<Object>();

		for (final Client client : clients) {
			executor.execute(new Runnable() {
				public void run() {
					InvokerContext ctxt = new DefaultInvokerContext(invokerConfig, invocationContext.getMethodName(),
							invocationContext.getParameterTypes(), invocationContext.getArguments());
					ctxt.setClient(client);
					ctxt.setRequest(null);
					ctxt.setRequest(InvokerUtils.createRemoteCallRequest(ctxt, invokerConfig));
					try {
						InvocationResponse resp = handler.handle(ctxt);
						ref.offer(resp);
					} catch (Throwable e) {
						int value = count.incrementAndGet();
						if (value >= clients.size()) {
							ref.offer(e);
						}
					}
				}
			});
		}
		Object ret = null;
		if (request.getTimeout() > 0) {
			ret = ref.poll(request.getTimeout(), TimeUnit.MILLISECONDS);
		} else {
			ret = ref.poll();
		}
		if (ret instanceof Throwable) {
			throw (Throwable) ret;
		} else if ((ret instanceof FutureResponse)
				&& Constants.CALL_FUTURE.equalsIgnoreCase(invokerConfig.getCallType())) {
			ServiceFutureFactory.setFuture(((FutureResponse) ret).getServiceFuture());
		}
		return (InvocationResponse) ret;
	}

	@Override
	public String getName() {
		return Constants.CLUSTER_FORKING;
	}

}
