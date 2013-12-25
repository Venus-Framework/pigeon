/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.demo.provider.version;

import com.dianping.pigeon.demo.EchoService;

public class EchoServiceImpl1 implements EchoService {

	@Override
	public String echo(String input) {
		System.out.println("version 1.0.0, received: " + input);
		return "version 1.0.0, echo:" + input;
	}

}
