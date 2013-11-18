/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.demo.invoker.spring;

import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.dianping.pigeon.demo.EchoService;

public class CallbackClient {
	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("META-INF/spring/app-invoker.xml");
		AtomicInteger atomicInteger = new AtomicInteger();
		for (;;) {
			try {
				EchoService service = (EchoService) context.getBean("echoServiceWithCallback");
				String input = "echoServiceWithCallback_" + atomicInteger.incrementAndGet();
				System.out.println("input:" + input);
				service.echo(input);
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
	}
}
