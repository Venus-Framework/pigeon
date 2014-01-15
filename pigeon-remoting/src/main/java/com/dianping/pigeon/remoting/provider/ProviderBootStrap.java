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
import com.dianping.pigeon.remoting.common.codec.SerializerFactory;
import com.dianping.pigeon.remoting.provider.config.ServerConfig;
import com.dianping.pigeon.remoting.provider.listener.ShutdownHookListener;
import com.dianping.pigeon.remoting.provider.process.RequestProcessHandlerFactory;
import com.dianping.pigeon.remoting.provider.service.ServiceProviderFactory;
import com.dianping.pigeon.util.VersionUtils;

public final class ProviderBootStrap {

	private static Logger logger = LoggerLoader.getLogger(ServiceProviderFactory.class);
	static volatile Map<String, Server> serversMap = null;
	static volatile ServerConfig serverConfig = null;
	static volatile boolean isStarted = false;

	public static void setServerConfig(ServerConfig serverConfig) {
		ProviderBootStrap.serverConfig = serverConfig;
	}

	public static ServerConfig getServerConfig() {
		return ProviderBootStrap.serverConfig;
	}

	public static ServerConfig startup(ServerConfig serverConfig) {
		if (ProviderBootStrap.serverConfig != null) {
			serverConfig = ProviderBootStrap.serverConfig;
		}
		if (serverConfig == null) {
			throw new IllegalArgumentException("server config is required");
		}
		if (!isStarted) {
			synchronized (ProviderBootStrap.class) {
				if (!isStarted) {
					serversMap = new HashMap<String, Server>();
					RegistryConfigLoader.init();
					RequestProcessHandlerFactory.init();
					SerializerFactory.init();
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
					// ProviderBootStrap.serverConfig = serverConfig;
					isStarted = true;
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
		for (Server server : serversMap.values()) {
			if (server != null) {
				logger.info("start to stop " + server);
				server.stop();
				if (logger.isInfoEnabled()) {
					logger.info(server + " has been shutdown");
				}
			}
		}
	}

}
