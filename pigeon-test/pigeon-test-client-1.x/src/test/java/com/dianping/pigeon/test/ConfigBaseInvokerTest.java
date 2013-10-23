/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.test;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

import com.dianping.dpsf.async.ServiceCallback;
import com.dianping.lion.client.ConfigCache;
import com.dianping.lion.client.LionException;
import com.dianping.pigeon.test.loader.SpringLoader;

public class ConfigBaseInvokerTest extends
		AbstractDependencyInjectionSpringContextTests {

	private static final Logger logger = Logger
			.getLogger(ConfigBaseInvokerTest.class);

	protected ServiceCallback callback = null;

	public ConfigBaseInvokerTest() {
		try {
			ConfigCache.getInstance("127.0.0.1:2181");
		} catch (LionException e) {
			e.printStackTrace();
		}
	}

	protected String[] getConfigLocations() {
		return new String[] { "classpath*:META-INF/spring/app-invoker.xml" };
	}

	public String getSpringPath() {
		return null;
	}

	@Before
	public void start() throws Exception {
		SpringLoader.startupProvider(4625);
	}

	@After
	public void stop() throws Exception {
		SpringLoader.stopProvider(4625);
	}

}
