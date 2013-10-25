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
import com.dianping.pigeon.test.service.EchoServiceCallbackImpl;

/**
 * 
 * 
 * @author jianhuihuang
 * @version $Id: EchoServiceCallbackTest.java, v 0.1 2013-7-26 上午10:42:47
 *          jianhuihuang Exp $
 */
public class CallbackEchoServiceTest extends AnnotationBaseInvokerTest {

	@PigeonAutoTest(serviceName = "http://service.dianping.com/testService/echoService_1.0.0", serialize = "java", callMethod = "callback", callback = "com.dianping.pigeon.test.service.EchoServiceCallbackImpl")
	public EchoService echoService;

	@Test
	public void testCallback() {
		String msg = System.currentTimeMillis() + "";
		System.out.println(msg);
		EchoServiceCallbackImpl echoCallback = (EchoServiceCallbackImpl) callback;
		echoCallback.setExpectedResult("echo:" + msg);
		String echo = echoService.echo(msg);
		System.out.println(echo);
		Assert.assertEquals(null, echo);
	}

	@Test
	public void testCallbackXml() {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("META-INF/spring/app-invoker.xml");
		EchoService service = (EchoService) context.getBean("echoServiceXml_callback");
		EchoServiceCallbackImpl echoCallback = (EchoServiceCallbackImpl) context.getBean("echoServiceServiceCallback");
		echoCallback.setExpectedResult("echo:dianping");
		String echo = service.echo("dianping");
		Assert.assertEquals(null, echo);
	}

}
