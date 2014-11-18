/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.test.benchmark.cache;

import com.dianping.avatar.cache.CacheService;
import com.dianping.pigeon.container.SpringContainer;

public class MemcacheTestMain {

	private static SpringContainer SERVER_CONTAINER = new SpringContainer("classpath*:spring/*.xml");

	static MemcacheTestService testService = new MemcacheTestService();

	static {
		SERVER_CONTAINER.start();
		testService.setCacheService((CacheService) SERVER_CONTAINER.getBean("cacheService"));
	}

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		System.setProperty("os.name", "linux");
		int threads = Integer.valueOf(System.getProperty("threads"));
		int rows = Integer.valueOf(System.getProperty("rows"));
		testService.concurrentGet(threads, rows);
	}

}
