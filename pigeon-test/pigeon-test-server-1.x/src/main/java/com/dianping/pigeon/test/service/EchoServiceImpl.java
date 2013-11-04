/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.test.service;

import org.apache.log4j.Logger;

public class EchoServiceImpl implements EchoService {

	Logger logger = Logger.getLogger(EchoServiceImpl.class);

	@Override
	public String echo(String input) {
		if (logger.isInfoEnabled()) {
			logger.info("received:" + input);
		}
		//System.out.println("received: " + input);
		return "echo:" + input;
	}

	@Override
	public String echoWithException1(String input) {
		System.out.println("received: " + input);
		throw new EchoException1("error while receive msg:" + input);
	}

	@Override
	public String echoWithException2(String input) {
		System.out.println("received: " + input);
		throw new EchoException2("error while receive msg:" + input);
	}

}
