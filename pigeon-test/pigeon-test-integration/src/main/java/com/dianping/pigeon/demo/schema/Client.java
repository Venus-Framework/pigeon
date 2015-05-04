/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.demo.schema;

import com.dianping.pigeon.container.SpringContainer;
import com.dianping.pigeon.demo.EchoService;

public class Client {

	private static SpringContainer CLIENT_CONTAINER = new SpringContainer(
			"classpath*:META-INF/spring/schema/invoker.xml");

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		CLIENT_CONTAINER.start();

		EchoService echoService = (EchoService) CLIENT_CONTAINER.getBean("echoService");
		EchoService echoServiceWithCallback = (EchoService) CLIENT_CONTAINER.getBean("echoServiceWithCallback");

		while (true) {
			System.out.println(echoService.echo("echoService_input"));
		}
		// for (int i = 0; i < 30; i++) {
		// new Thread(new Task(echoService)).start();
		// }
		// System.in.read();
		// echoServiceWithCallback.echo("echoServiceWithCallback_input");
	}

	static class Task implements Runnable {

		EchoService echoService;

		public Task(EchoService echoService) {
			this.echoService = echoService;
		}

		@Override
		public void run() {
			while (true) {
				try {
					echoService.echo("echoService_input");
				} catch (Exception e) {
				}
			}
		}

	}
}
