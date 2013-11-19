/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.test.client_1.x;

import java.lang.reflect.Field;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.codehaus.plexus.util.StringUtils;
import org.junit.After;
import org.junit.Before;

import com.dianping.dpsf.async.ServiceCallback;
import com.dianping.dpsf.spring.ProxyBeanFactory;
import com.dianping.lion.EnvZooKeeperConfig;
import com.dianping.lion.client.ConfigCache;
import com.dianping.pigeon.test.client_1.x.loader.ConfigLoader;
import com.dianping.pigeon.test.client_1.x.loader.SpringLoader;

public class BaseInvokerTest {

	private static final Logger logger = Logger.getLogger(BaseInvokerTest.class);

	protected ServiceCallback callback = null;

	protected String registryAddress = null;

	public String getSpringPath() {
		return null;
	}

	@Before
	public void start() throws Exception {
		Properties properties = ConfigLoader.getLocalProperties();
		registryAddress = properties.getProperty("pigeon.registry.address");
		System.out.println("registry address:" + registryAddress);
		if (StringUtils.isBlank(registryAddress)) {
			registryAddress = EnvZooKeeperConfig.getZKAddress();
		}
		//registryAddress = "127.0.0.1:2181";
		System.out.println("registry address:" + registryAddress);
		ConfigCache.getInstance(registryAddress);
		initClient();
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
					callback = (ServiceCallback) Class.forName(test.callback()).newInstance();
					factory.setCallback(callback);
				}
				factory.init();
				field.set(this, factory.getObject());
			}
		}
	}
}
