/**
 * Dianping.com Inc.
 * Copyright (c) 00-0 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.provider.process.threadpool;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

import com.dianping.pigeon.config.ConfigManagerLoader;
import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.common.exception.RejectedException;
import com.dianping.pigeon.remoting.common.process.ServiceInvocationHandler;
import com.dianping.pigeon.remoting.provider.config.ProviderConfig;
import com.dianping.pigeon.remoting.provider.config.ProviderMethodConfig;
import com.dianping.pigeon.remoting.provider.config.ServerConfig;
import com.dianping.pigeon.remoting.provider.domain.ProviderContext;
import com.dianping.pigeon.remoting.provider.process.AbstractRequestProcessor;
import com.dianping.pigeon.remoting.provider.process.ProviderProcessHandlerFactory;
import com.dianping.pigeon.remoting.provider.service.method.ServiceMethodCache;
import com.dianping.pigeon.remoting.provider.service.method.ServiceMethodFactory;
import com.dianping.pigeon.threadpool.DefaultThreadPool;
import com.dianping.pigeon.threadpool.ThreadPool;
import com.dianping.pigeon.util.CollectionUtils;

public class RequestThreadPoolProcessor extends AbstractRequestProcessor {

	private static final String poolStrategy = ConfigManagerLoader.getConfigManager().getStringValue(
			"pigeon.provider.pool.strategy", "shared");

	private static ThreadPool sharedRequestProcessThreadPool = null;

	private ThreadPool requestProcessThreadPool = null;

	private static ConcurrentHashMap<String, ThreadPool> methodThreadPools = null;

	private int DEFAULT_POOL_ACTIVES = ConfigManagerLoader.getConfigManager().getIntValue(
			"pigeon.provider.pool.actives", 60);

	public RequestThreadPoolProcessor(ServerConfig serverConfig) {
		if ("server".equals(poolStrategy)) {
			requestProcessThreadPool = new DefaultThreadPool("Pigeon-Server-Request-Processor-"
					+ serverConfig.getProtocol() + "-" + serverConfig.getActualPort(), serverConfig.getCorePoolSize(),
					serverConfig.getMaxPoolSize(), new LinkedBlockingQueue<Runnable>(serverConfig.getWorkQueueSize()));
		} else {
			sharedRequestProcessThreadPool = new DefaultThreadPool("Pigeon-Server-Request-Processor",
					serverConfig.getCorePoolSize(), serverConfig.getMaxPoolSize(), new LinkedBlockingQueue<Runnable>(
							serverConfig.getWorkQueueSize()));
			requestProcessThreadPool = sharedRequestProcessThreadPool;
		}
	}

	public void doStop() {
	}

	public Future<InvocationResponse> doProcessRequest(final InvocationRequest request,
			final ProviderContext providerContext) {
		requestContextMap.put(request, providerContext);
		Callable<InvocationResponse> requestExecutor = new Callable<InvocationResponse>() {

			@Override
			public InvocationResponse call() throws Exception {
				try {
					ServiceInvocationHandler invocationHandler = ProviderProcessHandlerFactory
							.selectInvocationHandler(providerContext.getRequest().getMessageType());
					if (invocationHandler != null) {
						providerContext.setThread(Thread.currentThread());
						return invocationHandler.handle(providerContext);
					}
				} catch (Throwable t) {
					logger.error("Process request failed with invocation handler, you should never be here.", t);
				} finally {
					requestContextMap.remove(request);
				}
				return null;
			}
		};

		ThreadPool pool = selectThreadPool(request);
		try {
			return pool.submit(requestExecutor);
		} catch (RejectedExecutionException e) {
			throw new RejectedException(getProcessorStatistics(request), e);
		}
	}

	private ThreadPool selectThreadPool(final InvocationRequest request) {
		ThreadPool pool = null;
		if (!CollectionUtils.isEmpty(methodThreadPools)) {
			pool = methodThreadPools.get(request.getServiceName() + "#" + request.getMethodName());
		}
		if (pool == null) {
			if ("server".equals(poolStrategy)) {
				pool = requestProcessThreadPool;
			} else {
				pool = sharedRequestProcessThreadPool;
			}
		}
		return pool;
	}

	@Override
	public String getProcessorStatistics() {
		StringBuilder stats = new StringBuilder();
		if ("server".equals(poolStrategy)) {
			stats.append("[server=").append(getThreadPoolStatistics(requestProcessThreadPool)).append("]");
		} else {
			stats.append("[shared=").append(getThreadPoolStatistics(sharedRequestProcessThreadPool)).append("]");
		}
		if (!CollectionUtils.isEmpty(methodThreadPools)) {
			for (String key : methodThreadPools.keySet()) {
				stats.append(",[").append(key).append("=").append(getThreadPoolStatistics(methodThreadPools.get(key)))
						.append("]");
			}
		}
		return stats.toString();
	}

	private boolean needStandalonePool(ProviderConfig<?> providerConfig) {
		return !providerConfig.isUseSharedPool() || "method".equals(poolStrategy);
	}

	@Override
	public synchronized <T> void addService(ProviderConfig<T> providerConfig) {
		if (needStandalonePool(providerConfig)) {
			if (methodThreadPools == null) {
				methodThreadPools = new ConcurrentHashMap<String, ThreadPool>();
			}
			String url = providerConfig.getUrl();
			Map<String, ProviderMethodConfig> methodConfigs = providerConfig.getMethods();
			ServiceMethodCache methodCache = ServiceMethodFactory.getServiceMethodCache(url);
			Set<String> methodNames = methodCache.getMethodMap().keySet();
			for (String name : methodNames) {
				String key = url + "#" + name;
				ThreadPool pool = methodThreadPools.get(key);
				if (pool == null) {
					int actives = DEFAULT_POOL_ACTIVES;
					if (methodConfigs != null) {
						ProviderMethodConfig methodConfig = methodConfigs.get(name);
						if (methodConfig != null && methodConfig.getActives() > 0) {
							actives = methodConfig.getActives();
						}
					}
					int coreSize = (actives / 3) > 0 ? (actives / 3) : actives;
					int maxSize = actives;
					int queueSize = (actives / 2) > 0 ? (actives / 2) : actives;
					pool = new DefaultThreadPool("Pigeon-Server-Request-Processor-method", coreSize, maxSize,
							new LinkedBlockingQueue<Runnable>(queueSize));
					methodThreadPools.putIfAbsent(key, pool);
				}
			}
		}
	}

	@Override
	public String getProcessorStatistics(InvocationRequest request) {
		ThreadPool pool = selectThreadPool(request);
		return getThreadPoolStatistics(pool);
	}

	private String getThreadPoolStatistics(ThreadPool pool) {
		if (pool == null) {
			return null;
		}
		ThreadPoolExecutor e = pool.getExecutor();
		String stats = String.format(
				"request pool size:%d(active:%d,core:%d,max:%d,largest:%d),task count:%d(completed:%d),queue size:%d",
				e.getPoolSize(), e.getActiveCount(), e.getCorePoolSize(), e.getMaximumPoolSize(),
				e.getLargestPoolSize(), e.getTaskCount(), e.getCompletedTaskCount(), e.getQueue().size());
		return stats;
	}

	@Override
	public synchronized <T> void removeService(ProviderConfig<T> providerConfig) {
		if (needStandalonePool(providerConfig)) {
			Set<String> toRemoveKeys = new HashSet<String>();
			for (String key : methodThreadPools.keySet()) {
				if (key.startsWith(providerConfig.getUrl() + "#")) {
					toRemoveKeys.add(key);
				}
			}
			for (String key : toRemoveKeys) {
				ThreadPool pool = methodThreadPools.remove(key);
				if (pool != null) {
					pool.getExecutor().shutdown();
				}
			}
		}
	}
}
