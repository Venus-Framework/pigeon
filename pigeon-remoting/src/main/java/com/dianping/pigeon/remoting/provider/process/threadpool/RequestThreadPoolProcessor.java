/**
 * Dianping.com Inc.
 * Copyright (c) 00-0 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.provider.process.threadpool;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.dianping.pigeon.config.ConfigChangeListener;
import com.dianping.pigeon.config.ConfigManagerLoader;
import com.dianping.pigeon.log.LoggerLoader;
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

	private static final Logger logger = LoggerLoader.getLogger(RequestThreadPoolProcessor.class);

	private static final String poolStrategy = ConfigManagerLoader.getConfigManager().getStringValue(
			"pigeon.provider.pool.strategy", "shared");

	private static ThreadPool sharedRequestProcessThreadPool = null;

	private ThreadPool requestProcessThreadPool = null;

	private static ConcurrentHashMap<String, ThreadPool> methodThreadPools = null;

	private static ConcurrentHashMap<String, ThreadPool> serviceThreadPools = null;

	private static int DEFAULT_POOL_ACTIVES = ConfigManagerLoader.getConfigManager().getIntValue(
			"pigeon.provider.pool.actives", 60);

	private static float DEFAULT_POOL_RATIO_CORE = ConfigManagerLoader.getConfigManager().getFloatValue(
			"pigeon.provider.pool.ratio.coresize", 3f);

	private static float DEFAULT_POOL_RATIO_QUEUE = ConfigManagerLoader.getConfigManager().getFloatValue(
			"pigeon.provider.pool.ratio.workqueue", 1f);

	private static float cancelRatio = ConfigManagerLoader.getConfigManager().getFloatValue(
			"pigeon.timeout.cancelratio", 1f);

	private static int waitTimeOfClosePool = ConfigManagerLoader.getConfigManager().getIntValue(
			"pigeon.provider.pool.waittimeofclose", 5);

	public static Map<String, String> methodPoolConfigKeys = new HashMap<String, String>();

	public static String sharedPoolCoreSizeKey = null;

	public static String sharedPoolMaxSizeKey = null;

	public static String sharedPoolQueueSizeKey = null;

	static {
		ConfigManagerLoader.getConfigManager().registerConfigChangeListener(new InnerConfigChangeListener());
	}

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
			requestContextMap.remove(request);
			throw new RejectedException(getProcessorStatistics(request), e);
		}
	}

	private ThreadPool selectThreadPool(final InvocationRequest request) {
		ThreadPool pool = null;
		if (!CollectionUtils.isEmpty(methodThreadPools)) {
			pool = methodThreadPools.get(request.getServiceName() + "#" + request.getMethodName());
		}
		if (!CollectionUtils.isEmpty(serviceThreadPools)) {
			pool = serviceThreadPools.get(request.getServiceName());
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
		if (!CollectionUtils.isEmpty(serviceThreadPools)) {
			for (String key : serviceThreadPools.keySet()) {
				stats.append(",[").append(key).append("=").append(getThreadPoolStatistics(serviceThreadPools.get(key)))
						.append("]");
			}
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
			if (serviceThreadPools == null) {
				serviceThreadPools = new ConcurrentHashMap<String, ThreadPool>();
			}
			String url = providerConfig.getUrl();
			Map<String, ProviderMethodConfig> methodConfigs = providerConfig.getMethods();
			ServiceMethodCache methodCache = ServiceMethodFactory.getServiceMethodCache(url);
			Set<String> methodNames = methodCache.getMethodMap().keySet();
			if (CollectionUtils.isEmpty(methodConfigs)) {
				String key = url;
				ThreadPool pool = serviceThreadPools.get(key);
				if (pool == null) {
					int actives = DEFAULT_POOL_ACTIVES;
					if (providerConfig.getActives() > 0) {
						actives = providerConfig.getActives();
					}
					int coreSize = (int) (actives / DEFAULT_POOL_RATIO_CORE) > 0 ? (int) (actives / DEFAULT_POOL_RATIO_CORE)
							: actives;
					int maxSize = actives;
					int queueSize = (int) (actives / DEFAULT_POOL_RATIO_QUEUE) > 0 ? (int) (actives / DEFAULT_POOL_RATIO_QUEUE)
							: actives;
					pool = new DefaultThreadPool("Pigeon-Server-Request-Processor-service", coreSize, maxSize,
							new LinkedBlockingQueue<Runnable>(queueSize));
					serviceThreadPools.putIfAbsent(key, pool);
				}
			} else {
				for (String name : methodNames) {
					if (!methodConfigs.containsKey(name)) {
						continue;
					}
					String key = url + "#" + name;
					ThreadPool pool = methodThreadPools.get(key);
					if (pool == null) {
						int actives = DEFAULT_POOL_ACTIVES;
						ProviderMethodConfig methodConfig = methodConfigs.get(name);
						if (methodConfig != null && methodConfig.getActives() > 0) {
							actives = methodConfig.getActives();
						}
						int coreSize = (int) (actives / DEFAULT_POOL_RATIO_CORE) > 0 ? (int) (actives / DEFAULT_POOL_RATIO_CORE)
								: actives;
						int maxSize = actives;
						int queueSize = (int) (actives / DEFAULT_POOL_RATIO_QUEUE) > 0 ? (int) (actives / DEFAULT_POOL_RATIO_QUEUE)
								: actives;
						pool = new DefaultThreadPool("Pigeon-Server-Request-Processor-method", coreSize, maxSize,
								new LinkedBlockingQueue<Runnable>(queueSize));
						methodThreadPools.putIfAbsent(key, pool);
					}
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
			ThreadPool pool = serviceThreadPools.remove(providerConfig.getUrl());
			if (pool != null) {
				pool.getExecutor().shutdown();
			}
		}
	}

	@Override
	public boolean needCancelRequest(InvocationRequest request) {
		ThreadPool pool = selectThreadPool(request);
		return pool.getExecutor().getPoolSize() >= pool.getExecutor().getMaximumPoolSize() * cancelRatio;
	}

	@Override
	public void doStart() {

	}

	private static class InnerConfigChangeListener implements ConfigChangeListener {

		@Override
		public void onKeyUpdated(String key, String value) {
			if (key.endsWith("pigeon.provider.pool.waittimeofclose")) {
				waitTimeOfClosePool = Integer.valueOf(value);
			} else if (key.endsWith("pigeon.timeout.cancelratio")) {
				cancelRatio = Float.valueOf(value);
			} else if (key.endsWith("pigeon.provider.pool.ratio.core")) {
				DEFAULT_POOL_RATIO_CORE = Integer.valueOf(value);
			} else if (key.endsWith("pigeon.provider.pool.ratio.queue")) {
				DEFAULT_POOL_RATIO_QUEUE = Integer.valueOf(value);
			} else if (StringUtils.isNotBlank(sharedPoolCoreSizeKey) && key.endsWith(sharedPoolCoreSizeKey)) {
				int size = Integer.valueOf(value);
				if (size != sharedRequestProcessThreadPool.getExecutor().getCorePoolSize() && size >= 0) {
					try {
						ThreadPool oldPool = sharedRequestProcessThreadPool;
						int queueSize = oldPool.getExecutor().getQueue().remainingCapacity()
								+ oldPool.getExecutor().getQueue().size();
						ThreadPool newPool = new DefaultThreadPool("Pigeon-Server-Request-Processor-method", size,
								oldPool.getExecutor().getMaximumPoolSize(),
								new LinkedBlockingQueue<Runnable>(queueSize));
						sharedRequestProcessThreadPool = newPool;
						try {
							oldPool.getExecutor().shutdown();
							oldPool.getExecutor().awaitTermination(waitTimeOfClosePool, TimeUnit.SECONDS);
							oldPool = null;
						} catch (Throwable e) {
							logger.warn("error when shuting down old shared pool", e);
						}
						if (logger.isInfoEnabled()) {
							logger.info("changed shared pool, key:" + key + ", value:" + value);
						}
					} catch (RuntimeException e) {
						logger.error("error while changing shared pool, key:" + key + ", value:" + value, e);
					}
				}
			} else if (StringUtils.isNotBlank(sharedPoolMaxSizeKey) && key.endsWith(sharedPoolMaxSizeKey)) {
				int size = Integer.valueOf(value);
				if (size != sharedRequestProcessThreadPool.getExecutor().getMaximumPoolSize() && size >= 0) {
					try {
						ThreadPool oldPool = sharedRequestProcessThreadPool;
						int queueSize = oldPool.getExecutor().getQueue().remainingCapacity()
								+ oldPool.getExecutor().getQueue().size();
						ThreadPool newPool = new DefaultThreadPool("Pigeon-Server-Request-Processor-method", oldPool
								.getExecutor().getCorePoolSize(), size, new LinkedBlockingQueue<Runnable>(queueSize));
						sharedRequestProcessThreadPool = newPool;
						try {
							oldPool.getExecutor().shutdown();
							oldPool.getExecutor().awaitTermination(waitTimeOfClosePool, TimeUnit.SECONDS);
							oldPool = null;
						} catch (Throwable e) {
							logger.warn("error when shuting down old shared pool", e);
						}
						if (logger.isInfoEnabled()) {
							logger.info("changed shared pool, key:" + key + ", value:" + value);
						}
					} catch (RuntimeException e) {
						logger.error("error while changing shared pool, key:" + key + ", value:" + value, e);
					}
				}
			} else if (StringUtils.isNotBlank(sharedPoolQueueSizeKey) && key.endsWith(sharedPoolQueueSizeKey)) {
				int size = Integer.valueOf(value);
				ThreadPool oldPool = sharedRequestProcessThreadPool;
				int queueSize = oldPool.getExecutor().getQueue().remainingCapacity()
						+ oldPool.getExecutor().getQueue().size();
				if (size != queueSize && size >= 0) {
					try {
						ThreadPool newPool = new DefaultThreadPool("Pigeon-Server-Request-Processor-method", oldPool
								.getExecutor().getCorePoolSize(), oldPool.getExecutor().getMaximumPoolSize(),
								new LinkedBlockingQueue<Runnable>(size));
						sharedRequestProcessThreadPool = newPool;
						try {
							oldPool.getExecutor().shutdown();
							oldPool.getExecutor().awaitTermination(waitTimeOfClosePool, TimeUnit.SECONDS);
							oldPool = null;
						} catch (Throwable e) {
							logger.warn("error when shuting down old shared pool", e);
						}
						if (logger.isInfoEnabled()) {
							logger.info("changed shared pool, key:" + key + ", value:" + value);
						}
					} catch (RuntimeException e) {
						logger.error("error while changing shared pool, key:" + key + ", value:" + value, e);
					}
				}
			} else {
				for (String k : methodPoolConfigKeys.keySet()) {
					String v = methodPoolConfigKeys.get(k);
					if (key.endsWith(v)) {
						try {
							String serviceKey = k;
							if (StringUtils.isNotBlank(serviceKey)) {
								ThreadPool pool = null;
								if (!CollectionUtils.isEmpty(methodThreadPools)) {
									pool = methodThreadPools.get(serviceKey);
									int actives = Integer.valueOf(value);
									if (pool != null && actives != pool.getExecutor().getMaximumPoolSize()
											&& actives >= 0) {
										int coreSize = (int) (actives / DEFAULT_POOL_RATIO_CORE) > 0 ? (int) (actives / DEFAULT_POOL_RATIO_CORE)
												: actives;
										int queueSize = (int) (actives / DEFAULT_POOL_RATIO_QUEUE) > 0 ? (int) (actives / DEFAULT_POOL_RATIO_QUEUE)
												: actives;
										int maxSize = actives;
										ThreadPool newPool = new DefaultThreadPool(
												"Pigeon-Server-Request-Processor-method", coreSize, maxSize,
												new LinkedBlockingQueue<Runnable>(queueSize));
										methodThreadPools.put(serviceKey, newPool);
										try {
											pool.getExecutor().shutdown();
											pool.getExecutor().awaitTermination(waitTimeOfClosePool, TimeUnit.SECONDS);
											pool = null;
										} catch (Throwable e) {
											logger.warn("error when shuting down old method pool", e);
										}
										if (logger.isInfoEnabled()) {
											logger.info("changed method pool, key:" + serviceKey + ", value:" + actives);
										}
									}
								}
							}
						} catch (RuntimeException e) {
							logger.error("error while changing method pool, key:" + key + ", value:" + value, e);
						}
					}
				}
			}
		}

		@Override
		public void onKeyAdded(String key, String value) {
		}

		@Override
		public void onKeyRemoved(String key) {
		}

	}
}
