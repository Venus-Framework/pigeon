/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.demo.typical;

import com.dianping.pigeon.container.SpringContainer;
import com.dianping.pigeon.demo.EchoService;
import com.dianping.pigeon.demo.UserService;
import com.dianping.pigeon.demo.UserService.Role;
import com.dianping.pigeon.demo.UserService.User;
import com.dianping.pigeon.remoting.common.codec.kryo.KryoSerializer;

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
		KryoSerializer.registerClass(EchoService.class, 1001);
		KryoSerializer.registerClass(UserService.class, 1002);
		KryoSerializer.registerClass(User.class, 1003);
		KryoSerializer.registerClass(Role.class, 1004);
		System.in.read();
	}

}
