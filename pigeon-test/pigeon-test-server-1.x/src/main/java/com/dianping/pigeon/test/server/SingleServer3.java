/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.test.server;

import com.dianping.pigeon.test.loader.SpringLoader;

public class SingleServer3 {

	public static void main(String[] args) throws Exception {
		SpringLoader.startupProvider(4627);
		Thread.currentThread().join();
	}

}
