/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.demo.provider.version;

import com.dianping.pigeon.demo.EchoService;
import com.dianping.pigeon.demo.UserService;
import com.dianping.pigeon.demo.provider.EchoServiceDefaultImpl;
import com.dianping.pigeon.demo.provider.UserServiceDefaultImpl;
import com.dianping.pigeon.remoting.ServiceFactory;
import com.dianping.pigeon.remoting.provider.config.ProviderConfig;

public class Server {

	public static void main(String[] args) throws Exception {
		ServiceFactory.addService(UserService.class, new UserServiceDefaultImpl());

		ServiceFactory.addService(EchoService.class, new EchoServiceDefaultImpl());

		ProviderConfig<EchoService> providerConfig1 = new ProviderConfig<EchoService>(EchoService.class,
				new EchoServiceImpl1());
		providerConfig1.setVersion("1.0.0");
		ServiceFactory.addService(providerConfig1);

		ProviderConfig<EchoService> providerConfig2 = new ProviderConfig<EchoService>(EchoService.class,
				new EchoServiceImpl2());
		providerConfig2.setVersion("2.0.0");
		ServiceFactory.addService(providerConfig2);

		System.in.read();
	}

}
