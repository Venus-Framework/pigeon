/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.demo.provider.spring;

import com.dianping.pigeon.container.SpringContainer;
import com.dianping.pigeon.demo.provider.MyProviderProcessInterceptor;
import com.dianping.pigeon.remoting.provider.process.ProviderProcessInterceptorFactory;

public class Server {

	private static SpringContainer SERVER_CONTAINER = new SpringContainer("classpath*:META-INF/spring/app-provider.xml");

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		ProviderProcessInterceptorFactory.registerInterceptor(new MyProviderProcessInterceptor());
		SERVER_CONTAINER.start();
		System.in.read();
	}

}
