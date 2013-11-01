/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.test.client_1.x.integration.call;

import org.junit.Assert;
import org.junit.Test;

import com.dianping.pigeon.test.client_1.x.BaseInvokerTest;
import com.dianping.pigeon.test.client_1.x.PigeonAutoTest;
import com.dianping.pigeon.test.service.EchoService;
import com.dianping.pigeon.test.service.EchoServiceCallbackImpl;

/**
 * 
 * 
 * @author jianhuihuang
 * @version $Id: EchoServiceCallbackTest.java, v 0.1 2013-7-26 上午10:42:47
 *          jianhuihuang Exp $
 */
public class CallbackEchoServiceTest extends BaseInvokerTest {

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

}
