/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.demo.invoker.http;

import java.util.concurrent.atomic.AtomicInteger;

import com.dianping.pigeon.demo.EchoService;
import com.dianping.pigeon.demo.UserService;
import com.dianping.pigeon.demo.UserService.User;
import com.dianping.pigeon.remoting.ServiceFactory;
import com.dianping.pigeon.remoting.invoker.config.InvokerConfig;

public class Client {

	public static void main(String[] args) throws Exception {
		InvokerConfig<EchoService> echoConfig = new InvokerConfig<EchoService>(EchoService.class);
		echoConfig.setProtocol(InvokerConfig.PROTOCOL_DEFAULT);
		echoConfig.setSerialize(InvokerConfig.SERIALIZE_JSON);
		EchoService echoService = ServiceFactory.getService(echoConfig);

		InvokerConfig<UserService> userConfig = new InvokerConfig<UserService>(UserService.class);
		userConfig.setProtocol(InvokerConfig.PROTOCOL_HTTP);
		userConfig.setSerialize(InvokerConfig.SERIALIZE_JSON);
		UserService userService = ServiceFactory.getService(userConfig);

		AtomicInteger atomicInteger = new AtomicInteger();
		for (;;) {
			try {
				int no = atomicInteger.incrementAndGet();
				String echoInput = "echoService_" + no;
				System.out.println("echo input:" + echoInput);
				System.out.println("echo service result:" + echoService.echo(echoInput));

				User user = new User();
				user.setUsername("user_" + no);
				System.out.println("user input:" + user);
				System.out.println("user service result:" + userService.getUserDetail(user, false));
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
	}

}
