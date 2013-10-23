/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.test.compatibility.call;

import org.junit.Assert;
import org.junit.Test;

import com.dianping.pigeon.test.AnnotationBaseInvokerTest;
import com.dianping.pigeon.test.PigeonAutoTest;
import com.dianping.pigeon.test.service.EchoService;

public class DefaultEchoServiceTest extends AnnotationBaseInvokerTest {

	@PigeonAutoTest(serviceName = "http://service.dianping.com/testService/echoService_1.0.0", timeout = 500000)
	public EchoService echoService;

	public void setEchoService(EchoService echoService) {
		this.echoService = echoService;
	}

	@Test
	public void test() throws Exception {
		String msg = System.currentTimeMillis() + "";
		System.out.println(msg);
		String echo = echoService.echo(msg);
		System.out.println(echo);
		Assert.assertEquals("Echo: " + msg, echo);
	}

	@Test(expected = Exception.class)
	public void testException() throws Exception {
		String msg = System.currentTimeMillis() + "";
		System.out.println(msg);
		try {
			echoService.echoWithException(msg);
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
}
