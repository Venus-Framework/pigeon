/**
 * 
 */
package com.dianping.pigeon.test.client.benchmark.call;

import com.dianping.pigeon.test.client.integration.call.DefaultEchoServiceTest;

/**
 * @author xiangwu
 * 
 */
public class DefaultTest {

	public static void main(String[] args) throws Exception {
		int threads = 10;
		if (args != null && args.length > 0) {
			threads = Integer.valueOf(args[0]);
		}

		DefaultEchoServiceTest test = new DefaultEchoServiceTest();
		test.start();

		for (int i = 0; i < threads; i++) {
			ClientThread thread = new ClientThread(test);
			thread.start();
		}
	}

}

class ClientThread extends Thread {

	DefaultEchoServiceTest test = null;

	public ClientThread(DefaultEchoServiceTest test) {
		this.test = test;
	}

	public void run() {
		while (true) {
			try {
				test.testWithServerInfo();
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
	}
}
