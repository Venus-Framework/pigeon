/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.provider.config.spring;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.log4j.Logger;

import com.dianping.dpsf.exception.ServiceException;
import com.dianping.pigeon.monitor.LoggerLoader;
import com.dianping.pigeon.remoting.ServiceFactory;
import com.dianping.pigeon.remoting.provider.config.ProviderConfig;

public class ServiceBean {

	private static final Logger logger = LoggerLoader.getLogger(ServiceBean.class);

	private String url;
	private Object serviceImpl;
	private String version;
	private String interfaceName;

	public String getInterfaceName() {
		return interfaceName;
	}

	public void setInterfaceName(String interfaceName) {
		this.interfaceName = interfaceName;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Object getServiceImpl() {
		return serviceImpl;
	}

	public void setServiceImpl(Object serviceImpl) {
		this.serviceImpl = serviceImpl;
	}

	/**
	 * 要确保只是启动一次！，调用Pigeon启动器，通过事件的机制来并行初始化，确保快速的启动。
	 * 
	 * @throws ServiceException
	 */
	public void init() throws Exception {
		if (serviceImpl == null) {
			throw new IllegalArgumentException("service not found:" + this);
		}
		ProviderConfig providerConfig = null;
		if (StringUtils.isBlank(interfaceName)) {
			providerConfig = new ProviderConfig(serviceImpl);
		} else {
			providerConfig = new ProviderConfig(Class.forName(interfaceName), serviceImpl);
		}
		providerConfig.setVersion(version);
		providerConfig.setUrl(url);
		ServiceFactory.publishService(providerConfig);
	}

	@Override
	public String toString() {
		return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
