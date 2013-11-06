/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.test.service;

import org.apache.log4j.Logger;

import com.dianping.pigeon.extension.ExtensionLoader;
import com.dianping.pigeon.remoting.provider.ServerFactory;
import com.dianping.pigeon.util.IpUtils;

public class EchoServiceImpl implements EchoService {

	Logger logger = Logger.getLogger(EchoServiceImpl.class);
	
	@Override
	public String echo(String input) {
		//System.out.println("received: " + input);
		return "echo:" + input;
	}

	@Override
	public String echoWithException1(String input) {
		System.out.println("server port:" + ExtensionLoader.getExtension(ServerFactory.class).getPort()
				+ ", received: " + input);
		throw new EchoException1("error while receive msg:" + input);
	}

	@Override
	public String echoWithException2(String input) {
		System.out.println("server port:" + ExtensionLoader.getExtension(ServerFactory.class).getPort()
				+ ", received: " + input);
		throw new EchoException2("error while receive msg:" + input);
	}

	@Override
	public String echoWithServerInfo(String input) {
		String serverInfo = "server:" + IpUtils.getFirstLocalIp() + ":"
				+ ExtensionLoader.getExtension(ServerFactory.class).getPort();
		// System.out.println("reveived: " + input);
		return serverInfo + ", echo:" + input;
	}
}
