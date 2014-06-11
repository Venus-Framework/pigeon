/**
 * 
 */
package com.dianping.pigeon.test.client.benchmark.call;

import java.util.concurrent.atomic.AtomicLong;

import org.junit.Test;
import org.springframework.util.Assert;

import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.extension.ExtensionLoader;
import com.dianping.pigeon.test.client.BaseInvokerTest;
import com.dianping.pigeon.test.client.PigeonAutoTest;
import com.dianping.pigeon.test.service.EchoService;

/**
 * @author xiangwu
 * 
 */
public class DefaultTest extends BaseInvokerTest {

	@PigeonAutoTest(callMethod = "sync", protocol = "default", serialize = "hessian", url = "http://service.dianping.com/testService/echoService_1.0.0", timeout = 100)
	public EchoService echoService;

	static AtomicLong counter = new AtomicLong(0);
	String startTime = System.currentTimeMillis() + "";
	ConfigManager configManager = ExtensionLoader.getExtension(ConfigManager.class);

	@Test
	public void test() throws Throwable {
		int threads = 20;
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
					// Thread.sleep(5);
					// msg = System.currentTimeMillis() + "" +
					// Math.abs(RandomUtils.nextLong());
					// Assert.assertEquals("echo:" + msg, echo);
					long count = counter.addAndGet(1);
					String echo = service.echo("input:" + count);
					// System.out.println(echo);
					int size = 10000;
					if (count % size == 0) {
						long now = System.currentTimeMillis();
						long cost = now - Long.valueOf(startTime);
						float tps = size * 1000 / cost;
						System.out.println("tps:" + tps + ",cost:" + cost);
						startTime = now + "";
					}
				} catch (Throwable e) {
					e.printStackTrace();
				}
			}
		}
	}
}