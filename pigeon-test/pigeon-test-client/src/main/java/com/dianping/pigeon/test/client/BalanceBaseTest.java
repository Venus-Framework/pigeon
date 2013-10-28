package com.dianping.pigeon.test.client;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.apache.log4j.Logger;

import com.dianping.dpsf.spring.ProxyBeanFactory;
import com.dianping.pigeon.remoting.invoker.component.async.ServiceCallback;
import com.dianping.pigeon.test.loader.SpringLoader;

public class BalanceBaseTest {

	private static final Logger logger = Logger.getLogger(BalanceBaseTest.class);

	protected static final int CLIENT_NUM = 10;
	protected CountDownLatch clientLatch = new CountDownLatch(CLIENT_NUM);
	protected CountDownLatch serverLatch = new CountDownLatch(1);

	public List<Integer> getPorts() {
		return new ArrayList<Integer>();

	}

	public void start() {
		new Thread() {
			public void run() {
				List<Integer> ports = getPorts();
				for (int port : ports) {
					SpringLoader.startupProvider(port);
				}
				try {
					serverLatch.countDown();
					logger.info("#######Server parepres done!!!!!!!!!");
				} catch (Exception e) {
					logger.error("", e);
				}
			}
		}.start();
	}

	protected void initClient() throws IllegalArgumentException, IllegalAccessException, InstantiationException,
			ClassNotFoundException {
		for (Field field : getClass().getFields()) {
			if (field.isAnnotationPresent(PigeonAutoTest.class)) {
				ProxyBeanFactory factory = new ProxyBeanFactory();
				PigeonAutoTest test = field.getAnnotation(PigeonAutoTest.class);
				factory.setServiceName(test.serviceName());
				factory.setIface(field.getType().getName());
				factory.setSerialize(test.serialize());
				factory.setCallMethod(test.callMethod());
				factory.setTimeout(test.timeout());
				factory.setCluster(test.cluster());
				factory.setTimeoutRetry(test.timeoutRetry());
				factory.setRetries(test.retries());
				factory.setLoadbalance(test.loadbalance());

				if (!test.callback().equals("null")) {
					factory.setCallback((ServiceCallback) Class.forName(test.callback()).newInstance());
				}
				try {
					factory.init();
				} catch (ClassNotFoundException e) {
					logger.error("", e);
				}
				field.set(this, factory.getObject());
			}
		}
	}
}
