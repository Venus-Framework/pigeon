/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.demo.api;

import com.dianping.pigeon.demo.EchoService;
import com.dianping.pigeon.demo.interceptor.MyInvokerProcessInterceptor;
import com.dianping.pigeon.remoting.ServiceFactory;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.invoker.config.InvokerConfig;
import com.dianping.pigeon.remoting.invoker.process.InvokerProcessInterceptorFactory;

public class Client {

	public static void main(String[] args) throws Exception {
		InvokerProcessInterceptorFactory.registerInterceptor(new MyInvokerProcessInterceptor());

		String url = "com.dianping.pigeon.demo.EchoService";
		InvokerConfig<EchoService> config = new InvokerConfig<EchoService>(url, EchoService.class);
		config.setLoadbalance("com.dianping.pigeon.demo.api.MyLoadbalance");
		config.setCallType(Constants.CALL_ONEWAY);

		EchoService echoService = ServiceFactory.getService(config);
		int i = 0;
		while (true) {
			System.out.println("echoService result:" + echoService.echo("echoService_input" + (i++)));
		}

		// ServiceCallback callback = new EchoServiceCallback();
		// EchoService serviceWithCallback = ServiceFactory.getService(url,
		// EchoService.class, callback);
		// serviceWithCallback.echo("echoServiceWithCallback_input");
	}
}
