/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.test.client.integration.vip;

import org.junit.Assert;
import org.junit.Test;

import com.dianping.pigeon.test.client.BaseInvokerTest;
import com.dianping.pigeon.test.client.PigeonAutoTest;
import com.dianping.pigeon.test.service.EchoService;

public class ErrorVipEchoServiceTest extends BaseInvokerTest {

	@PigeonAutoTest(env = "prod", vip = "127.0.0.2:4625", testVip = "127.0.0.1:4625")
	public EchoService echoService;

	@Test(expected = Exception.class)
	public void test() {
		String echo = echoService.echo("dianping");
		Assert.assertEquals("echo:dianping", echo);
	}

}
