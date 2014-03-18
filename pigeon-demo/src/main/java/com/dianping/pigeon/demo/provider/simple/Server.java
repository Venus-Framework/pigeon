/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.demo.provider.simple;

import com.dianping.pigeon.demo.EchoService;
import com.dianping.pigeon.demo.UserService;
import com.dianping.pigeon.demo.provider.EchoServiceDefaultImpl;
import com.dianping.pigeon.demo.provider.MyProviderProcessInterceptor;
import com.dianping.pigeon.demo.provider.TestServiceImpl;
import com.dianping.pigeon.demo.provider.UserServiceDefaultImpl;
import com.dianping.pigeon.remoting.ServiceFactory;
import com.dianping.pigeon.remoting.provider.config.ProviderConfig;
import com.dianping.pigeon.remoting.provider.process.ProviderProcessInterceptorFactory;

public class Server {

	public static void main(String[] args) throws Exception {
		ProviderProcessInterceptorFactory.registerInterceptor(new MyProviderProcessInterceptor());

		ProviderConfig<EchoService> providerConfig = new ProviderConfig<EchoService>(EchoService.class,
				new EchoServiceDefaultImpl());
		String url = "http://service.dianping.com/com.dianping.pigeon.demo.EchoService";
		providerConfig.setUrl(url);
		ServiceFactory.addService(providerConfig);
		ServiceFactory.addService(UserService.class, new UserServiceDefaultImpl());

		ServiceFactory.addService("http://service.dianping.com/testservice", TestServiceImpl.class,
				new TestServiceImpl());

		ServiceFactory.unpublishService(url);
		ServiceFactory.publishService(url);

		System.in.read();
	}

}
