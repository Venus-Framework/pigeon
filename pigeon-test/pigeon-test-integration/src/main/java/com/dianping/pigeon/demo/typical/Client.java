/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.demo.typical;

import java.util.Date;

import com.dianping.pigeon.container.SpringContainer;
import com.dianping.pigeon.demo.EchoService;
import com.dianping.pigeon.demo.UserService;
import com.dianping.pigeon.demo.UserService.User;
import com.dianping.pigeon.remoting.common.codec.kryo.KryoSerializer;

public class Client {

	private static SpringContainer CLIENT_CONTAINER = new SpringContainer(
			"classpath*:META-INF/spring/typical/invoker.xml");

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		CLIENT_CONTAINER.start();
		KryoSerializer.registerClass(Date.class, 100);
		KryoSerializer.registerClass(EchoService.class, 101);
		KryoSerializer.registerClass(UserService.class, 102);
		KryoSerializer.registerClass(User.class, 103);

		EchoService echoService = (EchoService) CLIENT_CONTAINER.getBean("echoService");
		EchoService echoServiceWithCallback = (EchoService) CLIENT_CONTAINER.getBean("echoServiceWithCallback");

		UserService userService = (UserService) CLIENT_CONTAINER.getBean("userService");

		User user = new User();
		user.setUsername("jason");
		User[] users = new User[] { user };
		while (true) {
			try {
				System.out.println(echoService.echo("hi"));
				System.out.println(userService.getUserDetail(users, false));

				// PhoenixContext.getInstance().setRequestId("aaaa1111111");
				// ExecutionContextHolder.setTrackerContext(new
				// TrackerContext());
				// int size = (int) (new Random().nextDouble() * 24);
				// StringBuilder sb = new StringBuilder();
				// for (int i = 0; i < size; i++) {
				// sb.append("i");
				// }
				// echoService.echo(sb.toString());
			} catch (Exception e) {
			}
		}
	}

}
