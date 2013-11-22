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
import com.dianping.pigeon.remoting.invoker.component.async.ServiceCallback;

public class Client {

	public static void main(String[] args) throws Exception {
		ConfigLoader.init();

//		ProxyFactory pf = new ProxyFactory();
//		pf.setIface(EchoService.class);
//		pf.init();
//		EchoService service = (EchoService)pf.getProxy();
		
		EchoService service = ServiceFactory.getService(EchoService.class);

		ServiceCallback callback = new EchoServiceCallback();
		EchoService serviceWithCallback = ServiceFactory.getService(EchoService.class, callback);

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
