/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.provider;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.dianping.pigeon.extension.ExtensionLoader;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.monitor.Monitor;
import com.dianping.pigeon.registry.config.RegistryConfigLoader;
import com.dianping.pigeon.remoting.common.codec.SerializerFactory;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.provider.config.ServerConfig;
import com.dianping.pigeon.remoting.provider.listener.ShutdownHookListener;
import com.dianping.pigeon.remoting.provider.process.ProviderProcessHandlerFactory;
import com.dianping.pigeon.remoting.provider.service.ServiceProviderFactory;
import com.dianping.pigeon.util.VersionUtils;

public final class ProviderBootStrap {

	private static Logger logger = LoggerLoader.getLogger(ServiceProviderFactory.class);
	static volatile Map<String, Server> serversMap = new HashMap<String, Server>();
	static volatile ServerConfig serverConfig = null;
	static volatile boolean isInitialized = false;

	public static void setServerConfig(ServerConfig serverConfig) {
		ProviderBootStrap.serverConfig = serverConfig;
	}

	public static ServerConfig getServerConfig() {
		return ProviderBootStrap.serverConfig;
	}

	public static void init() {
		if (!isInitialized) {
			RegistryConfigLoader.init();
			ProviderProcessHandlerFactory.init();
			SerializerFactory.init();
			Monitor monitor = ExtensionLoader.getExtension(Monitor.class);
			if (monitor != null) {
				monitor.init();
			}
			Thread shutdownHook = new Thread(new ShutdownHookListener());
			shutdownHook.setPriority(Thread.MAX_PRIORITY);
			Runtime.getRuntime().addShutdownHook(shutdownHook);

			ServerConfig config = new ServerConfig();
			Set<String> protocols = new HashSet<String>();
			protocols.add(Constants.PROTOCOL_HTTP);
			config.setProtocols(protocols);
			List<Server> servers = ExtensionLoader.getExtensionList(Server.class);
			for (Server server : servers) {
				if (!server.isStarted()) {
					if (server.support(config)) {
						server.start(config);
						serversMap.put(server.toString(), server);
						if (logger.isInfoEnabled()) {
							logger.info("pigeon server[version:" + VersionUtils.VERSION + "] has been started at port:"
									+ server.getPort());
						}
					}
				}
			}
			isInitialized = true;
		}
	}

	public static ServerConfig startup(ServerConfig serverConfig) {
		if (ProviderBootStrap.serverConfig != null) {
			serverConfig = ProviderBootStrap.serverConfig;
		}
		if (serverConfig == null) {
			throw new IllegalArgumentException("server config is required");
		}
		synchronized (ProviderBootStrap.class) {
			List<Server> servers = ExtensionLoader.getExtensionList(Server.class);
			if (logger.isInfoEnabled()) {
				logger.info("server list:" + servers);
			}
			for (Server server : servers) {
				if (!server.isStarted()) {
					if (server.support(serverConfig)) {
						server.start(serverConfig);
						serversMap.put(server.toString(), server);
						if (logger.isInfoEnabled()) {
							logger.info("pigeon server[version:" + VersionUtils.VERSION + "] has been started at port:"
									+ server.getPort());
						}
					}
				} else {
					logger.info("pigeon server[version:" + VersionUtils.VERSION + "] has already been started:"
							+ serversMap);
				}
			}
			ProviderBootStrap.serverConfig = serverConfig;
		}
		return ProviderBootStrap.serverConfig;
	}

	public static void shutdown() {
		for (Server server : serversMap.values()) {
			if (server != null) {
				logger.info("start to stop " + server);
				server.stop();
				if (logger.isInfoEnabled()) {
					logger.info(server + " has been shutdown");
				}
			}
		}
		ProviderProcessHandlerFactory.clearServerInternalFilters();
	}

}
