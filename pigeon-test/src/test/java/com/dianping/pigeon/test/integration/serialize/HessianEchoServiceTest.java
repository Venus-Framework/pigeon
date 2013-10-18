/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.test.integration.serialize;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.dianping.pigeon.test.PigeonAutoTest;
import com.dianping.pigeon.test.SingleServerBaseTest;
import com.dianping.pigeon.test.service.EchoService;

public class HessianEchoServiceTest extends SingleServerBaseTest {

	@PigeonAutoTest(serviceName = "http://service.dianping.com/testService/echoService_1.0.0", serialize = "hessian")
	public EchoService echoService;

	@Test
	public void test() {
		String echo = echoService.echo("dianping");
		Assert.assertEquals("Echo: dianping", echo);
	}

	@Test
	public void testXml() {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("META-INF/spring/app-invoker.xml");
		EchoService service = (EchoService) context.getBean("echoServiceXml");
		String echo = service.echo("dianping");
		Assert.assertEquals("Echo: dianping", echo);
	}
}
