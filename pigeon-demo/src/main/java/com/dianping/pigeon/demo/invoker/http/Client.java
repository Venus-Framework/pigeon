/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.demo.invoker.http;

import java.util.concurrent.atomic.AtomicInteger;

import com.dianping.pigeon.demo.ConfigLoader;
import com.dianping.pigeon.demo.EchoService;
import com.dianping.pigeon.remoting.ServiceFactory;
import com.dianping.pigeon.remoting.invoker.config.InvokerConfig;

public class Client {

	public static void main(String[] args) throws Exception {
		ConfigLoader.init();

		InvokerConfig<EchoService> config = new InvokerConfig<EchoService>(EchoService.class);
		config.setProtocol(InvokerConfig.PROTOCOL_DEFAULT);
		config.setSerialize(InvokerConfig.SERIALIZE_JSON);
		EchoService service = ServiceFactory.getService(config);

		AtomicInteger atomicInteger = new AtomicInteger();
		for (;;) {
			try {
				String input = "echoService_" + atomicInteger.incrementAndGet();
				System.out.println("input:" + input);
				System.out.println("service result:" + service.echo(input));
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
	}

}
