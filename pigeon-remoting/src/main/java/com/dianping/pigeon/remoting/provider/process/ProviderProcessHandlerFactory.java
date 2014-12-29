/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.provider.process;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.domain.phase.Disposable;
import com.dianping.pigeon.extension.ExtensionLoader;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.remoting.common.domain.InvocationContext;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.common.process.ServiceInvocationFilter;
import com.dianping.pigeon.remoting.common.process.ServiceInvocationHandler;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.provider.domain.ProviderContext;
import com.dianping.pigeon.remoting.provider.process.filter.BusinessProcessFilter;
import com.dianping.pigeon.remoting.provider.process.filter.ContextTransferProcessFilter;
import com.dianping.pigeon.remoting.provider.process.filter.ExceptionProcessFilter;
import com.dianping.pigeon.remoting.provider.process.filter.GatewayProcessFilter;
import com.dianping.pigeon.remoting.provider.process.filter.HealthCheckProcessFilter;
import com.dianping.pigeon.remoting.provider.process.filter.HeartbeatProcessFilter;
import com.dianping.pigeon.remoting.provider.process.filter.MonitorProcessFilter;
import com.dianping.pigeon.remoting.provider.process.filter.WriteResponseProcessFilter;

public final class ProviderProcessHandlerFactory {

	private static final Logger logger = LoggerLoader.getLogger(ProviderProcessHandlerFactory.class);

	private static List<ServiceInvocationFilter<ProviderContext>> bizProcessFilters = new LinkedList<ServiceInvocationFilter<ProviderContext>>();

	private static List<ServiceInvocationFilter<ProviderContext>> heartBeatProcessFilters = new LinkedList<ServiceInvocationFilter<ProviderContext>>();

	private static List<ServiceInvocationFilter<ProviderContext>> healthCheckProcessFilters = new LinkedList<ServiceInvocationFilter<ProviderContext>>();

	private static ServiceInvocationHandler bizInvocationHandler = null;

	private static ServiceInvocationHandler heartBeatInvocationHandler = null;

	private static ServiceInvocationHandler healthCheckInvocationHandler = null;

	private static ConfigManager configManager = ExtensionLoader.getExtension(ConfigManager.class);

	private static boolean isMonitorEnabled = configManager.getBooleanValue(Constants.KEY_MONITOR_ENABLED, true);

	public static ServiceInvocationHandler selectInvocationHandler(int messageType) {
		if (Constants.MESSAGE_TYPE_HEART == messageType) {
			return heartBeatInvocationHandler;
		} else if (Constants.MESSAGE_TYPE_HEALTHCHECK == messageType) {
			return healthCheckInvocationHandler;
		} else {
			return bizInvocationHandler;
		}
	}

	public static void init() {
		if (isMonitorEnabled) {
			registerBizProcessFilter(new MonitorProcessFilter());
		}
		registerBizProcessFilter(new WriteResponseProcessFilter());
		registerBizProcessFilter(new ContextTransferProcessFilter());
		registerBizProcessFilter(new ExceptionProcessFilter());
		registerBizProcessFilter(new GatewayProcessFilter());
		registerBizProcessFilter(new BusinessProcessFilter());
		bizInvocationHandler = createInvocationHandler(bizProcessFilters);

		registerHeartBeatProcessFilter(new WriteResponseProcessFilter());
		registerHeartBeatProcessFilter(new HeartbeatProcessFilter());
		heartBeatInvocationHandler = createInvocationHandler(heartBeatProcessFilters);

		registerHealthCheckProcessFilter(new WriteResponseProcessFilter());
		registerHealthCheckProcessFilter(new HealthCheckProcessFilter());
		healthCheckInvocationHandler = createInvocationHandler(healthCheckProcessFilters);
	}

	@SuppressWarnings({ "rawtypes" })
	private static <K, V extends ServiceInvocationFilter> ServiceInvocationHandler createInvocationHandler(
			List<V> internalFilters) {
		ServiceInvocationHandler last = null;
		List<V> filterList = new ArrayList<V>();
		filterList.addAll(internalFilters);
		for (int i = filterList.size() - 1; i >= 0; i--) {
			final V filter = filterList.get(i);
			final ServiceInvocationHandler next = last;
			last = new ServiceInvocationHandler() {
				@SuppressWarnings("unchecked")
				@Override
				public InvocationResponse handle(InvocationContext invocationContext) throws Throwable {
					return filter.invoke(next, invocationContext);
				}
			};
		}
		return last;
	}

	private static void registerBizProcessFilter(ServiceInvocationFilter<ProviderContext> filter) {
		bizProcessFilters.add(filter);
	}

	private static void registerHeartBeatProcessFilter(ServiceInvocationFilter<ProviderContext> filter) {
		heartBeatProcessFilters.add(filter);
	}

	private static void registerHealthCheckProcessFilter(ServiceInvocationFilter<ProviderContext> filter) {
		healthCheckProcessFilters.add(filter);
	}

	public static void destroy() {
		for (ServiceInvocationFilter<ProviderContext> filter : bizProcessFilters) {
			if (filter instanceof Disposable) {
				try {
					((Disposable) filter).destroy();
				} catch (Exception e) {
				}
			}
		}
		for (ServiceInvocationFilter<ProviderContext> filter : heartBeatProcessFilters) {
			if (filter instanceof Disposable) {
				try {
					((Disposable) filter).destroy();
				} catch (Exception e) {
				}
			}
		}
		bizProcessFilters.clear();
		heartBeatProcessFilters.clear();
	}
}
