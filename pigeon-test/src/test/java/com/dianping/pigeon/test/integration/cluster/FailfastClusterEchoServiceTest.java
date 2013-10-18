/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.test.integration.cluster;

import org.junit.Assert;
import org.junit.Test;

import com.dianping.pigeon.test.MultiServerBaseTest;
import com.dianping.pigeon.test.PigeonAutoTest;
import com.dianping.pigeon.test.service.EchoService;

public class FailfastClusterEchoServiceTest extends MultiServerBaseTest {

	@PigeonAutoTest(serviceName = "http://service.dianping.com/testService/echoService_1.0.0", cluster = "failfast")
	public EchoService echoService;

	@Test
	public void test() {
		String echo = echoService.echo("dianping");
		Assert.assertEquals("Echo: dianping", echo);
	}
}
