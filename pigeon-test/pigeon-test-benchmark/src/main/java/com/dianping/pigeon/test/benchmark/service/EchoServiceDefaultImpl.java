/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.test.benchmark.service;

import java.util.Date;

import com.dianping.pigeon.remoting.provider.config.annotation.Service;

@Service(url = "com.dianping.pigeon.test.benchmark.service.EchoService")
public class EchoServiceDefaultImpl implements EchoService {

	@Override
	public String echo(String input) {
		return "echo:" + input;
	}

	@Override
	public Date now() {
		return new Date();
	}

}
