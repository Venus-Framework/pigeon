/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.provider.process.filter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.Logger;

import com.dianping.pigeon.config.ConfigChangeListener;
import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.config.ConfigManagerLoader;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.remoting.common.domain.Disposable;
import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.common.exception.RejectedException;
import com.dianping.pigeon.remoting.common.process.ServiceInvocationFilter;
import com.dianping.pigeon.remoting.common.process.ServiceInvocationHandler;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.provider.domain.ProviderContext;
import com.dianping.pigeon.remoting.provider.process.statistics.ProviderStatisticsChecker;
import com.dianping.pigeon.remoting.provider.process.statistics.ProviderStatisticsHolder;
import com.dianping.pigeon.threadpool.DefaultThreadPool;
import com.dianping.pigeon.threadpool.ThreadPool;
import com.dianping.pigeon.util.ThreadPoolUtils;

/**
 * @author xiangwu
 * 
 */
public class GatewayProcessFilter implements ServiceInvocationFilter<ProviderContext>, Disposable {

	private static final Logger logger = LoggerLoader.getLogger(GatewayProcessFilter.class);
	private static final ConfigManager configManager = ConfigManagerLoader.getConfigManager();
	private static final String KEY_APPLIMIT_ENABLE = "pigeon.provider.applimit.enable";
	private static final String KEY_APPLIMIT = "pigeon.provider.applimit";
	private static volatile Map<String, Long> appLimitMap = new ConcurrentHashMap<String, Long>();
	private static ThreadPool statisticsCheckerPool = new DefaultThreadPool("Pigeon-Server-Statistics-Checker");

	static {
		String appLimitConfig = configManager.getStringValue(KEY_APPLIMIT);
		parseAppLimitConfig(appLimitConfig);
		configManager.getBooleanValue(KEY_APPLIMIT_ENABLE, false);
		ConfigManagerLoader.getConfigManager().registerConfigChangeListener(new InnerConfigChangeListener());
		ProviderStatisticsChecker appStatisticsChecker = new ProviderStatisticsChecker();
		statisticsCheckerPool.execute(appStatisticsChecker);
	}

	public void destroy() throws Exception {
		ThreadPoolUtils.shutdown(statisticsCheckerPool.getExecutor());
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
		if (logger.isDebugEnabled()) {
			logger.debug("invoke the GatewayProcessFilter, invocationContext:" + invocationContext);
		}
		InvocationRequest request = invocationContext.getRequest();
		String fromApp = request.getApp();
		InvocationResponse response = null;
		try {
			ProviderStatisticsHolder.flowIn(request);
			if (configManager.getBooleanValue(KEY_APPLIMIT_ENABLE, false) && StringUtils.isNotBlank(fromApp)
					&& appLimitMap.containsKey(fromApp)) {
				Long limit = appLimitMap.get(fromApp);
				if (limit >= 0) {
					long requests = ProviderStatisticsHolder.getCapacityBucket(request).getRequestsInCurrentSecond();
					if (requests + 1 > limit) {
						throw new RejectedException("request from app:" + fromApp
								+ " refused, max requests limit reached:" + limit);
					}
				}
			}
			response = handler.handle(invocationContext);
			return response;
		} finally {
			if (!Constants.REPLY_MANUAL) {
				ProviderStatisticsHolder.flowOut(request);
			}
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
}
