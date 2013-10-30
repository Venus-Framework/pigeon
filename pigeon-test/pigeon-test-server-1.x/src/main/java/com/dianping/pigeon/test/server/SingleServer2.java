/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.test.server;

import com.dianping.lion.client.ConfigCache;
import com.dianping.pigeon.test.loader.SpringContainer;

public class SingleServer2 {

	public static void main(String[] args) throws Exception {
		ConfigCache.getInstance("127.0.0.1:2181");
		new SpringContainer("classpath*:META-INF/spring/app-provider2.xml")
				.start();
		Thread.currentThread().join();
	}

}
