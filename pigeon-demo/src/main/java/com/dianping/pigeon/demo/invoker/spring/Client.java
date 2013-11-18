/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.demo.invoker.spring;

import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.dianping.pigeon.demo.EchoService;
import com.dianping.pigeon.demo.loader.BootstrapLoader;

public class Client {
	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		BootstrapLoader.startupInvoker();
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("META-INF/spring/app-invoker.xml");
		AtomicInteger atomicInteger = new AtomicInteger();
		EchoService service1 = (EchoService) context.getBean("echoService1");
		EchoService service2 = (EchoService) context.getBean("echoService2");
		for (;;) {
			try {
				String input = "echoService_" + atomicInteger.incrementAndGet();
				System.out.println("input:" + input);
				System.out.println(service1.echo(input));
				System.out.println(service2.echo(input));
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
	}

}
