/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.test.client.integration.call;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.dianping.pigeon.test.client.AnnotationBaseInvokerTest;
import com.dianping.pigeon.test.client.PigeonAutoTest;
import com.dianping.pigeon.test.service.EchoService;

public class OnewayEchoServiceTest extends AnnotationBaseInvokerTest {
	
	@PigeonAutoTest(serviceName = "http://service.dianping.com/testService/echoService_1.0.0", serialize = "java", callMethod = "oneway")
	public EchoService echoService;

	@Test
	public void testOneway() {
		String msg = System.currentTimeMillis() + "";
		System.out.println(msg);
		String echo = echoService.echo(msg);
		System.out.println(echo);
		Assert.assertEquals(null, echo);
	}

	@Test
	public void testXmlOneway() {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("META-INF/spring/app-invoker.xml");
		EchoService service = (EchoService) context.getBean("echoServiceXml_oneway");
		String echo = service.echo("dianping");
		Assert.assertEquals(null, echo);
	}
}
