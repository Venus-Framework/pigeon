/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.provider.process.filter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.dianping.pigeon.remoting.common.codec.json.JacksonSerializer;
import com.google.common.collect.Maps;
import org.apache.commons.lang.StringUtils;
import com.dianping.pigeon.log.Logger;

import com.dianping.pigeon.config.ConfigChangeListener;
import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.config.ConfigManagerLoader;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.remoting.common.domain.Disposable;
import com.dianping.pigeon.remoting.common.domain.InvocationContext.TimePhase;
import com.dianping.pigeon.remoting.common.domain.InvocationContext.TimePoint;
import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.common.exception.RejectedException;
import com.dianping.pigeon.remoting.common.process.ServiceInvocationFilter;
import com.dianping.pigeon.remoting.common.process.ServiceInvocationHandler;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.provider.config.ProviderConfig;
import com.dianping.pigeon.remoting.provider.domain.ProviderContext;
import com.dianping.pigeon.remoting.provider.process.statistics.ProviderStatisticsChecker;
import com.dianping.pigeon.remoting.provider.process.statistics.ProviderStatisticsHolder;
import com.dianping.pigeon.remoting.provider.publish.ServiceChangeListener;
import com.dianping.pigeon.remoting.provider.publish.ServiceChangeListenerContainer;
import com.dianping.pigeon.remoting.provider.service.method.ServiceMethodCache;
import com.dianping.pigeon.remoting.provider.service.method.ServiceMethodFactory;
import com.dianping.pigeon.threadpool.DefaultThreadPool;
import com.dianping.pigeon.threadpool.ThreadPool;
import com.dianping.pigeon.util.CollectionUtils;
import com.dianping.pigeon.util.ThreadPoolUtils;

/**
 * @author xiangwu
 * 
 */
public class GatewayProcessFilter implements ServiceInvocationFilter<ProviderContext>, Disposable {

	private static final Logger logger = LoggerLoader.getLogger(GatewayProcessFilter.class);
	private static final ConfigManager configManager = ConfigManagerLoader.getConfigManager();
	private static final String KEY_APPLIMIT_ENABLE = "pigeon.provider.applimit.enable";
	private static final String KEY_METHODAPPLIMIT_ENABLE = "pigeon.provider.methodapplimit.enable";
	private static final String KEY_METHODLIMIT_ENABLE = "pigeon.provider.methodlimit.enable";
	private static final String KEY_APPLIMIT = "pigeon.provider.applimit";
	private static final String KEY_METHODAPPLIMIT = "pigeon.provider.methodapplimit";
	private static volatile Map<String, Long> appLimitMap = new ConcurrentHashMap<String, Long>();
	// api#method --> [app1 --> qpslimit, app2 --> qpslimit]
	private static volatile Map<String, List<Map<String, Long>>> methodAppLimitMap = Maps.newConcurrentMap();
	private static final JacksonSerializer jacksonSerializer = new JacksonSerializer();
	private static ThreadPool statisticsCheckerPool = new DefaultThreadPool("Pigeon-Server-Statistics-Checker");
	private static final ConcurrentHashMap<String, AtomicInteger> methodActives = new ConcurrentHashMap<String, AtomicInteger>();
	private static final AtomicInteger total = new AtomicInteger();
	private static final int MAX_THREADS = ConfigManagerLoader.getConfigManager().getIntValue(
			"pigeon.provider.pool.method.maxthreads", 100);

	static {
		String appLimitConfig = configManager.getStringValue(KEY_APPLIMIT);
		parseAppLimitConfig(appLimitConfig);
		configManager.getBooleanValue(KEY_APPLIMIT_ENABLE, false);
		configManager.getBooleanValue(KEY_METHODLIMIT_ENABLE, true);
		ConfigManagerLoader.getConfigManager().registerConfigChangeListener(new InnerConfigChangeListener());
		ProviderStatisticsChecker appStatisticsChecker = new ProviderStatisticsChecker();
		statisticsCheckerPool.execute(appStatisticsChecker);
		ServiceChangeListenerContainer.addServiceChangeListener(new InnerServiceChangeListener());
	}

	public void destroy() throws Exception {
		ThreadPoolUtils.shutdown(statisticsCheckerPool.getExecutor());
	}

	private static void parseMethodAppLimitConfig(String methodAppLimitConfig) {
		if (StringUtils.isNotBlank(methodAppLimitConfig)) {
			Map<String, List<Map<String, Long>>> map = Maps.newConcurrentMap();
			try {
				map = (HashMap) jacksonSerializer.toObject(HashMap.class, methodAppLimitConfig);
				methodAppLimitMap.clear();
				methodAppLimitMap = new ConcurrentHashMap<>(map);
			} catch (Throwable t) {
				logger.error("error while parsing method app limit configuration:" + methodAppLimitConfig, t);
			}
		}
	}

	private static void parseAppLimitConfig(String appLimitConfig) {
		if (StringUtils.isNotBlank(appLimitConfig)) {
			ConcurrentHashMap<String, Long> map = new ConcurrentHashMap<String, Long>();
			try {
				String[] appLimitConfigPair = appLimitConfig.split(",");
				for (String str : appLimitConfigPair) {
					if (StringUtils.isNotBlank(str)) {
						String[] pair = str.split(":");
						if (pair != null && pair.length == 2) {
							map.put(pair[0], Long.valueOf(pair[1]));
						}
					}
				}
				appLimitMap.clear();
				appLimitMap = map;
			} catch (RuntimeException e) {
				logger.error("error while parsing app limit configuration:" + appLimitConfig, e);
			}
		}
	}

	@Override
	public InvocationResponse invoke(ServiceInvocationHandler handler, ProviderContext invocationContext)
			throws Throwable {
		invocationContext.getTimeline().add(new TimePoint(TimePhase.G));
		InvocationRequest request = invocationContext.getRequest();
		String fromApp = request.getApp();
		InvocationResponse response = null;
		final String requestMethod = request.getServiceName() + "#" + request.getMethodName();
		final boolean enableMethodLimit = configManager.getBooleanValue(KEY_METHODLIMIT_ENABLE, true);
		try {
			ProviderStatisticsHolder.flowIn(request);
			if (Constants.MESSAGE_TYPE_SERVICE == request.getMessageType()) {
				if (enableMethodLimit) {
					incrementRequest(requestMethod);
				}
				//todo

				if (configManager.getBooleanValue(KEY_APPLIMIT_ENABLE, false) && StringUtils.isNotBlank(fromApp)
						&& appLimitMap.containsKey(fromApp)) {
					Long limit = appLimitMap.get(fromApp);
					if (limit >= 0) {
						long requests = ProviderStatisticsHolder.getCapacityBucket(request)
								.getRequestsInCurrentSecond();
						if (requests + 1 > limit) {
							throw new RejectedException(String.format(
									"Max requests limit %s reached for request from app:%s", limit, fromApp));
						}
					}
				}
			}
			response = handler.handle(invocationContext);
			return response;
		} finally {
			if (Constants.MESSAGE_TYPE_SERVICE == request.getMessageType() && enableMethodLimit) {
				decrementRequest(requestMethod);
			}
			if (!Constants.REPLY_MANUAL) {
				ProviderStatisticsHolder.flowOut(request);
			}
		}
	}

	public static String getStatistics() {
		StringBuilder stats = new StringBuilder();
		if (!CollectionUtils.isEmpty(methodActives)) {
			stats.append(",[method actives=[");
			for (String key : methodActives.keySet()) {
				stats.append("[").append(key).append("=").append(methodActives.get(key)).append("]");
			}
			stats.append("]");
		}
		return stats.toString();
	}

	public static void checkRequest(final InvocationRequest request) {
		if (Constants.MESSAGE_TYPE_SERVICE == request.getMessageType()
				&& configManager.getBooleanValue(KEY_METHODLIMIT_ENABLE, true)) {
			final String requestMethod = request.getServiceName() + "#" + request.getMethodName();
			AtomicInteger count = methodActives.get(requestMethod);
			if (count != null) {
				int limit = getMaxThreadsForMethod(requestMethod, count.get());
				if (count.get() > limit) {
					throw new RejectedException(String.format(
							"Reached the maximum limit %s for method: %s, current: %s", limit, requestMethod,
							count.get()));
				}
			}
		}
	}

	private static int getMaxThreadsForMethod(String requestMethod, int requestMethodThreadCount) {
		int totalThreads = total.get();
		int limit = MAX_THREADS > totalThreads ? MAX_THREADS - totalThreads + requestMethodThreadCount
				: requestMethodThreadCount;
		if (limit > MAX_THREADS - 20) {
			limit = MAX_THREADS - 20;
		}
		return limit;
	}

	private static void incrementRequest(String requestMethod) {
		total.incrementAndGet();
		AtomicInteger count = methodActives.get(requestMethod);
		if (count != null) {
			int limit = getMaxThreadsForMethod(requestMethod, count.get());
			if (count.incrementAndGet() > limit) {
				throw new RejectedException(String.format("Reached the maximum limit %s for method: %s, current: %s",
						limit, requestMethod, count.get()));
			}
		}
	}

	private static void decrementRequest(String requestMethod) {
		total.decrementAndGet();
		AtomicInteger count = methodActives.get(requestMethod);
		if (count != null) {
			count.decrementAndGet();
		}
	}

	private static class InnerConfigChangeListener implements ConfigChangeListener {

		@Override
		public void onKeyUpdated(String key, String value) {
			if (key.endsWith(KEY_APPLIMIT)) {
				parseAppLimitConfig(value);
			}
		}

		@Override
		public void onKeyAdded(String key, String value) {

		}

		@Override
		public void onKeyRemoved(String key) {

		}

	}

	private static class InnerServiceChangeListener implements ServiceChangeListener {

		@Override
		public void notifyServicePublished(ProviderConfig<?> providerConfig) {
		}

		@Override
		public void notifyServiceUnpublished(ProviderConfig<?> providerConfig) {
		}

		@Override
		public void notifyServiceOnline(ProviderConfig<?> providerConfig) {
		}

		@Override
		public void notifyServiceOffline(ProviderConfig<?> providerConfig) {
		}

		@Override
		public void notifyServiceAdded(ProviderConfig<?> providerConfig) {
			String url = providerConfig.getUrl();
			ServiceMethodCache methodCache = ServiceMethodFactory.getServiceMethodCache(url);
			Set<String> methodNames = methodCache.getMethodMap().keySet();
			for (String method : methodNames) {
				methodActives.put(url + "#" + method, new AtomicInteger());
			}
		}

		@Override
		public void notifyServiceRemoved(ProviderConfig<?> providerConfig) {
			String url = providerConfig.getUrl();
			ServiceMethodCache methodCache = ServiceMethodFactory.getServiceMethodCache(url);
			Set<String> methodNames = methodCache.getMethodMap().keySet();
			for (String method : methodNames) {
				methodActives.remove(url + "#" + method);
			}
		}

	}
}
