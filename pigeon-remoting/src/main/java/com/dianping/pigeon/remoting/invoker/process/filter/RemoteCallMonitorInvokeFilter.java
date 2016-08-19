/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.invoker.process.filter;

import java.util.Calendar;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang.StringUtils;
import com.dianping.pigeon.log.Logger;
import org.springframework.util.CollectionUtils;

import com.dianping.dpsf.exception.NetTimeoutException;
import com.dianping.pigeon.config.ConfigChangeListener;
import com.dianping.pigeon.config.ConfigManagerLoader;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.monitor.Monitor;
import com.dianping.pigeon.monitor.MonitorLoader;
import com.dianping.pigeon.monitor.MonitorTransaction;
import com.dianping.pigeon.registry.RegistryManager;
import com.dianping.pigeon.remoting.common.domain.InvocationContext.TimePhase;
import com.dianping.pigeon.remoting.common.domain.InvocationContext.TimePoint;
import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.common.monitor.SizeMonitor;
import com.dianping.pigeon.remoting.common.process.ServiceInvocationHandler;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.common.util.InvocationUtils;
import com.dianping.pigeon.remoting.invoker.Client;
import com.dianping.pigeon.remoting.invoker.config.InvokerConfig;
import com.dianping.pigeon.remoting.invoker.domain.InvokerContext;

public class RemoteCallMonitorInvokeFilter extends InvocationInvokeFilter {

	private static final Logger logger = LoggerLoader.getLogger(RemoteCallMonitorInvokeFilter.class);

	private final Monitor monitor = MonitorLoader.getMonitor();

	private static volatile Map<String, Integer> appLogTimeoutPeriodMap = new ConcurrentHashMap<String, Integer>();

	private static final int logTimeoutPeriodLimit = ConfigManagerLoader.getConfigManager().getIntValue(
			"pigeon.invoker.log.timeout.period.limit", 9999);

	private static volatile Map<String, AtomicInteger> appTimeouts = new ConcurrentHashMap<String, AtomicInteger>();

	private static final String KEY_LOG_TIMEOUT_PERIOD = "pigeon.invoker.log.timeout.period.apps";

	private static final Random random = new Random();

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
		invocationContext.getTimeline().add(new TimePoint(TimePhase.O));
		MonitorTransaction transaction = null;
		InvocationRequest request = invocationContext.getRequest();
		String targetApp = null;
		String callInterface = null;
		InvokerConfig<?> invokerConfig = invocationContext.getInvokerConfig();
		if (monitor != null) {
			try {
				callInterface = InvocationUtils.getRemoteCallFullName(invokerConfig.getUrl(),
						invocationContext.getMethodName(), invocationContext.getParameterTypes());
				transaction = monitor.createTransaction("PigeonCall", callInterface, invocationContext);
				if (transaction != null) {
					monitor.setCurrentCallTransaction(transaction);
					transaction.setStatusOk();
					transaction.addData("CallType", invokerConfig.getCallType());
					transaction.addData("Timeout", invokerConfig.getTimeout());
					transaction.addData("Serialize", invokerConfig.getSerialize());

					transaction.logEvent("PigeonCall.QPS", "S" + Calendar.getInstance().get(Calendar.SECOND), "");
					/*boolean logTimeout = random.nextInt(Constants.INVOKER_LOG_TIMEOUT_PERCENT) < 1;
					if (logTimeout) {
						transaction
								.logEvent("PigeonCall.timeout." + callInterface, invokerConfig.getTimeout() + "", "");
					}*/
					transaction.readMonitorContext();
				}
			} catch (Throwable e) {
				monitor.logMonitorError(e);
			}
		}
		boolean error = false;
		try {
			InvocationResponse response = handler.handle(invocationContext);
			if (transaction != null) {
				if (invocationContext.isDegraded()) {
					transaction.logEvent("PigeonCall.degrade", callInterface, "");
				}
				Client client = invocationContext.getClient();
				if (client != null) {
					targetApp = RegistryManager.getInstance().getReferencedAppFromCache(client.getAddress());
					transaction.logEvent("PigeonCall.app", targetApp, "");
					String parameters = "";
					if (Constants.LOG_PARAMETERS) {
						parameters = InvocationUtils.toJsonString(request.getParameters(), 1000, 50);
					}
					transaction.logEvent("PigeonCall.server", client.getAddress(), parameters);
				}
				if (request != null) {
					String reqSize = SizeMonitor.getInstance().getLogSize(request.getSize());
					if (reqSize != null) {
						monitor.logEvent("PigeonCall.requestSize", reqSize, "" + request.getSize());
					}
				}
				if (response != null && response.getSize() > 0) {
					String respSize = SizeMonitor.getInstance().getLogSize(response.getSize());
					if (respSize != null) {
						monitor.logEvent("PigeonCall.responseSize", respSize, "" + response.getSize());
					}
					invocationContext.getTimeline().add(new TimePoint(TimePhase.R, response.getCreateMillisTime()));
					invocationContext.getTimeline().add(new TimePoint(TimePhase.R));
				}
			}
			return response;
		} catch (NetTimeoutException e) {
			Client client = invocationContext.getClient();
			if (client != null) {
				targetApp = RegistryManager.getInstance().getReferencedAppFromCache(client.getAddress());
				transaction.logEvent("PigeonCall.app", targetApp, "");
				String parameters = "";
				if (Constants.LOG_PARAMETERS) {
					parameters = InvocationUtils.toJsonString(request.getParameters(), 1000, 50);
				}
				transaction.logEvent("PigeonCall.server", client.getAddress(), parameters);
			}
			if (request != null) {
				String reqSize = SizeMonitor.getInstance().getLogSize(request.getSize());
				if (reqSize != null) {
					monitor.logEvent("PigeonCall.requestSize", reqSize, "" + request.getSize());
				}
			}

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
			Client client = invocationContext.getClient();
			if (client != null) {
				targetApp = RegistryManager.getInstance().getReferencedAppFromCache(client.getAddress());
				transaction.logEvent("PigeonCall.app", targetApp, "");
				String parameters = "";
				if (Constants.LOG_PARAMETERS) {
					parameters = InvocationUtils.toJsonString(request.getParameters(), 1000, 50);
				}
				transaction.logEvent("PigeonCall.server", client.getAddress(), parameters);
			}
			if (request != null) {
				String reqSize = SizeMonitor.getInstance().getLogSize(request.getSize());
				if (reqSize != null) {
					monitor.logEvent("PigeonCall.requestSize", reqSize, "" + request.getSize());
				}
			}

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
					if (!Constants.CALL_FUTURE.equals(invokerConfig.getCallType()) || error) {
						invocationContext.getTimeline().add(new TimePoint(TimePhase.E, System.currentTimeMillis()));
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
