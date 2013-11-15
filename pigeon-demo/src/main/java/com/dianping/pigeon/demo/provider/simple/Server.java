/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.demo.provider.simple;

import com.dianping.pigeon.demo.EchoService;
import com.dianping.pigeon.demo.loader.ConfigLoader;
import com.dianping.pigeon.demo.provider.EchoServiceImpl1;
import com.dianping.pigeon.demo.provider.EchoServiceImpl2;
import com.dianping.pigeon.remoting.common.service.ServiceFactory;
import com.dianping.pigeon.remoting.provider.component.ProviderConfig;

public class Server {

	public static void main(String[] args) throws Exception {
		ConfigLoader.init();

		ServiceFactory.publishService(EchoService.class, new EchoServiceImpl1());
		
		ProviderConfig<EchoService> providerConfig = new ProviderConfig<EchoService>(EchoService.class, new EchoServiceImpl2());
		providerConfig.setVersion("2.0.0");
		ServiceFactory.publishService(providerConfig);
		
		System.in.read();
	}

}
