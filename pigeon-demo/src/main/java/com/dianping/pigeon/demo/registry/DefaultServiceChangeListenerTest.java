package com.dianping.pigeon.demo.registry;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.Test;

import com.dianping.pigeon.registry.listener.DefaultServiceChangeListener;
import com.dianping.pigeon.registry.zookeeper.Utils;
import com.dianping.pigeon.remoting.invoker.ClientManager;
import com.dianping.pigeon.remoting.invoker.listener.ClusterListenerManager;
import com.dianping.pigeon.remoting.invoker.route.DefaultRouteManager;

public class DefaultServiceChangeListenerTest {

	@Test
	public void test1() throws Exception {
		final DefaultServiceChangeListener listener = new DefaultServiceChangeListener();
		final Random r = new Random();

		ClientManager.getInstance();
		ClusterListenerManager.getInstance();
		DefaultRouteManager router = new DefaultRouteManager();

		listener.onServiceHostChange("com.dianping.pigeon.demo.EchoService0",
				Utils.getServiceIpPortList("127.0.0.1:5008,127.0.0.1:5009,127.0.0.1:5010"));
		listener.onServiceHostChange("com.dianping.pigeon.demo.EchoService1",
				Utils.getServiceIpPortList("127.0.0.1:5008,127.0.0.1:5009,127.0.0.1:5010"));
		listener.onServiceHostChange("com.dianping.pigeon.demo.EchoService2",
				Utils.getServiceIpPortList("127.0.0.1:5008,127.0.0.1:5009,127.0.0.1:5010"));
		listener.onServiceHostChange("com.dianping.pigeon.demo.EchoService3",
				Utils.getServiceIpPortList("127.0.0.1:5008,127.0.0.1:5009,127.0.0.1:5010"));
		listener.onServiceHostChange("com.dianping.pigeon.demo.EchoService4",
				Utils.getServiceIpPortList("127.0.0.1:5008,127.0.0.1:5009,127.0.0.1:5010"));
		listener.onServiceHostChange("com.dianping.pigeon.demo.EchoService5",
				Utils.getServiceIpPortList("127.0.0.1:5008,127.0.0.1:5009,127.0.0.1:5010"));
		listener.onServiceHostChange("com.dianping.pigeon.demo.EchoService6",
				Utils.getServiceIpPortList("127.0.0.1:5008,127.0.0.1:5009,127.0.0.1:5010"));
		listener.onServiceHostChange("com.dianping.pigeon.demo.EchoService7",
				Utils.getServiceIpPortList("127.0.0.1:5008,127.0.0.1:5009,127.0.0.1:5010"));

		ExecutorService executor = Executors.newFixedThreadPool(20);
		for (int i = 0; i < 50; i++) {
			executor.submit(new Runnable() {

				@Override
				public void run() {
					while (true) {
						int num1 = r.nextInt(8);
						int num2 = r.nextInt(8);
						try {
							if (num2 >= 4) {
								listener.onServiceHostChange(
										"com.dianping.pigeon.demo.EchoService" + num1,
										Utils.getServiceIpPortList("127.0.0.1:5008,127.0.0.1:5009,127.0.0.1:5010,127.0.0.1:5011"));
							} else {
								listener.onServiceHostChange(
										"com.dianping.pigeon.demo.EchoService" + num1,
										Utils.getServiceIpPortList("127.0.0.1:5008,127.0.0.1:5010"));
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			});
		}
		System.in.read();
	}
}