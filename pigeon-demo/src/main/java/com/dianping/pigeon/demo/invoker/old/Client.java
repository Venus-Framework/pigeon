/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.demo.invoker.old;

import java.util.concurrent.atomic.AtomicInteger;

import com.dianping.dpsf.async.ServiceCallback;
import com.dianping.dpsf.spring.ProxyBeanFactory;
import com.dianping.pigeon.demo.EchoService;
import com.dianping.pigeon.demo.invoker.EchoServiceCallback;

public class Client {

	public static void main(String[] args) throws Exception {
		ProxyBeanFactory pf = new ProxyBeanFactory();
		pf.setIface(EchoService.class.getName());
		pf.init();
		EchoService service = (EchoService) pf.getObject();

		ServiceCallback callback = new EchoServiceCallback();
		ProxyBeanFactory pfCallback = new ProxyBeanFactory();
		pfCallback.setIface(EchoService.class.getName());
		pfCallback.setCallback(callback);
		pfCallback.init();
		EchoService serviceWithCallback = (EchoService) pfCallback.getObject();

		AtomicInteger atomicInteger = new AtomicInteger();
		for (;;) {
			try {
				String input = "echoService_" + atomicInteger.incrementAndGet();
				System.out.println("input:" + input);
				System.out.println("service result:" + service.echo(input));

				String input2 = "echoServiceWithCallback_" + atomicInteger.incrementAndGet();
				System.out.println("input:" + input2);
				serviceWithCallback.echo(input);
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
	}

}
