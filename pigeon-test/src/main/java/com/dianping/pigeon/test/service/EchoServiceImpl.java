/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.test.service;

public class EchoServiceImpl implements EchoService {

	@Override
    public String echo(String input) {
        System.out.println("Received: " + input);
        return "Echo: " + input;
    }
}
