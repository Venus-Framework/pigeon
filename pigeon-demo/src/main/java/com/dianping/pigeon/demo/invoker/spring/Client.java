/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.demo.invoker.spring;

import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.Assert;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.dianping.pigeon.demo.EchoService;
import com.dianping.pigeon.demo.loader.BootstrapLoader;
import com.dianping.pigeon.demo.provider.EchoException;

/**
 * @author jianhuihuang
 * @version $Id: Client.java, v 0.1 2013-6-22 下午7:04:30 jianhuihuang Exp $
 */
public class Client {
	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		BootstrapLoader.startupInvoker();
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("META-INF/spring/app-invoker.xml");
		AtomicInteger atomicInteger = new AtomicInteger();
		for (;;) {
			try {
				EchoService service = (EchoService) context.getBean("echoService");
				String input = "echoService_" + atomicInteger.incrementAndGet();
				System.out.println("input:" + input);
				String echo = service.echoWithException(input);
				System.out.println("result:" + echo);
			} catch (Throwable e) {
				Assert.assertEquals(EchoException.class, e.getClass());
			}
		}
	}

}
