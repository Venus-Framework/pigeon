/**
 * 
 */
package com.dianping.pigeon.test.client_1.x.benchmark.call;

import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang.math.RandomUtils;
import org.junit.Test;

import com.dianping.lion.EnvZooKeeperConfig;
import com.dianping.lion.client.ConfigCache;
import com.dianping.pigeon.test.client_1.x.AnnotationBaseInvokerTest;
import com.dianping.pigeon.test.client_1.x.PigeonAutoTest;
import com.dianping.pigeon.test.service.EchoService;

/**
 * @author xiangwu
 * 
 */
public class DefaultTest extends AnnotationBaseInvokerTest {

	@PigeonAutoTest(serviceName = "http://service.dianping.com/testService/echoService_1.0.0", timeout = 2000)
	public EchoService echoService;

	static AtomicLong counter = new AtomicLong(0);
	String startTime = System.currentTimeMillis() + "";

	@Test
	public void test() throws Throwable {
		int threads = 1;
		try {
			String threadsConfig = ConfigCache.getInstance(EnvZooKeeperConfig.getZKAddress()).getProperty(
					"pigeon.test.threads");
			if (threadsConfig != null) {
				threads = Integer.valueOf(threadsConfig);
			}
		} catch (Exception e) {
		}
		System.out.println("threads:" + threads);

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
					msg = System.currentTimeMillis() + "" + Math.abs(RandomUtils.nextLong());
					String echo = service.echo(msg);
					// System.out.println(echo);
					// Assert.assertEquals("echo:" + msg, echo);
					long count = counter.addAndGet(1);
					int size = 10000;
					if (count % size == 0) {
						long now = System.currentTimeMillis();
						long cost = now - Long.valueOf(startTime);
						float tps = size * 1000 / cost;
						System.out.println("start time:" + startTime + ",now:" + now + ",cost:" + cost + ",all count:"
								+ count + ",size:" + size + ",tps:" + tps);
						startTime = now + "";
					}
				} catch (Throwable e) {
					e.printStackTrace();
				}
			}
		}
	}
}