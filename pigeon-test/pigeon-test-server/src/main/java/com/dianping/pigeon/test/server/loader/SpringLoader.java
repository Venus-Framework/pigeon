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

	private static SpringContainer invokerSpringContainer = new SpringContainer(INVOKER_SPRING_CONFIG);

	// private static SpringContainer providerSpringContainer = new
	// SpringContainer(PROVIDER_SPRING_CONFIG);

	public static void startupInvoker() {
		ConfigLoader.initClient();
		invokerSpringContainer.start();
		// InvokerBootStrapLoader.startup();
	}

	public static void stopInvoker() {
		invokerSpringContainer.stop();
		// InvokerBootStrapLoader.shutdown();
	}

	public static void startupProvider() {
		ConfigLoader.initServer();
		SpringContainer providerSpringContainer = new SpringContainer(PROVIDER_SPRING_CONFIG);
		providerSpringContainer.start();
	}

	public static void stopProvider() {

	}

}
