/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.provider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.dianping.pigeon.extension.ExtensionLoader;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.monitor.Monitor;
import com.dianping.pigeon.registry.config.RegistryConfigLoader;
import com.dianping.pigeon.remoting.common.codec.SerializerFactory;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.provider.config.ProviderConfig;
import com.dianping.pigeon.remoting.provider.config.ServerConfig;
import com.dianping.pigeon.remoting.provider.listener.ShutdownHookListener;
import com.dianping.pigeon.remoting.provider.process.ProviderProcessHandlerFactory;
import com.dianping.pigeon.remoting.provider.service.ServiceProviderFactory;
import com.dianping.pigeon.util.VersionUtils;

public final class ProviderBootStrap {

	private static Logger logger = LoggerLoader.getLogger(ServiceProviderFactory.class);
	static Server httpServer = null;
	static volatile Map<String, Server> serversMap = new HashMap<String, Server>();
	static volatile boolean isInitialized = false;

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
			config.setProtocol(Constants.PROTOCOL_HTTP);
			List<Server> servers = ExtensionLoader.getExtensionList(Server.class);
			for (Server server : servers) {
				if (!server.isStarted()) {
					if (server.support(config)) {
						server.start(config);
						httpServer = server;
						serversMap.put(server.getProtocol() + server.getPort(), server);
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
		if (serverConfig == null) {
			throw new IllegalArgumentException("server config is required");
		}
		Server server = serversMap.get(serverConfig.getProtocol() + serverConfig.getPort());
		if (server != null) {
			return server.getServerConfig();
		} else {
			synchronized (ProviderBootStrap.class) {
				List<Server> servers = ExtensionLoader.newExtensionList(Server.class);
				for (Server s : servers) {
					if (!s.isStarted()) {
						if (s.support(serverConfig)) {
							s.start(serverConfig);
							serversMap.put(s.getProtocol() + s.getPort(), s);
							if (logger.isInfoEnabled()) {
								logger.info("pigeon server[version:" + VersionUtils.VERSION
										+ "] has been started at port:" + s.getPort());
							}
							break;
						}
					}
				}
				server = serversMap.get(serverConfig.getProtocol() + serverConfig.getPort());
				if (server != null) {
					return server.getServerConfig();
				}
				return null;
			}
		}
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

	public static List<Server> getServers(ProviderConfig<?> providerConfig) {
		List<Server> servers = new ArrayList<Server>();
		servers.add(httpServer);
		String protocol = providerConfig.getServerConfig().getProtocol();
		int port = providerConfig.getServerConfig().getPort();
		servers.add(serversMap.get(protocol + port));

		return servers;
	}

	public static Map<String, Server> getServersMap() {
		return serversMap;
	}
}
