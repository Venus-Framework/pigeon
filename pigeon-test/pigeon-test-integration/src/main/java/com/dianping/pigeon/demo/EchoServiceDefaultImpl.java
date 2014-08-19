/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.demo;


public class EchoServiceDefaultImpl implements EchoService {

	@Override
	public String echo(String input) {
		// throw new InvocationFailureException("error raised:" + input);
		// System.out.println(PhoenixContext.getInstance().getRequestId());
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			System.out.println("#########");
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		return "echo:" + input;
	}

	@Override
	public String echo2(String input, int size) {
		// throw new InvocationFailureException("error raised:" + input);
		return "echo2:" + input + ",size:" + size;
	}

}
