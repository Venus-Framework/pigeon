/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.provider.config.spring;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import com.dianping.pigeon.log.LoggerLoader;
import org.apache.logging.log4j.Logger;

import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.extension.ExtensionLoader;
import com.dianping.pigeon.remoting.ServiceFactory;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.provider.config.ProviderConfig;
import com.dianping.pigeon.remoting.provider.config.ProviderMethodConfig;
import com.dianping.pigeon.util.ClassUtils;
import com.dianping.pigeon.util.CollectionUtils;

public class ServiceBean {

	private static final Logger logger = LoggerLoader.getLogger(ServiceBean.class);

	private String url;
	private Object serviceImpl;
	private String version;
	private String interfaceName;
	private ServerBean serverBean;
	private boolean cancelTimeout = Constants.DEFAULT_TIMEOUT_CANCEL;
	private ConfigManager configManager = ExtensionLoader.getExtension(ConfigManager.class);
	private boolean useSharedPool = configManager.getBooleanValue(Constants.KEY_SERVICE_SHARED,
			Constants.DEFAULT_SERVICE_SHARED);
	private List<ProviderMethodConfig> methods;
	private ClassLoader classLoader;
	private int actives;

	public int getActives() {
		return actives;
	}

	public void setActives(int actives) {
		this.actives = actives;
	}

	public ClassLoader getClassLoader() {
		return classLoader;
	}

	public void setClassLoader(ClassLoader classLoader) {
		this.classLoader = classLoader;
	}

	public List<ProviderMethodConfig> getMethods() {
		return methods;
	}

	public void setMethods(List<ProviderMethodConfig> methods) {
		this.methods = methods;
	}

	public boolean isUseSharedPool() {
		return useSharedPool;
	}

	public void setUseSharedPool(boolean useSharedPool) {
		this.useSharedPool = useSharedPool;
	}

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
		ProviderConfig<?> providerConfig = null;
		if (StringUtils.isBlank(interfaceName)) {
			providerConfig = new ProviderConfig<Object>(serviceImpl);
		} else {
			Class<?> cl = ClassUtils.loadClass(interfaceName);
			providerConfig = new ProviderConfig(cl, serviceImpl);
		}
		providerConfig.setVersion(version);
		providerConfig.setUrl(url);
		providerConfig.setCancelTimeout(cancelTimeout);
		providerConfig.setSharedPool(useSharedPool);
		if (!CollectionUtils.isEmpty(methods)) {
			Map<String, ProviderMethodConfig> methodMap = new HashMap<String, ProviderMethodConfig>();
			providerConfig.setMethods(methodMap);
			for (ProviderMethodConfig method : methods) {
				methodMap.put(method.getName(), method);
			}
		}
		providerConfig.setActives(actives);
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
