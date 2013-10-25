package com.dianping.pigeon.test.client.integration.zone;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.dianping.pigeon.test.client.BalanceBaseTest;
import com.dianping.pigeon.test.client.PigeonAutoTest;
import com.dianping.pigeon.test.service.EchoService;

public class LoadBalanceTest extends BalanceBaseTest {
	@PigeonAutoTest(serviceName = "http://service.dianping.com/testService__ForMultiServer/echoService_1.0.0")
	public EchoService echoService;

	@PigeonAutoTest(serviceName = "http://service.dianping.com/testService_dpsf/echoService_1.0.0")
	public EchoService echoService2;

	private static Logger logger = Logger.getLogger(LoadBalanceTest.class);

	public List<Integer> getPorts() {
		List<Integer> ports = new ArrayList<Integer>();
		ports.add(4625);
		return ports;

	}

	@Test
	public void test() {
		start();
		try {
			serverLatch.await();
		} catch (InterruptedException e) {
			logger.error("Client wait time out", e);
		}
		for (int i = 0; i < CLIENT_NUM; i++) {
			new Thread() {
				public void run() {
					try {
						Thread.sleep(1000);
						initClient();
						String input = "dianping " + atomicInteger.incrementAndGet();
						String echo = echoService.echo(input);
						logger.error(echo);
						echo = echoService2.echo("Hi girl.");
						logger.error(echo);
					} catch (Exception e) {
						logger.error("Invoke Error", e);
					} finally {
						clientLatch.countDown();
					}
				}
			}.start();
		}

		try {
			clientLatch.await();
		} catch (InterruptedException e) {
			logger.error("", e);
		}
	}

	AtomicInteger atomicInteger = new AtomicInteger();
}
