/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.test.service;

import org.apache.log4j.Logger;
import org.junit.Assert;

import com.dianping.dpsf.async.ServiceCallback;
import com.dianping.dpsf.exception.DPSFException;

public class EchoServiceCallbackImpl implements ServiceCallback {

	private static final Logger logger = Logger.getLogger(EchoServiceCallbackImpl.class);

	String expectedResult = null;

	public void setExpectedResult(String result) {
		expectedResult = result;
	}

	@Override
	public void callback(Object result) {
		System.out.println("Callback:" + result);
		Assert.assertEquals(expectedResult, result);
	}

	@Override
	public void serviceException(Exception e) {
		logger.error("", e);
	}

	@Override
	public void frameworkException(DPSFException e) {
		logger.error("", e);
	}

}
