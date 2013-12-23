/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.provider;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.dianping.pigeon.extension.ExtensionLoader;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.monitor.Monitor;
import com.dianping.pigeon.registry.config.RegistryConfigLoader;
import com.dianping.pigeon.remoting.provider.config.ServerConfig;
import com.dianping.pigeon.remoting.provider.listener.ShutdownHookListener;
import com.dianping.pigeon.remoting.provider.process.RequestProcessHandlerFactory;
import com.dianping.pigeon.remoting.provider.service.ServiceProviderFactory;
import com.dianping.pigeon.util.VersionUtils;

public final class ProviderBootStrap {

	private static Logger logger = LoggerLoader.getLogger(ServiceProviderFactory.class);
	static volatile Map<String, Server> serversMap = null;
	static volatile ServerConfig serverConfig = null;

	public static ServerConfig startup(ServerConfig serverConfig) {
		if (ProviderBootStrap.serverConfig == null) {
			synchronized (ProviderBootStrap.class) {
				if (ProviderBootStrap.serverConfig == null) {
					serversMap = new HashMap<String, Server>();
					RegistryConfigLoader.init();
					RequestProcessHandlerFactory.init();
					Monitor monitor = ExtensionLoader.getExtension(Monitor.class);
					if (monitor != null) {
						monitor.init();
					}
					List<Server> servers = ExtensionLoader.getExtensionList(Server.class);
					for (Server server : servers) {
						if (server.support(serverConfig)) {
							server.start(serverConfig);
							Runtime.getRuntime().addShutdownHook(new Thread(new ShutdownHookListener(server)));
							serversMap.put(server.toString(), server);
							if (logger.isInfoEnabled()) {
								logger.info("pigeon server[version:" + VersionUtils.VERSION
										+ "] has been started at port:" + server.getPort());
							}
						}
					}
					ProviderBootStrap.serverConfig = serverConfig;
				} else {
					logger.warn("pigeon server[version:" + VersionUtils.VERSION + "] has already been started:"
							+ serversMap);
				}
			}
		} else {
			logger.warn("pigeon server[version:" + VersionUtils.VERSION + "] has already been started:" + serversMap);
		}
		return ProviderBootStrap.serverConfig;
	}

	public static void shutdown() {
		RequestProcessHandlerFactory.clearServerInternalFilters();
		synchronized (ProviderBootStrap.class) {
			for (Server server : serversMap.values()) {
				if (server != null) {
					server.stop();
					if (logger.isInfoEnabled()) {
						logger.info("pigeon server[" + server + "] has been shutdown");
					}
				}
			}
		}
	}

}
