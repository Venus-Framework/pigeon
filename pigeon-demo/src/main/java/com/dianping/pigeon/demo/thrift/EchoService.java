/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.demo.thrift;

import java.io.IOException;
import java.util.Map;

import com.facebook.swift.service.ThriftMethod;
import com.facebook.swift.service.ThriftService;

@ThriftService
public interface EchoService {

	@ThriftMethod
	String echo(String msg);

	@ThriftMethod
	String echoInteger(Integer size);

	@ThriftMethod
	String echoWithException(String msg) throws IOException;

	@ThriftMethod
	Gender echoEnum(Gender gender);

	@ThriftMethod
	long now();

	@ThriftMethod
	boolean isMale();

	@ThriftMethod
	Map<String, String> echoMap(Map<String, String> values);

	public enum Gender {
		AUTO, MALE, FEMALE, XXX;
	}
}
