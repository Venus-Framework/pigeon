/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.test.client_1.x.integration.call;

import java.lang.reflect.UndeclaredThrowableException;

import org.junit.Assert;
import org.junit.Test;

import com.dianping.pigeon.test.client_1.x.AnnotationBaseInvokerTest;
import com.dianping.pigeon.test.client_1.x.PigeonAutoTest;
import com.dianping.pigeon.test.service.EchoException2;
import com.dianping.pigeon.test.service.EchoService;

public class DefaultEchoServiceTest extends AnnotationBaseInvokerTest {

	@PigeonAutoTest(serviceName = "http://service.dianping.com/testService/echoService_1.0.0", timeout = 500000)
	public EchoService echoService;

	public void setEchoService(EchoService echoService) {
		this.echoService = echoService;
	}

	@Test
	public void test() throws Throwable {
		String msg = System.currentTimeMillis() + "";
		System.out.println(msg);
		String echo = echoService.echo(msg);
		System.out.println(echo);
		Assert.assertEquals("echo:" + msg, echo);
	}

	@Test(expected = ClassCastException.class)
	public void testException1() throws Throwable {
		String msg = System.currentTimeMillis() + "";
		System.out.println(msg);
		try {
			echoService.echoWithException1(msg);
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	@Test(expected = EchoException2.class)
	public void testException2() throws Throwable {
		String msg = System.currentTimeMillis() + "";
		System.out.println(msg);
		try {
			echoService.echoWithException2(msg);
		} catch (UndeclaredThrowableException e) {
			e.printStackTrace();
			throw e.getCause();
		}
	}
}
