/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.test.server.loader;

import java.util.HashMap;
import java.util.Map;

import com.dianping.pigeon.container.SpringContainer;

public final class SpringLoader {

	private static String PROVIDER_SPRING_CONFIG = "classpath*:META-INF/spring/app-provider.xml";
	private static String INVOKER_SPRING_CONFIG = "classpath*:META-INF/spring/app-invoker.xml";

	private static Map<String, SpringContainer> serverContainers = new HashMap<String, SpringContainer>();
	
	private static SpringContainer invokerSpringContainer = new SpringContainer(INVOKER_SPRING_CONFIG);

	//private static SpringContainer providerSpringContainer = new SpringContainer(PROVIDER_SPRING_CONFIG);

	public static void startupInvoker() {
		ConfigLoader.initClient();
		invokerSpringContainer.start();
		// InvokerBootStrapLoader.startup();
	}

	public static void stopInvoker() {
		invokerSpringContainer.stop();
		// InvokerBootStrapLoader.shutdown();
	}

	public static void startupProvider(int port) {
		ConfigLoader.initServer(port);
		String key = port + "";
		if(serverContainers.containsKey(key)) {
			throw new RuntimeException("existed server container:" + key);
		}
		SpringContainer providerSpringContainer = new SpringContainer(PROVIDER_SPRING_CONFIG);
		providerSpringContainer.start();
		serverContainers.put(key, providerSpringContainer);
	}

	public static void stopProvider(int port) {
		String key = port + "";
		SpringContainer providerSpringContainer = serverContainers.get(key);
		if(providerSpringContainer != null) {
			providerSpringContainer.stop();
			serverContainers.remove(key);
		}
	}

}
