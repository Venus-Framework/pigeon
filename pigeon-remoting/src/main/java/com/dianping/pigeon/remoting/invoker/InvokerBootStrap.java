/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.invoker;

import org.apache.log4j.Logger;

import com.dianping.pigeon.monitor.LoggerLoader;
import com.dianping.pigeon.registry.config.RegistryConfigLoader;
import com.dianping.pigeon.remoting.invoker.process.InvocationHandlerFactory;
import com.dianping.pigeon.remoting.invoker.service.ServiceInvocationRepository;

public final class InvokerBootStrap {

	private static final Logger logger = LoggerLoader.getLogger(InvokerBootStrap.class);

	private static volatile boolean isStartup = false;

	/**
	 * 初始化客户端组件，为了防止并发多次初始化，这里使用了synchronized的方式。
	 */
	public static void startup() {
		if (!isStartup) {
			synchronized (InvokerBootStrap.class) {
				if (!isStartup) {
					RegistryConfigLoader.init();
					ServiceInvocationRepository.getInstance().init();
					InvocationHandlerFactory.init();
					isStartup = true;
				}
			}
		}
	}

	public static void shutdown() {
		if (isStartup) {
			synchronized (InvokerBootStrap.class) {
				if (isStartup) {
					InvocationHandlerFactory.clearClientInternalFilters();
					ClientManager.getInstance().clear();
					isStartup = false;
				}
			}
		}
	}

}
