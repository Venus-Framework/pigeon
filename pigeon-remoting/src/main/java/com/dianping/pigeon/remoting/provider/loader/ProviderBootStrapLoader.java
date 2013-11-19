/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.provider.loader;

import com.dianping.pigeon.extension.ExtensionLoader;
import com.dianping.pigeon.registry.config.RegistryConfigLoader;
import com.dianping.pigeon.remoting.provider.Server;
import com.dianping.pigeon.remoting.provider.ServerFactory;
import com.dianping.pigeon.remoting.provider.listener.ShutdownHookListener;
import com.dianping.pigeon.util.NetUtils;

public final class ProviderBootStrapLoader {

	static volatile Server server = null;

	public static Server startup(int port) {
		if (server == null) {
			synchronized (ProviderBootStrapLoader.class) {
				if (server == null) {
					int availablePort = NetUtils.getAvailablePort(port);
					RegistryConfigLoader.init();
					RequestProcessHandlerLoader.init();
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
		RequestProcessHandlerLoader.clearServerInternalFilters();
		synchronized (ProviderBootStrapLoader.class) {
			if (server != null) {
				server.stop();
			}
		}
	}

}
