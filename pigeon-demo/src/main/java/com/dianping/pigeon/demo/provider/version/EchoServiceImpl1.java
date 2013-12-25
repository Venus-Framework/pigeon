/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.demo.provider;

import com.dianping.pigeon.demo.EchoService;

public class EchoServiceImpl1 implements EchoService {

	@Override
	public String echo(String input) {
		System.out.println("received: " + input);
		return "default version, echo:" + input;
	}

}
