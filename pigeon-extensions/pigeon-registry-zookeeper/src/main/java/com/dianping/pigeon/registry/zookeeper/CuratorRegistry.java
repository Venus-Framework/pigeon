package com.dianping.pigeon.registry.zookeeper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.zookeeper.KeeperException.NoNodeException;

import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.extension.ExtensionLoader;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.registry.Registry;
import com.dianping.pigeon.registry.RegistryManager;
import com.dianping.pigeon.registry.exception.RegistryException;
import com.dianping.pigeon.registry.listener.ServiceSiblingChangeListener;
import com.dianping.pigeon.registry.util.Constants;
import com.dianping.pigeon.util.CollectionUtils;

public class CuratorRegistry implements Registry {

	private static Logger logger = LoggerLoader.getLogger(CuratorRegistry.class);

	private ConfigManager configManager = ExtensionLoader.getExtension(ConfigManager.class);

	private CuratorClient client;

	private Properties properties;

	private volatile boolean inited = false;

	private ConcurrentHashMap<String, Object> registeredServices = new ConcurrentHashMap<String, Object>();

	private final int expirationTime = configManager.getIntValue("pigeon.registry.ephemeralnode.expirationtime", 5000);

	private final boolean enableSiblingChange = configManager.getBooleanValue("pigeon.registry.siblingchange.enable",
			false);

	private final boolean delEmptyNode = configManager.getBooleanValue("pigeon.registry.siblingchange.delemptynode",
			true);

	private ConcurrentHashMap<String, Object> referencedServices = new ConcurrentHashMap<String, Object>();

	private ServiceSiblingChangeListener serviceSiblingChangeListener;

	@Override
	public void init(Properties properties) {
		this.properties = properties;
		if (!inited) {
			synchronized (this) {
				if (!inited) {
					try {
						serviceSiblingChangeListener = new ServiceSiblingChangeListener() {

							private String localIp = configManager.getLocalIp();
							private String group = configManager.getGroup();

							@Override
							public void siblingAdded(String serviceName, String host) {
								// do nothing
							}

							@Override
							public void siblingRemoved(String serviceName, String host) {
								if (enableSiblingChange && registeredServices.containsKey(serviceName)) {
									try {
										List<String> hostList = getEphemeralServiceAddress(serviceName, group);
										if (!CollectionUtils.isEmpty(hostList)) {
											Collections.sort(hostList);
											if (hostList.get(0).startsWith(localIp)) {
												for (String h : hostList) {
													if (h.equals(host)) {
														// unregisterEphemeralNode(serviceName,
														// group, h);
														logger.info("existing sibling node:" + host);
														return;
													}
												}
												unregisterPersistentNode(serviceName, group, host);
											}
										}
									} catch (Exception e) {
										logger.error("", e);
									}
								}
							}

							@Override
							public void siblingWeightChanged(String serviceName, String host) {
								// do nothing
							}

						};
						String zkAddress = properties.getProperty(Constants.KEY_REGISTRY_ADDRESS);
						logger.info("start to initialize zookeeper client:" + zkAddress);
						client = new CuratorClient(zkAddress, this);
						logger.info("succeed to initialize zookeeper client:" + zkAddress);
					} catch (Exception ex) {
						logger.error("failed to initialize zookeeper client", ex);
					}
					inited = true;
				}
			}
		}
	}

	@Override
	public String getName() {
		return "curator";
	}

	@Override
	public String getValue(String key) {
		return properties.getProperty(key);
	}

	@Override
	public String getServiceAddress(String serviceName) throws RegistryException {
		return getServiceAddress(serviceName, Constants.DEFAULT_GROUP);
	}

	@Override
	public String getServiceAddress(String serviceName, String group) throws RegistryException {
		String addr = getServiceActualAddress(serviceName, group);
		addReferencedService(serviceName, group);
		return addr;
	}

	public String getServiceActualAddress(String serviceName, String group) throws RegistryException {
		try {
			String oldSrvAddr = getOldServiceAddress(serviceName, group);
			return oldSrvAddr;
		} catch (Exception e) {
			logger.error("failed to get service address for " + serviceName + "/" + group, e);
			throw new RegistryException(e);
		}
	}

	private void addReferencedService(String serviceName, String group) {
		if (StringUtils.isBlank(group)) {
			group = Constants.DEFAULT_GROUP;
		}
		referencedServices.put(serviceName, group);
	}

	public Set<String> getReferencedServices() {
		return referencedServices.keySet();
	}

	public boolean isReferencedService(String serviceName, String group) {
		// String g = (String) referencedServices.get(serviceName);
		// if (StringUtils.isBlank(group)) {
		// group = Constants.DEFAULT_GROUP;
		// }
		return referencedServices.containsKey(serviceName);
	}

	public Set<String> getRegisteredServices() {
		return registeredServices.keySet();
	}

	private List<String> getEphemeralServiceAddress(String serviceName, String group) throws Exception {
		String path = Utils.getEphemeralServicePath(serviceName, group);
		List<String> serverList = client.getChildren(path);
		return serverList;
	}

	private String mergeServiceAddress(String oldSrvAddr, List<String> newSrvAddr) {
		List<String> mergedList = (newSrvAddr == null ? new ArrayList<String>() : newSrvAddr);
		if (oldSrvAddr != null) {
			String[] addrList = oldSrvAddr.split(",");
			for (String addr : addrList) {
				addr = addr.trim();
				if (addr.length() > 0 && !mergedList.contains(addr)) {
					mergedList.add(addr);
				}
			}
		}
		return StringUtils.join(mergedList.iterator(), ',');
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

	String getOldServiceAddress(String serviceName, String group) throws Exception {
		String path = Utils.getServicePath(serviceName, group);
		String address = client.get(path);
		if (!StringUtils.isBlank(group)) {
			boolean fallback2DefaultGroup = false;
			if (StringUtils.isBlank(address)) {
				fallback2DefaultGroup = true;
			} else {
				String[] addressArray = address.split(",");
				int weightCount = 0;
				for (String addr : addressArray) {
					addr = addr.trim();
					if (addr.length() > 0) {
						int weight = RegistryManager.getInstance().getServiceWeight(addr);
						if (weight > 0) {
							weightCount += weight;
						}
					}
				}
				if (weightCount == 0) {
					fallback2DefaultGroup = true;
					logger.info("weight is 0 with address:" + address);
				}
			}
			if (fallback2DefaultGroup) {
				logger.info("node " + path + " does not exist, fallback to default group");
				path = Utils.getServicePath(serviceName, Constants.DEFAULT_GROUP);
				address = client.get(path);
			}
		}
		return address;
	}

	@Override
	public void registerService(String serviceName, String group, String serviceAddress, int weight)
			throws RegistryException {
		registerPersistentNode(serviceName, group, serviceAddress, weight);
		watchSelf(serviceName, group, serviceAddress);
	}

	void watchSelf(String serviceName, String group, String serviceAddress) throws RegistryException {
		registeredServices.putIfAbsent(serviceName, serviceAddress);
		String parentPath = Utils.getEphemeralServicePath(serviceName, group);
		try {
			client.watchChildren(parentPath);
		} catch (Exception e) {
			logger.error("failed to watch " + parentPath, e);
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

	@Override
	public void unregisterService(String serviceName, String serviceAddress) throws RegistryException {
		unregisterService(serviceName, Constants.DEFAULT_GROUP, serviceAddress);
	}

	@Override
	public void unregisterService(String serviceName, String group, String serviceAddress) throws RegistryException {
		unregisterPersistentNode(serviceName, group, serviceAddress);
		registeredServices.remove(serviceName);
	}

	public void unregisterPersistentNode(String serviceName, String group, String serviceAddress)
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
							if (delEmptyNode) {
								try {
									client.delete(servicePath);
								} catch (NoNodeException e) {
									logger.warn("Already deleted path:" + servicePath + ":" + e.getMessage());
								}
							} else {
								client.set(servicePath, "");
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

	@Override
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

	@Override
	public void setServerWeight(String serverAddress, int weight) throws RegistryException {
		String path = Utils.getWeightPath(serverAddress);
		try {
			client.set(path, weight);
		} catch (Throwable e) {
			logger.error("failed to set weight of " + serverAddress + " to " + weight);
			throw new RegistryException(e);
		}
	}

	@Override
	public List<String> getChildren(String path) throws RegistryException {
		// FIXME where is this used?
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

	public ServiceSiblingChangeListener getServiceSiblingChangeListener() {
		return serviceSiblingChangeListener;
	}
}
