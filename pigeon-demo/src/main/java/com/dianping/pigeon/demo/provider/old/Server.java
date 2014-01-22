/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.demo.provider.old;

import java.util.HashMap;
import java.util.Map;

import com.dianping.dpsf.spring.ServiceRegistry;
import com.dianping.pigeon.demo.EchoService;
import com.dianping.pigeon.demo.UserService;
import com.dianping.pigeon.demo.provider.EchoServiceDefaultImpl;
import com.dianping.pigeon.demo.provider.UserServiceDefaultImpl;

public class Server {

	public static void main(String[] args) throws Exception {
		ServiceRegistry sr = new ServiceRegistry();
		Map<String, Object> services = new HashMap<String, Object>();
		services.put(EchoService.class.getName(), new EchoServiceDefaultImpl());
		services.put(UserService.class.getName(), new UserServiceDefaultImpl());
		sr.setServices(services);
		sr.init();

		System.in.read();
	}

}
