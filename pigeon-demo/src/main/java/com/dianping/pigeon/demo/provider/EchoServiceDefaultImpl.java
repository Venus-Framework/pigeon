/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.demo.provider;

import com.dianping.pigeon.demo.EchoService;
import com.dianping.pigeon.util.ContextUtils;

public class EchoServiceDefaultImpl implements EchoService {

	@Override
	public String echo(String input) {
		System.out.println("received: " + input);
		System.out.println(ContextUtils.getContextValue("key1"));

		return "echo:" + input;
	}
}
