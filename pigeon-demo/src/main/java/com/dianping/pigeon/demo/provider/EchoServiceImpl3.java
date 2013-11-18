/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.demo.provider;

import com.dianping.pigeon.demo.EchoService;

public class EchoServiceImpl3 implements EchoService {

	@Override
	public String echo(String input) {
		System.out.println("received: " + input);
		return "version 3.0.0, echo:" + input;
	}

}
