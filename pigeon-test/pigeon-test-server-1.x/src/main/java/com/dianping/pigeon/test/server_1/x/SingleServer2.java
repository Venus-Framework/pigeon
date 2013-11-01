/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.test.server_1.x;

import com.dianping.pigeon.test.server_1.x.loader.ConfigLoader;
import com.dianping.pigeon.test.server_1.x.loader.SpringContainer;

public class SingleServer2 {

	public static void main(String[] args) throws Exception {
		ConfigLoader.initServer();
		new SpringContainer("classpath*:META-INF/spring/app-provider2.xml")
				.start();
		Thread.currentThread().join();
	}

}
