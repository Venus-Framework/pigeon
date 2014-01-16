/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.demo.invoker.annotation;

import java.util.concurrent.atomic.AtomicInteger;

import com.dianping.pigeon.container.SpringContainer;
import com.dianping.pigeon.demo.ConfigLoader;

public class Client {

	private static SpringContainer CLIENT_CONTAINER = new SpringContainer("classpath*:META-INF/spring/app-invoker.xml");

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		ConfigLoader.init();
		CLIENT_CONTAINER.start();
		AtomicInteger atomicInteger = new AtomicInteger();
		AnnotationTestService annotationTestService = (AnnotationTestService) CLIENT_CONTAINER
				.getBean("annotationTestService");
		for (;;) {
			try {
				String input = "annotationTestService_" + atomicInteger.incrementAndGet();
				System.out.println("input:" + input);
				System.out.println(annotationTestService.testEcho(input));
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
	}

}
