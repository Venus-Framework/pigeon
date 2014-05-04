/**
 * 
 */
package com.dianping.pigeon.test.client_1.x.benchmark.call;

import java.util.concurrent.atomic.AtomicLong;

import org.junit.Test;
import org.springframework.util.Assert;

import com.dianping.lion.client.ConfigCache;
import com.dianping.pigeon.test.client_1.x.BaseInvokerTest;
import com.dianping.pigeon.test.client_1.x.PigeonAutoTest;
import com.dianping.pigeon.test.service.EchoService;

/**
 * @author xiangwu
 * 
 */
public class DefaultTest extends BaseInvokerTest {

	@PigeonAutoTest(callMethod = "sync", serviceName = "http://service.dianping.com/testService/echoService_1.0.0", timeout = 500)
	public EchoService echoService;

	static AtomicLong counter = new AtomicLong(0);
	String startTime = System.currentTimeMillis() + "";

	@Test
	public void test() throws Throwable {
		int threads = 50;
		System.out.println("threads:" + threads);
		Assert.notNull(echoService);
		for (int i = 0; i < threads; i++) {
			ClientThread thread = new ClientThread(echoService);
			thread.start();
		}
		Thread.currentThread().join();
	}

	public static void main(String[] args) throws Throwable {
		DefaultTest test = new DefaultTest();
		test.start();
		test.test();
	}

	class ClientThread extends Thread {

		EchoService service = null;

		public ClientThread(EchoService service) {
			this.service = service;
		}

		public void run() {
			while (true) {
				String msg = null;
				try {
					// Thread.sleep(1000);
					long count = counter.addAndGet(1);
					String echo = service.echo(count + "");
					// System.out.println(echo);
					// Assert.assertEquals("echo:" + msg, echo);
					int size = 10000;
					if (count % size == 0) {
						long now = System.currentTimeMillis();
						long cost = now - Long.valueOf(startTime);
						float tps = size * 1000 / cost;
						System.out.println("" + tps);
						startTime = now + "";
					}
				} catch (Throwable e) {
					e.printStackTrace();
				}
			}
		}
	}
}