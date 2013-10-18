/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;

import com.dianping.dpsf.spring.ProxyBeanFactory;
import com.dianping.pigeon.component.QueryString;
import com.dianping.pigeon.remoting.invoker.component.async.ServiceCallback;
import com.dianping.pigeon.remoting.provider.ServerFactory;
import com.dianping.pigeon.test.loader.ConfigLoader;
import com.dianping.pigeon.test.loader.SpringLoader;

public class MultiServerBaseTest {

	private static final Logger logger = Logger.getLogger(MultiServerBaseTest.class);

	public List<Integer> getPorts() {
		return new ArrayList<Integer>();
	}

	@Before
	public void setUp() throws Exception {
		List<Integer> ports = getPorts();
		if (ports.isEmpty()) {
			ports.add(ServerFactory.DEFAULT_PORT);
		}
		for (int port : ports) {
			SpringLoader.startupProvider(port);
		}

		ConfigLoader.initClient();
		try {
			initClient();
		} catch (Exception e) {
			logger.error("", e);
		}
	}

	@After
	public void tearDown() throws Exception {
		List<Integer> ports = getPorts();
		if (ports.isEmpty()) {
			ports.add(ServerFactory.DEFAULT_PORT);
		}
		for (int port : ports) {
			SpringLoader.stopProvider(port);
		}
	}

	private void initClient() throws IllegalArgumentException, IllegalAccessException, InstantiationException,
			ClassNotFoundException {
		for (Field field : getClass().getFields()) {
			if (field.isAnnotationPresent(PigeonAutoTest.class)) {
				ProxyBeanFactory factory = new ProxyBeanFactory();
				PigeonAutoTest test = field.getAnnotation(PigeonAutoTest.class);
				if (test.group().isEmpty()) {
					factory.setServiceName(test.serviceName());
				} else {
					factory.setServiceName(test.serviceName() + QueryString.PREFIX + "group=" + test.group());
				}
				if (!test.zone().isEmpty()) {
					factory.setZone(test.zone());
				}
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
