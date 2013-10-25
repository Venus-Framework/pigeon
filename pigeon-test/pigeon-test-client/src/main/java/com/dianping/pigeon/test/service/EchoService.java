/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.test.service;

public interface EchoService {
	String echo(String input);
	
	String echoWithException1(String input);
	
	String echoWithException2(String input);
	
	String echoWithServerInfo(String input);
}
