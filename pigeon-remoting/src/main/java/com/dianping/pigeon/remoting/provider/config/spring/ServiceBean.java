/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.provider.config.spring;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.log4j.Logger;

import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.remoting.ServiceFactory;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.provider.config.ProviderConfig;
import com.dianping.pigeon.remoting.provider.listener.ServiceWarmupListener;

public class ServiceBean {

	private static final Logger logger = LoggerLoader.getLogger(ServiceBean.class);

	private String url;
	private Object serviceImpl;
	private String version;
	private String interfaceName;
	private ServerBean serverBean;
	private boolean cancelTimeout = Constants.DEFAULT_TIMEOUT_CANCEL;

	public boolean isCancelTimeout() {
		return cancelTimeout;
	}

	public void setCancelTimeout(boolean cancelTimeout) {
		this.cancelTimeout = cancelTimeout;
	}

	public ServerBean getServerBean() {
		return serverBean;
	}

	public void setServerBean(ServerBean serverBean) {
		this.serverBean = serverBean;
	}

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
		providerConfig.setCancelTimeout(cancelTimeout);
		if (serverBean != null) {
			providerConfig.setServerConfig(serverBean.init());
		}
		ServiceFactory.addService(providerConfig);
	}

	@Override
	public String toString() {
		return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
