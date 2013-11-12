/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.demo.invoker.spring;

import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.dianping.pigeon.demo.EchoService;
import com.dianping.pigeon.demo.loader.BootstrapLoader;

/**
 * @author jianhuihuang
 * @version $Id: Client.java, v 0.1 2013-6-22 下午7:04:30 jianhuihuang Exp $
 */
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
