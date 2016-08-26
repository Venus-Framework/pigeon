/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.demo.thrift;

import java.io.IOException;
import java.util.Map;

import com.dianping.lion.client.Lion;

public class EchoServiceDefaultImpl implements EchoService {

	public EchoServiceDefaultImpl() {
	}

	@Override
	public String echo(String msg) {
		try {
			Thread.sleep(Lion.getIntValue("pigeon-test.echo.sleep"));
		} catch (InterruptedException e) {
		}
		return "echo:" + msg;
	}

	@Override
	public String echoInteger(Integer size) {
		try {
			Thread.sleep(size);
		} catch (InterruptedException e) {
		}
		return "echo:" + size;
	}

	@Override
	public String echoWithException(String msg) throws IOException {
		throw new RuntimeException("error with echo service");
	}

	@Override
	public long now() {
		return System.currentTimeMillis();
	}

	@Override
	public Map<String, String> echoMap(Map<String, String> values) {
		System.out.println(values);
		return values;
	}

	@Override
	public boolean isMale() {
		return true;
	}

	@Override
	public Gender echoEnum(Gender gender) {
		Gender g = null;
		try {
			g = Gender.valueOf("XXX");
		} catch (RuntimeException e) {

		}
		if (g == null) {
			g = Gender.FEMALE;
		}
		return g;
	}

}
