/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.invoker.loader;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.dianping.pigeon.component.invocation.InvocationContext;
import com.dianping.pigeon.component.invocation.InvocationResponse;
import com.dianping.pigeon.extension.DefaultExtension;
import com.dianping.pigeon.remoting.common.filter.ServiceInvocationFilter;
import com.dianping.pigeon.remoting.common.filter.ServiceInvocationHandler;
import com.dianping.pigeon.remoting.invoker.component.InvokerMetaData;
import com.dianping.pigeon.remoting.invoker.filter.ContextPrepareInvokeFilter;
import com.dianping.pigeon.remoting.invoker.filter.FailoverClusterInvokeFilter;
import com.dianping.pigeon.remoting.invoker.filter.GatewayInvokeFilter;
import com.dianping.pigeon.remoting.invoker.filter.InvocationInvokeFilter;
import com.dianping.pigeon.remoting.invoker.filter.InvocationInvokeFilter.InvokePhase;
import com.dianping.pigeon.remoting.invoker.filter.RemoteCallInvokeFilter;
import com.dianping.pigeon.remoting.invoker.filter.RemoteCallMonitorInvokeFilter;
import com.dianping.pigeon.remoting.invoker.filter.ServiceRouteInvokeFilter;

public final class InvocationHandlerLoader extends DefaultExtension {

	{
		//descriptorClass = SubscriberPluginDescriptor.class;
	}

	private static Map<InvokePhase, List<InvocationInvokeFilter>> internalInvokeFilters = new LinkedHashMap<InvokePhase, List<InvocationInvokeFilter>>();

	private static Map<InvokePhase, List<InvocationInvokeFilter>> failoverInvokeFilters = new LinkedHashMap<InvokePhase, List<InvocationInvokeFilter>>();

	private static ServiceInvocationHandler bizInvocationHandler = null;

	private static ServiceInvocationHandler failOverInvocationHandler = null;

	public static void init() {
		// 怎么确保顺序?
		registerInternalInvokeFilter(InvokePhase.Finalize, new GatewayInvokeFilter());
		registerInternalInvokeFilter(InvokePhase.Cluster, new ServiceRouteInvokeFilter());
		registerInternalInvokeFilter(InvokePhase.Before_Call, new RemoteCallMonitorInvokeFilter());
		registerInternalInvokeFilter(InvokePhase.Call, new ContextPrepareInvokeFilter());
		registerInternalInvokeFilter(InvokePhase.Call, new RemoteCallInvokeFilter());
		bizInvocationHandler = createInvocationHandler(internalInvokeFilters);

		registerFailoverInvokeFilter(InvokePhase.Finalize, new GatewayInvokeFilter());
		registerFailoverInvokeFilter(InvokePhase.Cluster, new ServiceRouteInvokeFilter());
		// TODO FIXME! 是否需要fail over可以让service invoke方配置决定
		registerFailoverInvokeFilter(InvokePhase.Cluster, new FailoverClusterInvokeFilter());
		/** TODO 重构：监控组件可配置 **/
		registerFailoverInvokeFilter(InvokePhase.Before_Call, new RemoteCallMonitorInvokeFilter());
		registerFailoverInvokeFilter(InvokePhase.Call, new ContextPrepareInvokeFilter());
		registerFailoverInvokeFilter(InvokePhase.Call, new RemoteCallInvokeFilter());
		failOverInvocationHandler = createInvocationHandler(failoverInvokeFilters);
	}

	public static ServiceInvocationHandler createInvokeHandler(InvokerMetaData metadata) {
		if ("failover".equalsIgnoreCase(metadata.getCluster())) {
			return failOverInvocationHandler;
		}
		return bizInvocationHandler;
	}

	@SuppressWarnings({ "rawtypes" })
	private static <K, V extends ServiceInvocationFilter> ServiceInvocationHandler createInvocationHandler(
			Map<K, List<V>> internalFilters) {
		Map<K, List<V>> mergedFilters = new LinkedHashMap<K, List<V>>(internalFilters);
		ServiceInvocationHandler last = null;
		List<V> filterList = new ArrayList<V>();
		for (Map.Entry<K, List<V>> entry : mergedFilters.entrySet()) {
			filterList.addAll(entry.getValue());
		}
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

	public static void registerInternalInvokeFilter(InvocationInvokeFilter.InvokePhase phase,
			InvocationInvokeFilter filter) {
		List<InvocationInvokeFilter> filters = internalInvokeFilters.get(phase);
		if (filters == null) {
			filters = new ArrayList<InvocationInvokeFilter>();
			internalInvokeFilters.put(phase, filters);
		}
		filters.add(filter);
	}

	public static void registerFailoverInvokeFilter(InvocationInvokeFilter.InvokePhase phase,
			InvocationInvokeFilter filter) {
		List<InvocationInvokeFilter> filters = failoverInvokeFilters.get(phase);
		if (filters == null) {
			filters = new ArrayList<InvocationInvokeFilter>();
			failoverInvokeFilters.put(phase, filters);
		}
		filters.add(filter);
	}

	public static void clearClientInternalFilters() {
		internalInvokeFilters.clear();
	}

	@Override
	public void registerContribution(Object contribution, String extensionPoint) {
		// TODO 注册贡献者
		System.out.println("register contribution: " + contribution + "@" + extensionPoint);
	}
}
