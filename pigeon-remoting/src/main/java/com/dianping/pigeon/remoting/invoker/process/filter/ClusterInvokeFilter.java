/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.invoker.process.filter;

import com.dianping.pigeon.log.Logger;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.common.process.ServiceInvocationHandler;
import com.dianping.pigeon.remoting.invoker.cluster.Cluster;
import com.dianping.pigeon.remoting.invoker.cluster.ClusterFactory;
import com.dianping.pigeon.remoting.invoker.config.InvokerConfig;
import com.dianping.pigeon.remoting.invoker.domain.InvokerContext;

public class ClusterInvokeFilter extends InvocationInvokeFilter {

	private static final Logger logger = LoggerLoader.getLogger(ClusterInvokeFilter.class);

	public InvocationResponse invoke(ServiceInvocationHandler handler, InvokerContext invocationContext)
			throws Throwable {
		InvokerConfig<?> invokerConfig = invocationContext.getInvokerConfig();
		Cluster cluster = ClusterFactory.selectCluster(invokerConfig.getCluster());
		if (cluster == null) {
			throw new IllegalArgumentException("Unsupported cluster type:" + cluster);
		}
		return cluster.invoke(handler, invocationContext);
	}

}
