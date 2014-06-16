/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.test.service;

import org.apache.log4j.Logger;

import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.util.NetUtils;

public class Echo1ServiceImpl implements EchoService {

	Logger logger = LoggerLoader.getLogger(Echo1ServiceImpl.class);

	int count = 0;
	String str = null;
	{
		StringBuilder s = new StringBuilder();
		for (int i = 0; i < 999999; i++) {
			s.append("i=").append(i).append(",");
		}
		str = s.toString();
	}

	@Override
	public String echo(String input) {
		return "echo1:" + input;
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

	@Override
	public String echoWithServerInfo(String input) {
		String serverInfo = "server:" + NetUtils.getFirstLocalIp();
		// System.out.println("reveived: " + input);
		return serverInfo + ", echo:" + input;
	}
}
