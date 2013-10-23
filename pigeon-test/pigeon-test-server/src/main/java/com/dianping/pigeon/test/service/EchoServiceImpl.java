/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.test.service;

import com.dianping.pigeon.extension.ExtensionLoader;
import com.dianping.pigeon.remoting.provider.ServerFactory;

public class EchoServiceImpl implements EchoService {

	@Override
	public String echo(String input) {
		System.out.println("server port:"
				+ ExtensionLoader.getExtension(ServerFactory.class).getPort()
				+ ", received: " + input);
		return "Echo: " + input;
	}

	@Override
	public String echoWithException(String input) {
		System.out.println("server port:"
				+ ExtensionLoader.getExtension(ServerFactory.class).getPort()
				+ ", received: " + input);
		throw new EchoException("error while receive msg:" + input);
	}
}
