/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.invoker.process;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang.StringUtils;
import org.springframework.util.CollectionUtils;

import com.dianping.pigeon.config.ConfigChangeListener;
import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.config.ConfigManagerLoader;
import com.dianping.pigeon.log.Logger;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.monitor.Monitor;
import com.dianping.pigeon.monitor.MonitorLoader;
import com.dianping.pigeon.monitor.MonitorTransaction;
import com.dianping.pigeon.registry.RegistryManager;
import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.common.exception.RpcException;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.invoker.exception.RequestTimeoutException;
import com.dianping.pigeon.remoting.invoker.util.InvokerHelper;
import com.dianping.pigeon.remoting.invoker.util.InvokerUtils;

/**
 * @author xiangwu
 * 
 */
public enum ExceptionManager {

	INSTANCE;

	private static final Logger logger = LoggerLoader.getLogger(ExceptionManager.class);
	private static final ConfigManager configManager = ConfigManagerLoader.getConfigManager();
	private static final Monitor monitor = MonitorLoader.getMonitor();

	private static volatile Map<String, Integer> appLogTimeoutPeriodMap = new ConcurrentHashMap<String, Integer>();
	private static final int logTimeoutPeriodLimit = ConfigManagerLoader.getConfigManager()
			.getIntValue("pigeon.invoker.log.timeout.period.limit", 9999);
	private static volatile Map<String, AtomicInteger> appTimeouts = new ConcurrentHashMap<String, AtomicInteger>();
	private static final String KEY_LOG_TIMEOUT_PERIOD = "pigeon.invoker.log.timeout.period.apps";
	private static final String KEY_LOG_EXCEPTION_IGNORED = "pigeon.invoker.log.exception.ignored";
	private static volatile Set<String> ignoredLogExceptions = Collections
			.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
	private static final String KEY_LOG_SERVICE_IGNORED = "pigeon.invoker.log.service.ignored";
	private static volatile Set<String> ignoredLogServices = Collections
			.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
	private static final String KEY_LOG_SERVICE_EXCEPTION = "pigeon.invoker.log.bizexception.enable";

	private ExceptionManager() {
	}

	static {
		String appLogTimeoutPeriodConfig = ConfigManagerLoader.getConfigManager().getStringValue(KEY_LOG_TIMEOUT_PERIOD,
				"");
		parseAppLogTimeoutPeriod(appLogTimeoutPeriodConfig);

		String ignoredLogExceptionsConfig = ConfigManagerLoader.getConfigManager()
				.getStringValue(KEY_LOG_EXCEPTION_IGNORED, "");
		parseIgnoredLogExceptions(ignoredLogExceptionsConfig);

		String ignoredLogServicesConfig = ConfigManagerLoader.getConfigManager().getStringValue(KEY_LOG_SERVICE_IGNORED,
				"");
		parseIgnoredLogServices(ignoredLogServicesConfig);
		ConfigManagerLoader.getConfigManager().registerConfigChangeListener(new InnerConfigChangeListener());
		ConfigManagerLoader.getConfigManager().getBooleanValue(KEY_LOG_SERVICE_EXCEPTION, false);
	}

	private static class InnerConfigChangeListener implements ConfigChangeListener {

		@Override
		public void onKeyUpdated(String key, String value) {
			if (key.endsWith(KEY_LOG_TIMEOUT_PERIOD)) {
				try {
					parseAppLogTimeoutPeriod(value);
				} catch (RuntimeException e) {
				}
			} else if (key.endsWith(KEY_LOG_EXCEPTION_IGNORED)) {
				try {
					parseIgnoredLogExceptions(value);
				} catch (RuntimeException e) {
				}
			} else if (key.endsWith(KEY_LOG_SERVICE_IGNORED)) {
				try {
					parseIgnoredLogServices(value);
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

	private static void parseIgnoredLogExceptions(String config) {
		Set<String> set = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
		String[] exceptions = config.split(",");
		for (String ex : exceptions) {
			if (StringUtils.isNotBlank(ex)) {
				set.add(ex);
			}
		}
		ignoredLogExceptions = set;
	}

	private static void parseIgnoredLogServices(String config) {
		Set<String> set = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
		String[] services = config.split(",");
		for (String s : services) {
			if (StringUtils.isNotBlank(s)) {
				set.add(s);
			}
		}
		ignoredLogServices = set;
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

	public RpcException logRemoteCallException(String remoteAddress, String serviceName, String method, String msg,
			InvocationRequest request, InvocationResponse response, MonitorTransaction transaction) {
		if (response.getMessageType() == Constants.MESSAGE_TYPE_EXCEPTION) {
			RpcException ex = InvokerUtils.toRpcException(response);
			logRpcException(remoteAddress, serviceName, method, msg, ex, request, response, transaction);
			return ex;
		}
		return null;
	}

	public Exception logRemoteServiceException(String msg, InvocationRequest request, InvocationResponse response) {
		if (response.getMessageType() == Constants.MESSAGE_TYPE_SERVICE_EXCEPTION) {
			Exception cause = InvokerUtils.toApplicationException(response);
			if (ConfigManagerLoader.getConfigManager().getBooleanValue(KEY_LOG_SERVICE_EXCEPTION, false)) {
				msg = String.format("%s# request:\r\n%s,\r\n response:\r\n%s\r\n", msg, request, response);
				logger.error(msg, cause);
				if (monitor != null) {
					monitor.logError(msg, cause);
				}
			}
			return cause;
		}
		return null;
	}

	public void logRpcException(String remoteAddress, String serviceName, String method, String msg, Throwable e,
			InvocationRequest request, InvocationResponse response, MonitorTransaction transaction) {
		boolean isLog = true;
		if (!InvokerHelper.getLogCallException()) {
			isLog = false;
		} else if (ignoredLogExceptions.contains(e.getClass().getName())) {
			isLog = false;
		} else if (!(e instanceof RpcException)
				&& !ConfigManagerLoader.getConfigManager().getBooleanValue(KEY_LOG_SERVICE_EXCEPTION, false)) {
			isLog = false;
		} else {
			if (e instanceof RequestTimeoutException) {
				isLog = false;
				int logTimeoutPeriod = 0;
				String targetApp = null;
				if (remoteAddress != null) {
					targetApp = RegistryManager.getInstance().getReferencedAppFromCache(remoteAddress);
				}
				if (targetApp != null && appLogTimeoutPeriodMap.containsKey(targetApp)) {
					logTimeoutPeriod = appLogTimeoutPeriodMap.get(targetApp);
				}
				if (targetApp != null && logTimeoutPeriod > 0) {
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
			}
			if (ignoredLogServices.contains(serviceName + "#" + method)) {
				isLog = false;
			}
		}
		if (isLog) {
			StringBuilder error = new StringBuilder();
			if (StringUtils.isNotBlank(msg)) {
				error.append(msg).append(", ");
			}
			if (serviceName != null && request == null) {
				error.append("call:").append(serviceName).append("#").append(method).append(", ");
			}
			if (remoteAddress != null) {
				error.append("address:").append(remoteAddress).append(", ");
			}
			if (request != null) {
				error.append("\r\nrequest:\r\n").append(request);
			}
			if (response != null) {
				error.append("\r\nresponse:\r\n").append(response);
			}
			String s = error.toString();
			logger.error(s, e);
			if (monitor != null) {
				monitor.logError(s, e);
			}
			if (transaction != null) {
				transaction.setStatusError(e);
			}
		}
	}

}
