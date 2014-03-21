/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.demo.invoker.spring;

import java.util.concurrent.atomic.AtomicInteger;

import com.dianping.pigeon.container.SpringContainer;
import com.dianping.pigeon.demo.EchoService;
import com.dianping.pigeon.demo.invoker.MyInvokerProcessInterceptor;
import com.dianping.pigeon.remoting.invoker.process.InvokerProcessInterceptorFactory;

public class Client {

	private static SpringContainer CLIENT_CONTAINER = new SpringContainer("classpath*:META-INF/spring/app-invoker.xml");

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		InvokerProcessInterceptorFactory.registerInterceptor(new MyInvokerProcessInterceptor());

		CLIENT_CONTAINER.start();
		AtomicInteger atomicInteger = new AtomicInteger();
		EchoService defaultEchoService = (EchoService) CLIENT_CONTAINER.getBean("defaultEchoService");
		EchoService echoServiceWithCallback = (EchoService) CLIENT_CONTAINER.getBean("echoServiceWithCallback");
		//for (;;) {
			try {
				String input = "echoService_" + atomicInteger.incrementAndGet();
				System.out.println("input:" + input);
				System.out.println(defaultEchoService.echo(input));
				//echoServiceWithCallback.echo(input);
			} catch (Throwable e) {
				e.printStackTrace();
			}
		//}
	}

}
