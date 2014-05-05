package com.dianping.pigeon.registry;

import java.util.List;
import java.util.Properties;

import com.dianping.pigeon.registry.exception.RegistryException;

public interface Registry {

	void init(Properties properties);

	String getName();

	String getValue(String key);
	
	String getServiceAddress(String serviceName) throws RegistryException;
	
	String getServiceAddress(String serviceName, String group) throws RegistryException;

	void registerService(String serviceName, String serviceAddress) throws RegistryException;

	void registerService(String serviceName, String group, String serviceAddress, int weight) throws RegistryException;

	void unregisterService(String serviceName, String serviceAddress) throws RegistryException;

	void unregisterService(String serviceName, String group, String serviceAddress) throws RegistryException;

	int getServiceWeigth(String serviceAddress) throws RegistryException;

	Properties getRegistryMeta(String serviceAddress) throws RegistryException;
	
	List<String> getChildren(String key) throws RegistryException;
	
	public void setServerWeight(String serverAddress, int weight) throws RegistryException;
}
