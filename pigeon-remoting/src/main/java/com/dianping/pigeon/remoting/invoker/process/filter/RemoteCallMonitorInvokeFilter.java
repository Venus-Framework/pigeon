/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.invoker.process.filter;

import java.util.Calendar;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.Logger;
import org.springframework.util.CollectionUtils;

import com.dianping.dpsf.exception.NetTimeoutException;
import com.dianping.pigeon.config.ConfigChangeListener;
import com.dianping.pigeon.config.ConfigManagerLoader;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.monitor.Monitor;
import com.dianping.pigeon.monitor.MonitorLoader;
import com.dianping.pigeon.monitor.MonitorTransaction;
import com.dianping.pigeon.registry.RegistryManager;
import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.common.monitor.SizeMonitor;
import com.dianping.pigeon.remoting.common.process.ServiceInvocationHandler;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.common.util.InvocationUtils;
import com.dianping.pigeon.remoting.common.util.TimelineManager;
import com.dianping.pigeon.remoting.common.util.TimelineManager.Timeline;
import com.dianping.pigeon.remoting.invoker.Client;
import com.dianping.pigeon.remoting.invoker.config.InvokerConfig;
import com.dianping.pigeon.remoting.invoker.domain.InvokerContext;

public class RemoteCallMonitorInvokeFilter extends InvocationInvokeFilter {

	private static final Logger logger = LoggerLoader.getLogger(RemoteCallMonitorInvokeFilter.class);

	private Monitor monitor = MonitorLoader.getMonitor();

	private static Map<String, Integer> appLogTimeoutPeriodMap = new ConcurrentHashMap<String, Integer>();

	private static final int logTimeoutPeriodLimit = ConfigManagerLoader.getConfigManager().getIntValue(
			"pigeon.invoker.log.timeout.period.limit", 9999);

	private static Map<String, AtomicInteger> appTimeouts = new ConcurrentHashMap<String, AtomicInteger>();

	private static final String KEY_LOG_TIMEOUT_PERIOD = "pigeon.invoker.log.timeout.period.apps";

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
		MonitorTransaction transaction = null;
		InvocationRequest request = invocationContext.getRequest();
		String targetApp = null;
		InvokerConfig<?> invokerConfig = invocationContext.getInvokerConfig();
		if (monitor != null) {
			try {
				String callInterface = InvocationUtils.getRemoteCallFullName(invokerConfig.getUrl(),
						invocationContext.getMethodName(), invocationContext.getParameterTypes());
				boolean isAutoCommit = true;
				String callType = invokerConfig.getCallType();
				if (Constants.CALL_CALLBACK.equalsIgnoreCase(callType)) {
					isAutoCommit = true;
				}
				transaction = monitor.createTransaction("PigeonCall", callInterface, invocationContext, isAutoCommit);
				if (transaction != null) {
					monitor.setCurrentCallTransaction(transaction);
					invocationContext.setMonitorTransaction(transaction);
					// if (!isAutoCommit) {
					// monitor.getCurrentServiceTransaction().addTransaction(transaction);
					// }
					transaction.setStatusOk();
					transaction.addData("CallType", invokerConfig.getCallType());
					transaction.addData("Timeout", invokerConfig.getTimeout());
					transaction.addData("Serialize", invokerConfig.getSerialize());

					Client client = invocationContext.getClient();
					targetApp = RegistryManager.getInstance().getReferencedApp(client.getAddress());
					transaction.logEvent("PigeonCall.app", targetApp, "");
					transaction.logEvent("PigeonCall.QPS", "S" + Calendar.getInstance().get(Calendar.SECOND), "");
					if (Constants.LOG_INVOKER_TIMEOUT) {
						transaction
								.logEvent("PigeonCall.timeout." + callInterface, invokerConfig.getTimeout() + "", "");
					}
					String parameters = "";
					if (Constants.LOG_PARAMETERS) {
						parameters = InvocationUtils.toJsonString(request.getParameters(), 1000, 50);
					}
					transaction.logEvent("PigeonCall.server", client.getAddress(), parameters);

					transaction.readMonitorContext();
				}
			} catch (Throwable e) {
				monitor.logMonitorError(e);
			}
		}
		boolean error = false;
		try {
			InvocationResponse response = handler.handle(invocationContext);
			String reqSize = SizeMonitor.getInstance().getLogSize(request.getSize());
			if (reqSize != null) {
				transaction.logEvent("PigeonCall.requestSize", reqSize, "" + request.getSize());
			}
			if (response != null && response.getSize() > 0) {
				String respSize = SizeMonitor.getInstance().getLogSize(response.getSize());
				if (respSize != null) {
					transaction.logEvent("PigeonCall.responseSize", respSize, "" + response.getSize());
				}
			}
			return response;
		} catch (NetTimeoutException e) {
			error = true;
			boolean isLog = false;
			int logTimeoutPeriod = 0;
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
			if (isLog) {
				if (transaction != null) {
					try {
						transaction.setStatusError(e);
					} catch (Throwable e2) {
						monitor.logMonitorError(e2);
					}
				}
				if (monitor != null) {
					monitor.logError(e);
				}
			}
			throw e;
		} catch (Throwable e) {
			error = true;
			if (transaction != null) {
				try {
					transaction.setStatusError(e);
				} catch (Throwable e2) {
					monitor.logMonitorError(e2);
				}
			}
			if (monitor != null) {
				monitor.logError(e);
			}
			throw e;
		} finally {
			if (transaction != null) {
				try {
					if (TimelineManager.isEnabled()) {
						Timeline timeline = TimelineManager.getTimeline(request, TimelineManager.getLocalIp());
						transaction.addData("Timeline", timeline);
					}
					if (!Constants.CALL_FUTURE.equals(invokerConfig.getCallType()) || error) {
						transaction.complete();
					}
				} catch (Throwable e) {
					monitor.logMonitorError(e);
				}
				if (monitor != null) {
					monitor.clearCallTransaction();
				}
			}
		}
	}
}
