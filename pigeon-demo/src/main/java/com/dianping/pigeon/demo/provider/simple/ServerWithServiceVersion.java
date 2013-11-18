/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.demo.provider.simple;

import com.dianping.pigeon.demo.EchoService;
import com.dianping.pigeon.demo.loader.ConfigLoader;
import com.dianping.pigeon.demo.provider.EchoServiceImpl1;
import com.dianping.pigeon.demo.provider.EchoServiceImpl2;
import com.dianping.pigeon.demo.provider.EchoServiceImpl3;
import com.dianping.pigeon.remoting.common.service.ServiceFactory;
import com.dianping.pigeon.remoting.provider.component.ProviderConfig;

public class ServerWithServiceVersion {

	public static void main(String[] args) throws Exception {
		ConfigLoader.init();

		ServiceFactory.publishService(EchoService.class, new EchoServiceImpl1());
		
		ProviderConfig<EchoService> providerConfig2 = new ProviderConfig<EchoService>(EchoService.class, new EchoServiceImpl2());
		providerConfig2.setVersion("2.0.0");
		ServiceFactory.publishService(providerConfig2);
		
		ProviderConfig<EchoService> providerConfig3 = new ProviderConfig<EchoService>(EchoService.class, new EchoServiceImpl3());
		providerConfig3.setVersion("3.0.0");
		ServiceFactory.publishService(providerConfig3);
		
		System.in.read();
	}

}
