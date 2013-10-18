/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.test.compatibility;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.dianping.pigeon.test.PigeonAutoTest;
import com.dianping.pigeon.test.service.EchoService;

public class XmlConfigEchoServiceTest {

	public static String DEFAULT_SPRING_CONFIG = "classpath*:META-INF/dpsf/*.xml";

	public String getSpringPath() {
		return DEFAULT_SPRING_CONFIG;
	}

	@PigeonAutoTest(serviceName = "http://service.dianping.com/testService_dpsf/echoService_1.0.0")
	public EchoService echoService;

	@Test
	public void testXml() {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("META-INF/dpsf/app-invoker.xml");
		EchoService service = (EchoService) context.getBean("echoService");
		String echo = service.echo("dianping");
		Assert.assertEquals("Echo: dianping", echo);
	}

}
