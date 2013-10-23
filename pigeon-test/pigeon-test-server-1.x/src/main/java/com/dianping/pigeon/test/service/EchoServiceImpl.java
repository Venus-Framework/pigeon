/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.test.service;


public class EchoServiceImpl implements EchoService {

	@Override
    public String echo(String input) {
        System.out.println("Received: " + input);
        //throw new RuntimeException("error while receive msg:" + input);
        return "Echo: " + input;
    }

	@Override
	public String echoWithException(String input) {
		System.out.println("received: " + input);
		throw new EchoException("error while receive msg:" + input);
	}
}
