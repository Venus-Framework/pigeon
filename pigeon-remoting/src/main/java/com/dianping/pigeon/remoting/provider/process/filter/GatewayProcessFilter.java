/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.provider.process.filter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.dianping.pigeon.config.ConfigChangeListener;
import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.config.ConfigManagerLoader;
import com.dianping.pigeon.extension.ExtensionLoader;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.common.exception.RejectedException;
import com.dianping.pigeon.remoting.common.process.ServiceInvocationFilter;
import com.dianping.pigeon.remoting.common.process.ServiceInvocationHandler;
import com.dianping.pigeon.remoting.provider.domain.ProviderContext;

/**
 * 
 * 
 */
public class GatewayProcessFilter implements ServiceInvocationFilter<ProviderContext> {

	private static final Logger logger = LoggerLoader.getLogger(GatewayProcessFilter.class);
	private static ConfigManager configManager = ExtensionLoader.getExtension(ConfigManager.class);
	private static boolean isAppLimitEnabled = configManager.getBooleanValue("pigeon.provider.applimit.enable", false);
	private static Map<String, Long> appLimitMap = new ConcurrentHashMap<String, Long>();
	private static Map<String, AtomicLong> appRequestsMap = new ConcurrentHashMap<String, AtomicLong>();

	static {
		String appLimitConfig = configManager.getStringValue("pigeon.provider.applimit");
		parseAppLimitConfig(appLimitConfig);
		ConfigManagerLoader.getConfigManager().registerConfigChangeListener(new InnerConfigChangeListener());
	}

	private static void parseAppLimitConfig(String appLimitConfig) {
		try {
			String[] appLimitConfigPair = appLimitConfig.split(",");
			for (String str : appLimitConfigPair) {
				String[] pair = str.split(":");
				appLimitMap.put(pair[0], Long.valueOf(pair[1]));
			}
		} catch (RuntimeException e) {
			logger.error("error while parsing app limit configuration", e);
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
		Long limit = null;
		AtomicLong requests = null;
		if (isAppLimitEnabled && StringUtils.isNotBlank(fromApp) && appLimitMap.containsKey(fromApp)) {
			limit = appLimitMap.get(fromApp);
			if (limit >= 0) {
				requests = appRequestsMap.get(fromApp);
				if (requests == null) {
					requests = new AtomicLong(0);
					appRequestsMap.put(fromApp, requests);
				}
				if (requests.get() + 1 > limit) {
					throw new RejectedException("request from app:" + fromApp + " refused, max requests limit reached:"
							+ limit);
				} else {
					requests.incrementAndGet();
				}
			}
		}
		try {
			response = handler.handle(invocationContext);
			return response;
		} finally {
			if (isAppLimitEnabled && limit != null && limit >= 0 && requests != null) {
				requests.decrementAndGet();
			}
		}
	}

	private static class InnerConfigChangeListener implements ConfigChangeListener {

		@Override
		public void onKeyUpdated(String key, String value) {
			if (key.endsWith("pigeon.provider.applimit")) {
				parseAppLimitConfig(value);
			} else if (key.endsWith("pigeon.provider.applimit.enable")) {
				try {
					isAppLimitEnabled = Boolean.valueOf(value);
				} catch (RuntimeException e) {
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
