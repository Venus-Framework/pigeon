/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.demo.provider;

import com.dianping.pigeon.demo.EchoService;
import com.dianping.pigeon.extension.ExtensionLoader;
import com.dianping.pigeon.remoting.provider.ServerFactory;

/**
 * 
 * 
 * @author jianhuihuang
 * @version $Id: EchoServiceImpl.java, v 0.1 2013-6-22 下午7:05:18 jianhuihuang
 *          Exp $
 */
public class EchoServiceImpl implements EchoService {

	// private SayHelloService sayHelloService;

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
