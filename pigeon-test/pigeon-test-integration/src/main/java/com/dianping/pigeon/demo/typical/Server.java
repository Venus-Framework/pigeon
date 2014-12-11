/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.demo.typical;

import java.util.Date;

import com.dianping.pigeon.container.SpringContainer;
import com.dianping.pigeon.demo.EchoService;
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
		KryoSerializer.registerClass(Date.class, 100);
		KryoSerializer.registerClass(EchoService.class, 101);
		System.in.read();
	}

}
