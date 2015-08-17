/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.demo.typical;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import com.dianping.pigeon.container.SpringContainer;
import com.dianping.pigeon.demo.EchoService;

public class ConcurrentClient {

	private static SpringContainer CLIENT_CONTAINER = new SpringContainer(
			"classpath*:META-INF/spring/typical/invoker.xml");

	static AtomicInteger counter = new AtomicInteger(0);

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		CLIENT_CONTAINER.start();

		final EchoService echoService = (EchoService) CLIENT_CONTAINER.getBean("echoService");

		int threads = 20;

		ExecutorService executor = Executors.newFixedThreadPool(threads);
		for (int i = 0; i < threads; i++) {
			executor.submit(new Runnable() {

				@Override
				public void run() {
					while (true) {
						try {
							System.out.println(echoService.echo("" + counter.getAndIncrement()));
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			});
		}
	}

}
