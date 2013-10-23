/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.test.compatibility.call;

import org.junit.Assert;
import org.junit.Test;

import com.dianping.pigeon.test.PigeonAutoTest;
import com.dianping.pigeon.test.AnnotationBaseInvokerTest;
import com.dianping.pigeon.test.service.EchoService;

public class DefaultSyncEchoServiceTest extends AnnotationBaseInvokerTest {

	@PigeonAutoTest(serviceName = "http://service.dianping.com/testService/echoService_1.0.0", callMethod = "sync")
	public EchoService echoService;

	@Test
	public void test() {
		String msg = System.currentTimeMillis() + "";
		System.out.println(msg);
		String echo = echoService.echo(msg);
		System.out.println(echo);
		Assert.assertEquals("Echo: " + msg, echo);
	}

}
