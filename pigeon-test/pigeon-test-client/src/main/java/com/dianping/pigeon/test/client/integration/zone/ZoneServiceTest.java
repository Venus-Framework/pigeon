/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.test.client.integration.zone;

import org.junit.Assert;
import org.junit.Test;

import com.dianping.pigeon.test.client.BaseInvokerTest;
import com.dianping.pigeon.test.client.PigeonAutoTest;
import com.dianping.pigeon.test.service.EchoService;

public class ZoneServiceTest extends BaseInvokerTest {

	@PigeonAutoTest(url = "http://service.dianping.com/testService__ForMultiServer/echoService_1.0.0", zone = "pre")
	public EchoService echoService;

	@Test
	public void test() {
		String echo = echoService.echo("dianping");
		System.err.println(echo);
		Assert.assertEquals("echo:dianping", echo);
	}
}
