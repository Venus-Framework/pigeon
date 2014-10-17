/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.demo;

import com.dianping.pigeon.util.ContextUtils;

public class EchoServiceDefaultImpl implements EchoService {

	@Override
	public String echo(String input) {
		// throw new InvocationFailureException("error raised:" + input);
		// System.out.println(input);
		// System.out.println(ContextUtils.getLocalContext("CLIENT_APP"));
		// try {
		// Thread.sleep(1000);
		// } catch (InterruptedException e) {
		// System.out.println("#########");
		// e.printStackTrace();
		// throw new RuntimeException(e);
		// }
		// System.out.println(input);
		return "echo:" + input;
	}

	@Override
	public String echo2(String input, int size) {
		// throw new InvocationFailureException("error raised:" + input);
		return "echo2:" + input + ",size:" + size;
	}

}
