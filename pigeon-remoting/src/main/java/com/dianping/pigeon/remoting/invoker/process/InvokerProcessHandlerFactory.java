/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.invoker.process;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.dianping.pigeon.remoting.common.domain.InvocationContext;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.common.process.ServiceInvocationFilter;
import com.dianping.pigeon.remoting.common.process.ServiceInvocationHandler;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.invoker.config.InvokerConfig;
import com.dianping.pigeon.remoting.invoker.process.filter.ClusterInvokeFilter;
import com.dianping.pigeon.remoting.invoker.process.filter.ContextPrepareInvokeFilter;
import com.dianping.pigeon.remoting.invoker.process.filter.DegradationFilter;
import com.dianping.pigeon.remoting.invoker.process.filter.GatewayInvokeFilter;
import com.dianping.pigeon.remoting.invoker.process.filter.InvocationInvokeFilter;
import com.dianping.pigeon.remoting.invoker.process.filter.RemoteCallInvokeFilter;
import com.dianping.pigeon.remoting.invoker.process.filter.RemoteCallMonitorInvokeFilter;
import com.dianping.pigeon.remoting.invoker.process.filter.SecurityFilter;

public final class InvokerProcessHandlerFactory {

	private static List<InvocationInvokeFilter> bizProcessFilters = new LinkedList<InvocationInvokeFilter>();

	private static ServiceInvocationHandler bizInvocationHandler = null;

	private static volatile boolean isInitialized = false;

	public static void init() {
		if (!isInitialized) {
			registerBizProcessFilter(new ClusterInvokeFilter());
			registerBizProcessFilter(new GatewayInvokeFilter());
			if (Constants.MONITOR_ENABLE) {
				registerBizProcessFilter(new RemoteCallMonitorInvokeFilter());
			}
			registerBizProcessFilter(new DegradationFilter());
			registerBizProcessFilter(new ContextPrepareInvokeFilter());
			registerBizProcessFilter(new SecurityFilter());
			registerBizProcessFilter(new RemoteCallInvokeFilter());
			bizInvocationHandler = createInvocationHandler(bizProcessFilters);
			isInitialized = true;
		}
	}

	public static ServiceInvocationHandler selectInvocationHandler(InvokerConfig<?> invokerConfig) {
		return bizInvocationHandler;
	}

	@SuppressWarnings({ "rawtypes" })
	private static <V extends ServiceInvocationFilter> ServiceInvocationHandler createInvocationHandler(
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
					InvocationResponse resp = filter.invoke(next, invocationContext);
					return resp;
				}
			};
		}
		return last;
	}

	public static void registerBizProcessFilter(InvocationInvokeFilter filter) {
		bizProcessFilters.add(filter);
	}

	public static void clearClientFilters() {
		bizProcessFilters.clear();
	}

}
