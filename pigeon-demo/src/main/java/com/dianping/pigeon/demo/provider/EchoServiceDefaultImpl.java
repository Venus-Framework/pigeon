/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.demo.provider;

import com.dianping.pigeon.demo.EchoService;

public class EchoServiceDefaultImpl implements EchoService {

	@Override
	public String echo(String input) {
		System.out.println("received: " + input);
		return "echo:" + input;
	}

}
