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
import com.dianping.pigeon.test.service.protobuf.EchoProtos;
import com.dianping.pigeon.test.service.protobuf.EchoProtos.EchoResponse;

/**
 * @author xiangwu
 * 
 */
public class ProtobufTest extends BaseInvokerTest {

	@PigeonAutoTest(callMethod = "sync", protocol = "http", serialize = "protobuf", url = "http://service.dianping.com/testService/echoServiceProtobuf_1.0.0", timeout = 2000)
	public EchoProtos.EchoService.BlockingInterface echoService;

	static AtomicLong counter = new AtomicLong(0);
	String startTime = System.currentTimeMillis() + "";
	ConfigManager configManager = ExtensionLoader.getExtension(ConfigManager.class);

	@Test
	public void test() throws Throwable {
		int threads = configManager.getIntValue("pigeon.test.threads", 50);
		System.out.println("threads:" + threads);
		threads = 50;
		Assert.notNull(echoService);
		for (int i = 0; i < threads; i++) {
			ClientThread thread = new ClientThread(echoService);
			thread.start();
		}
		Thread.currentThread().join();
	}

	public static void main(String[] args) throws Throwable {
		ProtobufTest test = new ProtobufTest();
		test.start();
		test.test();
	}

	class ClientThread extends Thread {

		EchoProtos.EchoService.BlockingInterface service = null;

		public ClientThread(EchoProtos.EchoService.BlockingInterface service) {
			this.service = service;
		}

		public void run() {
			while (true) {
				try {
					long count = counter.addAndGet(1);
					EchoProtos.EchoRequest req = EchoProtos.EchoRequest.newBuilder().setMessage(count + "").build();
					EchoResponse resp = service.echo(null, req);
					// System.out.println(resp.getResult());
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