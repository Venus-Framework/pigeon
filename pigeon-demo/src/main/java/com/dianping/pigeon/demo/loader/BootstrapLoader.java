/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.demo.loader;

import com.dianping.pigeon.container.SpringContainer;

public final class BootstrapLoader {

	private static String PROVIDER_SPRING_CONFIG = "classpath*:META-INF/spring/app-provider.xml";
	private static String INVOKER_SPRING_CONFIG = "classpath*:META-INF/spring/app-invoker.xml";

	private static SpringContainer invokerSpringContainer = new SpringContainer(INVOKER_SPRING_CONFIG);

	private static SpringContainer providerSpringContainer = new SpringContainer(PROVIDER_SPRING_CONFIG);

	public static void startupInvoker() {
		ConfigLoader.init();
		invokerSpringContainer.start();
		// InvokerBootStrapLoader.startup();
	}

	public static void stopInvoker() {
		invokerSpringContainer.stop();
		// InvokerBootStrapLoader.shutdown();
	}

	public static void startupProvider() {
		ConfigLoader.init();
		providerSpringContainer.start();
		// ProviderBootStrapLoader.startup();
	}

	public static void stopProvider() {
		providerSpringContainer.stop();
	}

}
