/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.demo.typical;

import com.dianping.pigeon.container.SpringContainer;
import com.dianping.pigeon.demo.EchoService;
import com.dianping.pigeon.demo.UserService;

public class ConcurrentClient {

	private static SpringContainer CLIENT_CONTAINER = new SpringContainer(
			"classpath*:META-INF/spring/typical/invoker.xml");

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		CLIENT_CONTAINER.start();

		final EchoService echoService = (EchoService) CLIENT_CONTAINER.getBean("echoService");
		EchoService echoServiceWithCallback = (EchoService) CLIENT_CONTAINER.getBean("echoServiceWithCallback");
		UserService userService = (UserService) CLIENT_CONTAINER.getBean("userService");

		for (int i = 0; i < 300; i++) {
			// Thread.sleep(1000);
			// InvokerHelper.setCancel(true);
			// InvokerHelper.setDefaultResult("hello, scott");
			Runnable r = new Runnable() {

				@Override
				public void run() {
					while (true) {
						try {
							//echoService.echo("scott");
							System.out.println(echoService.echo("scott"));
							//System.out.println(echoService.echo(2005));

							// System.out.println("getUserDetailArray="
							// +
							// Arrays.toString(userService.getUserDetailArray(new
							// User[] { new User("jack") }, true)));
							// System.out.println("getUserDetailList="
							// + userService.getUserDetailList(Arrays.asList(new
							// User[] { new User("jack") }), true));
							// System.out.println("getUserDetail=" +
							// userService.getUserDetail(new User("jack")));
						} catch (Exception e) {
							//e.printStackTrace();
						}
					}
				}

			};
			new Thread(r).start();
		}
		System.in.read();
		// echoServiceWithCallback.echo("echoServiceWithCallback_input");
	}
}
