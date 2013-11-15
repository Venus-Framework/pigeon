/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.demo.invoker.simple;

import java.util.concurrent.atomic.AtomicInteger;

import com.dianping.pigeon.demo.EchoService;
import com.dianping.pigeon.demo.invoker.EchoServiceCallback;
import com.dianping.pigeon.demo.loader.ConfigLoader;
import com.dianping.pigeon.remoting.common.service.ServiceFactory;
import com.dianping.pigeon.remoting.invoker.component.InvokerConfig;
import com.dianping.pigeon.remoting.invoker.component.async.ServiceCallback;

public class Client {

	public static void main(String[] args) throws Exception {
		ConfigLoader.init();

		EchoService service1 = ServiceFactory.getService(EchoService.class);
		
		InvokerConfig<EchoService> invokerConfig = new InvokerConfig<EchoService>(EchoService.class);
		invokerConfig.setVersion("2.0.0");
		EchoService service2 = ServiceFactory.getService(invokerConfig);
		
		ServiceCallback callback = new EchoServiceCallback();
		EchoService serviceWithCallback = ServiceFactory.getService(EchoService.class, callback);

		AtomicInteger atomicInteger = new AtomicInteger();
		for (;;) {
			try {
				String input = "echoService_" + atomicInteger.incrementAndGet();
				System.out.println("input:" + input);
				
				System.out.println("service1 result:" + service1.echo(input));
				
				System.out.println("service2 result:" + service2.echo(input));

				String input2 = "echoServiceWithCallback_" + atomicInteger.incrementAndGet();
				System.out.println("input:" + input2);
				serviceWithCallback.echo(input);
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
	}

}
