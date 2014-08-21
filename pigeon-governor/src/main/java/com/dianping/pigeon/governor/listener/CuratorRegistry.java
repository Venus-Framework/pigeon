package com.dianping.pigeon.governor.listener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.zookeeper.KeeperException.NoNodeException;

import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.extension.ExtensionLoader;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.registry.exception.RegistryException;
import com.dianping.pigeon.registry.util.Constants;
import com.dianping.pigeon.registry.zookeeper.Utils;
import com.dianping.pigeon.util.CollectionUtils;

public class CuratorRegistry {

	private static Logger logger = LoggerLoader.getLogger(CuratorRegistry.class);

	private ConfigManager configManager = ExtensionLoader.getExtension(ConfigManager.class);

	private CuratorClient client;

	private boolean inited = false;

	private final int expirationTime = configManager.getIntValue("pigeon.registry.ephemeralnode.expirationtime", 5000);

	private ServiceOfflineListener serviceOfflineListener;

	private Map<String, List<String>> ephemeralAddresses = new HashMap<String, List<String>>();

	private Set<String> services = new HashSet<String>();

	public Map<String, List<String>> getEphemeralAddresses() {
		return ephemeralAddresses;
	}

	public Set<String> getServices() {
		return services;
	}

	public void init(String address) {
		if (!inited) {
			try {
				serviceOfflineListener = new ServiceOfflineListener() {

					@Override
					public void offline(String serviceName, String host, String group) {
						try {
							List<String> hostList = getNewServiceAddress(serviceName, group);
							Collections.sort(hostList);
							for (String h : hostList) {
								if (h.equals(host)) {
									unregisterEphemeralNode(serviceName, group, h);
									return;
								}
							}
							unregisterPersistentNode(serviceName, group, host);
						} catch (Exception e) {
							logger.error("", e);
						}
					}

				};
				logger.info("start to initialize zookeeper client:" + address);
				client = new CuratorClient(address, this);
			} catch (Exception ex) {
				logger.error("failed to initialize zookeeper client", ex);
			}
			inited = true;
		}
	}

	public String getName() {
		return "curator";
	}

	List<String> getNewServiceAddress(String serviceName, String group) throws Exception {
		String path = Utils.getEphemeralServicePath(serviceName, group);
		List<String> serverList = client.getChildren(path);
		if (!StringUtils.isBlank(group) && CollectionUtils.isEmpty(serverList)) {
			logger.info("node " + path + " does not exist or has no child, fallback to default group");
			path = Utils.getEphemeralServicePath(serviceName, Constants.DEFAULT_GROUP);
			serverList = client.getChildren(path);
		}
		return serverList;
	}

	void watchSelf(String serviceName, String group, String serviceAddress) throws RegistryException {
		String parentPath = Utils.getEphemeralServicePath(serviceName, group);
		try {
			client.watchChildren(parentPath);
		} catch (Exception e) {
			logger.error("failed to watch " + parentPath, e);
			throw new RegistryException(e);
		}
	}

	void registerEphemeralNode(String serviceName, String group, String serviceAddress, int weight)
			throws RegistryException {
		String weightPath = Utils.getWeightPath(serviceAddress);
		String servicePath = Utils.getEphemeralServicePath(serviceName, group, serviceAddress);
		try {
			if (weight >= 0) {
				client.set(weightPath, weight);
			}
			boolean exists = client.exists(servicePath);
			if (exists) {
				if (logger.isInfoEnabled()) {
					logger.info("delete existing ephemeral node: " + servicePath);
				}
				client.delete(servicePath);
				Thread.sleep(expirationTime);
			}
			client.createEphemeral(servicePath);
			if (logger.isInfoEnabled()) {
				logger.info("registered service to ephemeral node: " + servicePath);
			}
		} catch (Exception e) {
			logger.error("failed to register service to " + servicePath, e);
			throw new RegistryException(e);
		}
	}

	void registerPersistentNode(String serviceName, String group, String serviceAddress, int weight)
			throws RegistryException {
		String servicePath = Utils.getServicePath(serviceName, group);
		try {
			if (client.exists(servicePath)) {
				String addressValue = client.get(servicePath);
				String[] addressArray = addressValue.split(",");
				List<String> addressList = new ArrayList<String>();
				for (String addr : addressArray) {
					addr = addr.trim();
					if (addr.length() > 0 && !addressList.contains(addr)) {
						addressList.add(addr.trim());
					}
				}
				if (!addressList.contains(serviceAddress)) {
					addressList.add(serviceAddress);
					Collections.sort(addressList);
					client.set(servicePath, StringUtils.join(addressList.iterator(), ","));
				}
			} else {
				client.create(servicePath, serviceAddress);
			}
			if (logger.isInfoEnabled()) {
				logger.info("registered service to persistent node: " + servicePath);
			}
		} catch (Throwable e) {
			logger.error("failed to register service to " + servicePath, e);
			throw new RegistryException(e);
		}
	}

	public void unregisterService(String serviceName, String serviceAddress) throws RegistryException {
		unregisterService(serviceName, Constants.DEFAULT_GROUP, serviceAddress);
	}

	public void unregisterService(String serviceName, String group, String serviceAddress) throws RegistryException {
		unregisterEphemeralNode(serviceName, group, serviceAddress);
		unregisterPersistentNode(serviceName, group, serviceAddress);
	}

	void unregisterEphemeralNode(String serviceName, String group, String serviceAddress) throws RegistryException {
		String path = Utils.getEphemeralServicePath(serviceName, group, serviceAddress);
		try {
			boolean exists = client.exists(path);
			if (exists) {
				client.delete(path);
			}
			// remove parent node if no child is there
			String parentPath = Utils.getEphemeralServicePath(serviceName, group);
			List<String> children = client.getChildren(parentPath);
			if (CollectionUtils.isEmpty(children)) {
				client.delete(parentPath);
			}
			if (logger.isInfoEnabled()) {
				logger.info("unregistered service from " + path);
			}
		} catch (Throwable e) {
			logger.error("failed to unregister service from " + path);
			throw new RegistryException(e);
		}
	}

	void unregisterPersistentNode(String serviceName, String group, String serviceAddress) throws RegistryException {
		String servicePath = Utils.getServicePath(serviceName, group);
		try {
			if (client.exists(servicePath)) {
				String addressValue = client.get(servicePath);
				String[] addressArray = addressValue.split(",");
				List<String> addressList = new ArrayList<String>();
				for (String addr : addressArray) {
					addr = addr.trim();
					if (addr.length() > 0 && !addressList.contains(addr)) {
						addressList.add(addr);
					}
				}
				if (addressList.contains(serviceAddress)) {
					addressList.remove(serviceAddress);
					if (!addressList.isEmpty()) {
						Collections.sort(addressList);
						client.set(servicePath, StringUtils.join(addressList.iterator(), ","));
					} else {
						List<String> children = client.getChildren(servicePath);
						if (CollectionUtils.isEmpty(children)) {
							try {
								client.delete(servicePath);
							} catch (NoNodeException e) {
								logger.warn("Already deleted path:" + servicePath + ":" + e.getMessage());
							}
						} else {
							logger.warn("Existing children [" + children + "] under path:" + servicePath);
							client.set(servicePath, "");
						}
					}
				}
				if (logger.isInfoEnabled()) {
					logger.info("unregistered service from " + servicePath);
				}
			}
		} catch (Throwable e) {
			logger.error("failed to unregister service from " + servicePath, e);
			throw new RegistryException(e);
		}
	}

	public int getServerWeight(String serverAddress) throws RegistryException {
		String path = Utils.getWeightPath(serverAddress);
		String strWeight;
		try {
			strWeight = client.get(path);
			int result = Constants.WEIGHT_DEFAULT;
			if (strWeight != null) {
				try {
					result = Integer.parseInt(strWeight);
				} catch (NumberFormatException e) {
					logger.warn("invalid weight for " + serverAddress + ": " + strWeight);
				}
			}
			return result;
		} catch (Throwable e) {
			logger.error("failed to get weight for " + serverAddress);
			throw new RegistryException(e);
		}
	}

	public List<String> getChildren(String path) throws RegistryException {
		try {
			List<String> children = client.getChildren(path);
			return children;
		} catch (Throwable e) {
			logger.error("failed to get children of node: " + path, e);
			throw new RegistryException(e);
		}
	}

	public void close() {
		client.close();
	}

	CuratorClient getCuratorClient() {
		return client;
	}

	public ServiceOfflineListener getServiceOfflineListener() {
		return serviceOfflineListener;
	}
}
