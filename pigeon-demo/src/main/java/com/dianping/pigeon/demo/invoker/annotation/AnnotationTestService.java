/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.demo.invoker.annotation;

import com.dianping.pigeon.demo.AnnotationEchoService;
import com.dianping.pigeon.remoting.invoker.config.annotation.Reference;

public class AnnotationTestService {

	@Reference(timeout = 100, serialize = "json", protocol = "http")
	private AnnotationEchoService annotationEchoService;

	public String testEcho(String input) {
		return annotationEchoService.echo(input);
	}
}
