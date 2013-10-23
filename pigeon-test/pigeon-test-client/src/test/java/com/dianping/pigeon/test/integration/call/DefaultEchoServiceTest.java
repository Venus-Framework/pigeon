/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.test.integration.call;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.dianping.pigeon.test.PigeonAutoTest;
import com.dianping.pigeon.test.AnnotationBaseInvokerTest;
import com.dianping.pigeon.test.service.EchoService;

public class DefaultEchoServiceTest extends AnnotationBaseInvokerTest {

	@PigeonAutoTest(serviceName = "http://service.dianping.com/testService/echoService_1.0.0", timeout = 500000)
	public EchoService echoService;

	@Test
	public void test() {
		String msg = System.currentTimeMillis() + "";
		System.out.println(msg);
		String echo = echoService.echo(msg);
		System.out.println(echo);
		Assert.assertEquals("Echo: " + msg, echo);
	}
	
	@Test
	public void testException() {
		String msg = System.currentTimeMillis() + "";
		System.out.println(msg);
		Exception exception = null;
		try {
			echoService.echoWithException(msg);
		} catch(Exception e) {
			exception = e;
			e.printStackTrace();
		}
		Assert.assertNotNull(exception);
	}

	@Test
	public void testXml() {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("META-INF/spring/app-invoker.xml");
		EchoService service = (EchoService) context.getBean("echoServiceXml");
		String echo = service.echo("dianping");
		Assert.assertEquals("Echo: dianping", echo);
	}
}
