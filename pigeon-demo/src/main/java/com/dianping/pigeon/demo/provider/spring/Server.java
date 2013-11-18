/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.demo.provider.spring;

import com.dianping.pigeon.demo.loader.BootstrapLoader;

public class Server {

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		BootstrapLoader.startupProvider();
		
		System.in.read();
	}

}
