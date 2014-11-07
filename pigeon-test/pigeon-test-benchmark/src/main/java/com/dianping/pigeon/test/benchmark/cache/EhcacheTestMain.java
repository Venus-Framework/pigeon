/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.test.benchmark.cache;

import com.dianping.pigeon.container.SpringContainer;

public class EhcacheTestMain {

	private static SpringContainer SERVER_CONTAINER = new SpringContainer("classpath*:spring/*.xml");

	static EhcacheTestService testService;

	static {
		SERVER_CONTAINER.start();
		testService = ((EhcacheTestService) SERVER_CONTAINER.getBean("EhcacheTestService"));
	}

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		System.setProperty("os.name", "linux");
		testService.randomGet();
	}

}
