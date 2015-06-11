/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.demo.typical;

import com.dianping.pigeon.container.SpringContainer;


public class Server2 {

	private static SpringContainer SERVER_CONTAINER = new SpringContainer(
			"classpath*:META-INF/spring/typical/provider2.xml");

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		SERVER_CONTAINER.start();
		System.in.read();
	}

}
