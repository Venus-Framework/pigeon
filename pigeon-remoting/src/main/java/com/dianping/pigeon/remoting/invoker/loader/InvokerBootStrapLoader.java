/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.invoker.loader;

import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.dianping.pigeon.extension.plugin.Component;
import com.dianping.pigeon.extension.plugin.Plugin;
import com.dianping.pigeon.extension.plugin.PluginContainer;
import com.dianping.pigeon.extension.plugin.PluginManager;
import com.dianping.pigeon.extension.plugin.PluginManager.Phase;
import com.dianping.pigeon.remoting.invoker.service.ClientManager;
import com.dianping.pigeon.remoting.invoker.service.ServiceInvocationRepository;

public final class InvokerBootStrapLoader {

	private static final Logger logger = Logger.getLogger(InvokerBootStrapLoader.class);

	private static volatile boolean isStartup = false;

	/**
	 * 初始化客户端组件，为了防止并发多次初始化，这里使用了synchronized的方式。
	 */
	public static void startup() {
		if (!isStartup) {
			synchronized (InvokerBootStrapLoader.class) {
				if (!isStartup) {
					ServiceInvocationRepository.getInstance().init();
					InvocationHandlerLoader.init();
					PluginContainer.start();
					ConcurrentHashMap<String, Plugin> pluginOnes = PluginManager.loadPlugins(Phase.PHASE_ONE);

					for (Plugin plugin : pluginOnes.values()) {
						if ("init".equalsIgnoreCase(plugin.getPoint())) {
							String componentClassName = plugin.getComponent();

							if (logger.isInfoEnabled()) {
								logger.info("componentClassName" + componentClassName);
							}
							try {
								Component component = (Component) Class.forName(componentClassName).newInstance();
								component.init();
							} catch (Exception e) {
								logger.error("", e);
							}
						}
					}

					isStartup = true;
				}
			}
		}
	}

	public static void shutdown() {
		if (isStartup) {
			synchronized (InvokerBootStrapLoader.class) {
				if (isStartup) {
					InvocationHandlerLoader.clearClientInternalFilters();
					ClientManager.getInstance().clear();
					isStartup = false;
				}
			}
		}
	}

}
