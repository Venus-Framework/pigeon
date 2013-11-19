/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.dpsf.spring;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.dianping.dpsf.exception.ServiceException;
import com.dianping.pigeon.monitor.LoggerLoader;
import com.dianping.pigeon.remoting.common.exception.RpcException;
import com.dianping.pigeon.remoting.common.service.ServiceFactory;
import com.dianping.pigeon.remoting.provider.ServerFactory;
import com.dianping.pigeon.remoting.provider.component.ProviderConfig;

/**
 * @deprecated 后续请使用spring的xml配置方式。务必！！新功能不再支持该方式了。
 *             pigeon的服务发布的spring配置入口。example: <bean
 *             class="com.dianping.dpsf.spring.ServiceRegistry"
 *             init-method="init"> <property name="port" value="6666" />
 *             <property name="services"> <map> <entry
 *             key="http://service.dianping.com/testService/echoService_1.0.0"
 *             value-ref="echoServiceImpl" /> </map> </property> </bean> <bean
 *             id="echoServiceImpl"
 *             class="com.dianping.pigeon.test.EchoServiceImpl"/>
 * @author jianhuihuang
 * @version $Id: ServiceRegistry.java, v 0.1 2013-6-17 下午6:11:08 jianhuihuang
 *          Exp $
 */
public final class ServiceRegistry {

	private static final Logger logger = LoggerLoader.getLogger(ServiceRegistry.class);

	private boolean publish = true;

	private Map<String, Object> services;

	private int port = ServerFactory.DEFAULT_PORT;

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	/**
	 * 要确保只是启动一次！，调用Pigeon启动器，通过事件的机制来并行初始化，确保快速的启动。
	 * 
	 * @throws ServiceException
	 * 
	 * @throws ClassNotFoundException
	 */
	public void init() throws ServiceException {
		List<ProviderConfig<?>> providerConfigList = new ArrayList<ProviderConfig<?>>();
		for (String url : services.keySet()) {
			ProviderConfig providerConfig = new ProviderConfig(services.get(url));
			providerConfig.setUrl(url);
			providerConfig.setPort(port);
			providerConfigList.add(providerConfig);
		}
		try {
			ServiceFactory.publishServices(providerConfigList, port);
		} catch (RpcException e) {
			throw new ServiceException("", e);
		}
	}

	/**
	 * @return the publish
	 */
	public boolean isPublish() {
		return publish;
	}

	public void setPublish(boolean publish) {
		this.publish = publish;
	}

	public Map<String, Object> getServices() {
		return services;
	}

	public void setServices(Map<String, Object> services) {
		this.services = services;
	}

}
