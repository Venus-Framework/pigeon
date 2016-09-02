package com.dianping.pigeon.registry;

import java.util.List;
import java.util.Properties;

import com.dianping.pigeon.registry.exception.RegistryException;

public interface Registry {

	void init(Properties properties);

	String getName();

	String getValue(String key);

	// for invoker
	String getServiceAddress(String serviceName) throws RegistryException;

	// for invoker
	String getServiceAddress(String serviceName, String group) throws RegistryException;

	// for invoker
	String getServiceAddress(String serviceName, String group, boolean fallbackDefaultGroup) throws RegistryException;

	// for invoker
	String getServiceAddress(String remoteAppkey, String serviceName, String group,
							 boolean fallbackDefaultGroup) throws RegistryException;

	// for provider
	void registerService(String serviceName, String group, String serviceAddress, int weight) throws RegistryException;

	// for provider
	void unregisterService(String serviceName, String serviceAddress) throws RegistryException;

	// for provider
	void unregisterService(String serviceName, String group, String serviceAddress) throws RegistryException;

	// for invoker
	int getServerWeight(String serverAddress) throws RegistryException;

	// for provider
	void setServerWeight(String serverAddress, int weight) throws RegistryException;

	// for invoker
	String getServerApp(String serverAddress) throws RegistryException;

	// for provider
	void setServerApp(String serverAddress, String app);

	// for provider
	void unregisterServerApp(String serverAddress);

	// for invoker
	String getServerVersion(String serverAddress) throws RegistryException;

	// for provider
	void setServerVersion(String serverAddress, String version);

	// for provider
	void unregisterServerVersion(String serverAddress);

	// for invoker
	byte getServerHeartBeatSupport(String serviceAddress) throws RegistryException;

	// for invoker
	boolean isSupportNewProtocol(String serviceAddress) throws RegistryException;

	// for invoker
	boolean isSupportNewProtocol(String serviceAddress, String serviceName) throws RegistryException;

	// for provider
	void setSupportNewProtocol(String serviceAddress, String serviceName, boolean support) throws RegistryException;

	// for provider
	void unregisterSupportNewProtocol(String serviceAddress, String serviceName, boolean support) throws RegistryException;

	String getStatistics();

	List<String> getChildren(String key) throws RegistryException;

	// for provider
	void updateHeartBeat(String serviceAddress, Long heartBeatTimeMillis);

	// for provider
	void deleteHeartBeat(String serviceAddress);

	// for governor
	void setServerService(String serviceName, String group, String hosts) throws RegistryException;

	// for governor
	void delServerService(String serviceName, String group) throws RegistryException;

	// for governor
	void setHostsWeight(String serviceName, String group, String hosts, int weight) throws RegistryException;

	// for governor
	String getServiceAddress(String remoteAppkey, String serviceName, String group,
							 boolean fallbackDefaultGroup, boolean needListener) throws RegistryException;

	// for governor
	String getServiceAddress(String serviceName, String group,
							 boolean fallbackDefaultGroup, boolean needListener) throws RegistryException;
}
