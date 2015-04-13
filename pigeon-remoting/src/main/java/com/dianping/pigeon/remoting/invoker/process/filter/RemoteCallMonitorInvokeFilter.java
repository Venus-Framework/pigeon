/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.invoker.process.filter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.util.CollectionUtils;

import com.dianping.dpsf.exception.NetTimeoutException;
import com.dianping.pigeon.config.ConfigChangeListener;
import com.dianping.pigeon.config.ConfigManagerLoader;
import com.dianping.pigeon.extension.ExtensionLoader;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.monitor.Monitor;
import com.dianping.pigeon.monitor.MonitorLogger;
import com.dianping.pigeon.monitor.MonitorTransaction;
import com.dianping.pigeon.registry.RegistryManager;
import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.common.monitor.MonitorHelper;
import com.dianping.pigeon.remoting.common.monitor.SizeMonitor;
import com.dianping.pigeon.remoting.common.monitor.SizeMonitor.SizeMonitorInfo;
import com.dianping.pigeon.remoting.common.process.ServiceInvocationHandler;
import com.dianping.pigeon.remoting.common.util.InvocationUtils;
import com.dianping.pigeon.remoting.common.util.TimelineManager;
import com.dianping.pigeon.remoting.common.util.TimelineManager.Timeline;
import com.dianping.pigeon.remoting.invoker.Client;
import com.dianping.pigeon.remoting.invoker.config.InvokerConfig;
import com.dianping.pigeon.remoting.invoker.domain.InvokerContext;

public class RemoteCallMonitorInvokeFilter extends InvocationInvokeFilter {

	private static final Logger logger = LoggerLoader.getLogger(RemoteCallMonitorInvokeFilter.class);

	private Monitor monitor = ExtensionLoader.getExtension(Monitor.class);

	private static Map<String, Integer> appLogTimeoutPeriodMap = new ConcurrentHashMap<String, Integer>();

	private static final int logTimeoutPeriodLimit = ConfigManagerLoader.getConfigManager().getIntValue(
			"pigeon.invoker.log.timeout.period.limit", 9999);

	private static Map<String, AtomicInteger> appTimeouts = new ConcurrentHashMap<String, AtomicInteger>();

	private static final String KEY_LOG_TIMEOUT_PERIOD = "pigeon.invoker.log.timeout.period.apps";

	private static boolean isLogParameters = ConfigManagerLoader.getConfigManager().getBooleanValue(
			"pigeon.provider.log.parameters", true);

	private static class InnerConfigChangeListener implements ConfigChangeListener {

		@Override
		public void onKeyUpdated(String key, String value) {
			if (key.endsWith(KEY_LOG_TIMEOUT_PERIOD)) {
				try {
					parseAppLogTimeoutPeriod(value);
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

	private static void parseAppLogTimeoutPeriod(String appLogTimeoutPeriod) {
		Map<String, Integer> configMap = new ConcurrentHashMap<String, Integer>();
		Map<String, AtomicInteger> timeoutsMap = new ConcurrentHashMap<String, AtomicInteger>();
		if (StringUtils.isNotBlank(appLogTimeoutPeriod)) {
			String[] appArray = appLogTimeoutPeriod.split(",");
			for (String appConfig : appArray) {
				if (StringUtils.isNotBlank(appConfig) && appConfig.indexOf(":") != -1) {
					String[] appPeriodArray = appConfig.split(":");
					String app = appPeriodArray[0];
					int period = Integer.parseInt(appPeriodArray[1]);
					configMap.put(app, period);
					timeoutsMap.put(app, new AtomicInteger(0));
				}
			}
		}
		if (!CollectionUtils.isEmpty(configMap)) {
			appLogTimeoutPeriodMap.clear();
			appLogTimeoutPeriodMap = configMap;
			appTimeouts.clear();
			appTimeouts = timeoutsMap;
		}
	}

	public RemoteCallMonitorInvokeFilter() {
		String appLogTimeoutPeriod = ConfigManagerLoader.getConfigManager().getStringValue(KEY_LOG_TIMEOUT_PERIOD, "");
		parseAppLogTimeoutPeriod(appLogTimeoutPeriod);
		ConfigManagerLoader.getConfigManager().registerConfigChangeListener(new InnerConfigChangeListener());
	}

	@Override
	public InvocationResponse invoke(ServiceInvocationHandler handler, InvokerContext invocationContext)
			throws Throwable {
		if (logger.isDebugEnabled()) {
			logger.debug("invoke the RemoteCallMonitorInvokeFilter, invocationContext:" + invocationContext);
		}
		MonitorLogger logger = null;
		MonitorTransaction transaction = null;
		InvocationRequest request = invocationContext.getRequest();
		String targetApp = null;
		if (monitor != null) {
			InvokerConfig<?> invokerConfig = invocationContext.getInvokerConfig();
			logger = monitor.getLogger();
			if (logger != null) {
				try {
					transaction = logger.createTransaction(
							"PigeonCall",
							InvocationUtils.getRemoteCallFullName(invokerConfig.getUrl(),
									invocationContext.getMethodName(), invocationContext.getParameterTypes()),
							invocationContext);
					if (transaction != null) {
						transaction.setStatusOk();
						transaction.addData("CallType", invokerConfig.getCallType());
						transaction.addData("Timeout", invokerConfig.getTimeout());
						transaction.addData("Serialize", invokerConfig.getSerialize());

						Client client = invocationContext.getClient();
						targetApp = RegistryManager.getInstance().getServerApp(client.getAddress());
						logger.logEvent("PigeonCall.app", targetApp, "");

						String parameters = "";
						if (isLogParameters) {
							parameters = InvocationUtils.toJsonString(request.getParameters(), 1000, 50);
						}
						logger.logEvent("PigeonCall.server", client.getAddress(), parameters);

						if (SizeMonitor.isEnable()) {
							SizeMonitorInfo sizeInfo = MonitorHelper.getSize();
							if (sizeInfo != null) {
								SizeMonitor.getInstance().logSize(sizeInfo.getSize(), sizeInfo.getEvent(),
										client.getAddress());
							}
						}
						transaction.readMonitorContext();
					}
				} catch (Throwable e) {
					logger.logMonitorError(e);
				}
			}
		}
		try {
			return handler.handle(invocationContext);
		} catch (NetTimeoutException e) {
			boolean isLog = false;
			int logTimeoutPeriod = 0;
			if (appLogTimeoutPeriodMap.containsKey(targetApp)) {
				logTimeoutPeriod = appLogTimeoutPeriodMap.get(targetApp);
			}
			if (logTimeoutPeriod > 0) {
				if (logTimeoutPeriod <= logTimeoutPeriodLimit) {
					AtomicInteger timeouts = appTimeouts.get(targetApp);
					if (timeouts != null && timeouts.incrementAndGet() > logTimeoutPeriod) {
						isLog = true;
						timeouts.set(0);
					}
				}
			} else {
				isLog = true;
			}
			if (isLog) {
				if (transaction != null) {
					try {
						transaction.setStatusError(e);
					} catch (Throwable e2) {
						logger.logMonitorError(e2);
					}
				}
				if (logger != null) {
					logger.logError(e);
				}
			}
			throw e;
		} catch (Throwable e) {
			if (transaction != null) {
				try {
					transaction.setStatusError(e);
				} catch (Throwable e2) {
					logger.logMonitorError(e2);
				}
			}
			if (logger != null) {
				logger.logError(e);
			}
			throw e;
		} finally {
			if (transaction != null) {
				try {
					if (TimelineManager.isEnabled()) {
						Timeline timeline = TimelineManager.getTimeline(request, TimelineManager.getLocalIp());
						transaction.addData("Timeline", timeline);
					}
					transaction.complete();
				} catch (Throwable e) {
					logger.logMonitorError(e);
				}
			}
		}
	}

}
