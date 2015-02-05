/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.demo.typical;

import com.dianping.pigeon.container.SpringContainer;

public class Server {

	private static SpringContainer SERVER_CONTAINER = new SpringContainer(
			"classpath*:META-INF/spring/typical/provider.xml");

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		SERVER_CONTAINER.start();
//		KryoSerializer.registerClass(Date.class, 20);
//		KryoSerializer.registerClass(List.class, 21);
//		KryoSerializer.registerClass(Serializable.class, 22);
//		KryoSerializer.registerClass(EchoService.class, 10001);
//		KryoSerializer.registerClass(UserService.class, 10002);
//		KryoSerializer.registerClass(User.class, 10003);
//		KryoSerializer.registerClass(Role.class, 10004);
		System.in.read();
	}

}
