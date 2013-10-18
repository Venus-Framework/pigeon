/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.provider.loader;

import com.dianping.pigeon.extension.ExtensionLoader;
import com.dianping.pigeon.remoting.provider.Server;
import com.dianping.pigeon.remoting.provider.ServerFactory;

public final class ProviderBootStrapLoader {

	private static volatile boolean isStartup = false;

	private static Server server = null;
	
	public static void startup(int port) {
		if (!isStartup) {
			synchronized (ProviderBootStrapLoader.class) {
				if (!isStartup) {
					RequestProcessHandlerLoader.init();
					server = ExtensionLoader.getExtension(ServerFactory.class).createServer(port);
					if (server != null) {
						server.start();
					}
					isStartup = true;
				}
			}
		}
	}

	public static void shutdown() {
		RequestProcessHandlerLoader.clearServerInternalFilters();
		if (server != null) {
			server.stop();
		}
		isStartup = false;
	}

}
