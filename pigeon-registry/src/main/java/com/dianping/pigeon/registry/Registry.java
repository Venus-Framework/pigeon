package com.dianping.pigeon.registry;

import java.util.Properties;

import com.dianping.pigeon.registry.exception.RegistryException;
import com.dianping.pigeon.registry.listener.ConfigChangeListener;

public interface Registry {

	void init(Properties properties);

	String getName();

	String getValue(String key) throws RegistryException;

	void addConfigChangeListener(ConfigChangeListener configChangeListener);

	String getServiceAddress(String serviceName) throws RegistryException;

	void publishServiceAddress(String serviceName, String serviceAddress) throws RegistryException;

	Integer getServiceWeigth(String serviceAddress) throws RegistryException;

}
