/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.test.client_1.x;

import java.lang.reflect.Field;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;

import com.dianping.dpsf.async.ServiceCallback;
import com.dianping.dpsf.spring.ProxyBeanFactory;
import com.dianping.lion.client.ConfigCache;
import com.dianping.pigeon.test.loader.SpringLoader;

public class AnnotationBaseInvokerTest {

	private static final Logger logger = Logger
			.getLogger(AnnotationBaseInvokerTest.class);

	protected ServiceCallback callback = null;

	public String getSpringPath() {
		return null;
	}

	@Before
	public void start() throws Exception {
		ConfigCache.getInstance("127.0.0.1:2181");
		try {
			initClient();
		} catch (Exception e) {
			logger.error("", e);
		}
	}

	@After
	public void stop() throws Exception {
		SpringLoader.stopProvider(4625);
	}

	private void initClient() throws Exception {
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

				factory.setServiceName(serviceName);
				factory.setIface(field.getType().getName());
				factory.setSerialize(test.serialize());
				factory.setCallMethod(test.callMethod());
				factory.setTimeout(test.timeout());

				if (!test.callback().equals("null")) {
					callback = (ServiceCallback) Class.forName(test.callback())
							.newInstance();
					factory.setCallback(callback);
				}
				factory.init();
				field.set(this, factory.getObject());
			}
		}
	}
}
