/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.provider;

import com.dianping.pigeon.extension.ExtensionLoader;
import com.dianping.pigeon.registry.config.RegistryConfigLoader;
import com.dianping.pigeon.remoting.provider.listener.ShutdownHookListener;
import com.dianping.pigeon.remoting.provider.process.RequestProcessHandlerFactory;
import com.dianping.pigeon.util.NetUtils;

public final class ProviderBootStrap {

	static volatile Server server = null;

	public static Server startup(int port) {
		if (server == null) {
			synchronized (ProviderBootStrap.class) {
				if (server == null) {
					int availablePort = NetUtils.getAvailablePort(port);
					RegistryConfigLoader.init();
					RequestProcessHandlerFactory.init();
					server = ExtensionLoader.getExtension(ServerFactory.class).createServer(availablePort);
					if (server != null) {
						server.start();
						Runtime.getRuntime().addShutdownHook(new Thread(new ShutdownHookListener(server)));
					}
				}
			}
		}
		return server;
	}

	public static void shutdown() {
		RequestProcessHandlerFactory.clearServerInternalFilters();
		synchronized (ProviderBootStrap.class) {
			if (server != null) {
				server.stop();
			}
		}
	}

}
