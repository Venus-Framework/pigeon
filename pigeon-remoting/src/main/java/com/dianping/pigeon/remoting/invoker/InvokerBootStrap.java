/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.invoker;

import org.apache.log4j.Logger;

import com.dianping.pigeon.extension.ExtensionLoader;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.monitor.Monitor;
import com.dianping.pigeon.registry.config.RegistryConfigLoader;
import com.dianping.pigeon.remoting.common.codec.SerializerFactory;
import com.dianping.pigeon.remoting.invoker.process.InvokerProcessHandlerFactory;
import com.dianping.pigeon.remoting.invoker.service.ServiceInvocationRepository;
import com.dianping.pigeon.util.VersionUtils;

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
					InvokerProcessHandlerFactory.init();
					SerializerFactory.init();
					Monitor monitor = ExtensionLoader.getExtension(Monitor.class);
					if(monitor != null) {
						monitor.init();
					}
					isStartup = true;
					if (logger.isInfoEnabled()) {
						logger.info("pigeon client[version:" + VersionUtils.VERSION + "] has been started");
					}
				}
			}
		}
	}

	public static void shutdown() {
		if (isStartup) {
			synchronized (InvokerBootStrap.class) {
				if (isStartup) {
					InvokerProcessHandlerFactory.clearClientFilters();
					ClientManager.getInstance().clear();
					isStartup = false;
					if (logger.isInfoEnabled()) {
						logger.info("pigeon client[version:" + VersionUtils.VERSION + "] has been shutdown");
					}
				}
			}
		}
	}

}
