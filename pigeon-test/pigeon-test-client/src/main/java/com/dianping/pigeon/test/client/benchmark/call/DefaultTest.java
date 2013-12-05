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

	@PigeonAutoTest(callMethod = "sync", url = "http://service.dianping.com/testService/echoService_1.0.0", timeout = 2000)
	public EchoService echoService1;
	
	@PigeonAutoTest(callMethod = "sync", url = "http://service.dianping.com/testService/echoService_2.0.0", timeout = 2000)
	public EchoService echoService2;

	static AtomicLong counter = new AtomicLong(0);
	String startTime = System.currentTimeMillis() + "";
	ConfigManager configManager = ExtensionLoader.getExtension(ConfigManager.class);

	@Test
	public void test() throws Throwable {
		int threads = configManager.getIntValue("pigeon.test.threads", 50);
		System.out.println("threads:" + threads);

		Assert.notNull(echoService1);
		Assert.notNull(echoService2);

		for (int i = 0; i < threads; i++) {
			ClientThread thread = new ClientThread(echoService1, echoService2);
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

		EchoService service1 = null;
		EchoService service2 = null;

		public ClientThread(EchoService service1, EchoService service2) {
			this.service1 = service1;
			this.service2 = service2;
		}

		public void run() {
			while (true) {
				String msg = null;
				try {
					//Thread.sleep(5);
					//msg = System.currentTimeMillis() + "" + Math.abs(RandomUtils.nextLong());
					// System.out.println(echo);
					// Assert.assertEquals("echo:" + msg, echo);
					long count = counter.addAndGet(1);
					String echo1 = service1.echo(count + "");
					String echo2 = service2.echo(count + "");
					//System.out.println(echo1);
					//System.out.println(echo2);
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