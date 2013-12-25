/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.demo.provider;

import com.dianping.pigeon.demo.EchoService;

public class EchoServiceImpl2 implements EchoService {

	@Override
	public String echo(String input) {
		System.out.println("received: " + input);
		return "version 2.0.0, echo:" + input;
	}

}
