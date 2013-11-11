/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.demo.provider.simple;

import com.dianping.pigeon.demo.EchoService;
import com.dianping.pigeon.demo.loader.ConfigLoader;
import com.dianping.pigeon.demo.provider.EchoServiceImpl;
import com.dianping.pigeon.remoting.common.service.ServiceFactory;

public class Server {

	public static void main(String[] args) throws Exception {
		ConfigLoader.init();

		ServiceFactory.publishService(EchoService.class, new EchoServiceImpl());

		Thread.currentThread().join();
	}

}
