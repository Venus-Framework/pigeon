/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.test.service;

import org.apache.log4j.Logger;
import org.junit.Assert;

import com.dianping.pigeon.monitor.Log4jLoader;
import com.dianping.pigeon.remoting.invoker.component.async.ServiceCallback;

public class EchoServiceCallbackImpl implements ServiceCallback {

	private static final Logger logger = Log4jLoader.getLogger(EchoServiceCallbackImpl.class);

	String expectedResult = null;

	public void setExpectedResult(String result) {
		expectedResult = result;
	}

	@Override
	public void callback(Object result) {
		System.out.println("Callback: " + result);
		Assert.assertEquals(expectedResult, result);
	}

	@Override
	public void serviceException(Exception e) {
		logger.error("", e);
	}

	@Override
	public void frameworkException(RuntimeException e) {
		logger.error("", e);
	}

}
