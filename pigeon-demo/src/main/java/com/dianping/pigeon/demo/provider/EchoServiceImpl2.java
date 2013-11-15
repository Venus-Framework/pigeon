/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.demo.provider;

import com.dianping.pigeon.demo.EchoService;

/**
 * 
 * 
 * @author jianhuihuang
 * @version $Id: EchoServiceImpl.java, v 0.1 2013-6-22 下午7:05:18 jianhuihuang
 *          Exp $
 */
public class EchoServiceImpl2 implements EchoService {

	@Override
	public String echo(String input) {
		System.out.println("received: " + input);
		return "version 2.0.0, echo:" + input;
	}

	@Override
	public String echoWithException(String input) {
		System.out.println("received: " + input);
		throw new EchoException("error while receiving msg:" + input);
	}

}
