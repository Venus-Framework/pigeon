/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.demo.provider.simple;

import com.dianping.pigeon.demo.EchoService;
import com.dianping.pigeon.demo.loader.ConfigLoader;
import com.dianping.pigeon.demo.provider.EchoServiceImpl1;
import com.dianping.pigeon.remoting.common.service.ServiceFactory;

public class Server {

	public static void main(String[] args) throws Exception {
		ConfigLoader.init();
		
//		ServiceRegistry sr = new ServiceRegistry();
//		Map<String, Object> services = new HashMap<String, Object>();
//		services.put("com.dianping.pigeon.demo.EchoService", new EchoServiceImpl1());
//		sr.setServices(services);
//		sr.init();
		
		ServiceFactory.publishService(EchoService.class, new EchoServiceImpl1());
		
		System.in.read();
	}

}
