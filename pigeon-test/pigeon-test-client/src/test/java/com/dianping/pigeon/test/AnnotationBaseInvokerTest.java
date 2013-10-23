/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.test;

import java.lang.reflect.Field;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;

import com.dianping.dpsf.spring.ProxyBeanFactory;
import com.dianping.pigeon.component.QueryString;
import com.dianping.pigeon.registry.cache.RegistryCache;
import com.dianping.pigeon.remoting.invoker.component.async.ServiceCallback;
import com.dianping.pigeon.remoting.provider.ServerFactory;
import com.dianping.pigeon.test.loader.SpringLoader;

public class AnnotationBaseInvokerTest {

	private static final Logger logger = Logger.getLogger(AnnotationBaseInvokerTest.class);

	protected ServiceCallback callback = null;

	public String getSpringPath() {
		return null;
	}

	@Before
	public void start() throws Exception {
		//SpringLoader.startupProvider(ServerFactory.DEFAULT_PORT);

		//ConfigLoader.initClient();
		try {
			initClient();
		} catch (Exception e) {
			logger.error("", e);
		}
	}

	@After
	public void stop() throws Exception {
		SpringLoader.stopProvider(ServerFactory.DEFAULT_PORT);
	}

	private void initClient() throws IllegalArgumentException, IllegalAccessException, InstantiationException,
			ClassNotFoundException {
		for (Field field : getClass().getFields()) {
			if (field.isAnnotationPresent(PigeonAutoTest.class)) {
				ProxyBeanFactory factory = new ProxyBeanFactory();
				PigeonAutoTest test = field.getAnnotation(PigeonAutoTest.class);

				String serviceName = null;
				if (test.serviceName().isEmpty()) {
					serviceName = field.getType().getName();

				} else {
					serviceName = test.serviceName();
				}

				if (test.group().isEmpty()) {
					factory.setServiceName(serviceName);
				} else {
					factory.setServiceName(serviceName + QueryString.PREFIX + "group=" + test.group());
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
				factory.setVip(test.vip());
				factory.setTestVip(test.testVip());
				RegistryCache.update("env", test.env());
				if (!test.callback().equals("null")) {
					callback = (ServiceCallback) Class.forName(test.callback()).newInstance();
					factory.setCallback(callback);
				}
				factory.init();
				field.set(this, factory.getObject());
			}
		}
	}
}
