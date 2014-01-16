/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.demo.provider.annotation;

import com.dianping.pigeon.demo.AnnotationEchoService;
import com.dianping.pigeon.remoting.provider.config.annotation.Service;

@Service
public class AnnotationEchoServiceDefaultImpl implements AnnotationEchoService {

	@Override
	public String echo(String input) {
		System.out.println("received: " + input);
		return "annotation service echo:" + input;
	}

}
