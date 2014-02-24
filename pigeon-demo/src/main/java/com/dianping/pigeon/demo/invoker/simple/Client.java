/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.demo.invoker.simple;

import java.util.concurrent.atomic.AtomicInteger;

import com.dianping.dpsf.async.ServiceCallback;
import com.dianping.pigeon.demo.EchoService;
import com.dianping.pigeon.demo.invoker.EchoServiceCallback;
import com.dianping.pigeon.remoting.ServiceFactory;
import com.dianping.pigeon.util.ContextUtils;

public class Client {

	public static void main(String[] args) throws Exception {
		String url = "http://service.dianping.com/com.dianping.pigeon.demo.EchoService";
		EchoService service = ServiceFactory.getService(url, EchoService.class);

		ServiceCallback callback = new EchoServiceCallback();
		EchoService serviceWithCallback = ServiceFactory.getService(url, EchoService.class, callback);

		AtomicInteger atomicInteger = new AtomicInteger();
		for (;;) {
			try {
				String input = "echoService_" + atomicInteger.incrementAndGet();
				//System.out.println("input:" + input);
				ContextUtils.putContextValue("key1", input);
				
				//System.out.println("service result:" + service.echo(input));

				String input2 = "echoServiceWithCallback_" + atomicInteger.incrementAndGet();
				//System.out.println("input:" + input2);
				//serviceWithCallback.echo(input);
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
	}

}
