/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.demo.provider;


public class TestServiceImpl {

	public String echo(String input) {
		System.out.println("received: " + input);

		return "echo:" + input;
	}

}
