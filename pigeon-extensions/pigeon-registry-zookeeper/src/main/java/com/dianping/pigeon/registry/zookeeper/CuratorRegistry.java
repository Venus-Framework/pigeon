package com.dianping.pigeon.registry.zookeeper;

import java.util.*;

import com.dianping.pigeon.util.VersionUtils;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.Logger;
import org.apache.zookeeper.KeeperException.NodeExistsException;
import org.apache.zookeeper.KeeperException.BadVersionException;
import org.apache.zookeeper.KeeperException.NoNodeException;

import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.config.ConfigManagerLoader;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.registry.Registry;
import com.dianping.pigeon.registry.RegistryManager;
import com.dianping.pigeon.registry.exception.RegistryException;
import com.dianping.pigeon.registry.util.Constants;
import com.dianping.pigeon.util.CollectionUtils;
import org.apache.zookeeper.data.Stat;

public class CuratorRegistry implements Registry {

	private static Logger logger = LoggerLoader.getLogger(CuratorRegistry.class);

	private ConfigManager configManager = ConfigManagerLoader.getConfigManager();

	private CuratorClient client;

	private Properties properties;

	private volatile boolean inited = false;

	private final boolean delEmptyNode = configManager.getBooleanValue("pigeon.registry.delemptynode", true);

	@Override
	public void init(Properties properties) {
		this.properties = properties;
		if (!inited) {
			synchronized (this) {
				if (!inited) {
					try {
						String zkAddress = properties.getProperty(Constants.KEY_REGISTRY_ADDRESS);

						if(StringUtils.isBlank(zkAddress)) {
							zkAddress = configManager.getStringValue(Constants.KEY_REGISTRY_ADDRESS);
						}

						logger.info("start to initialize zookeeper client:" + zkAddress);
						client = new CuratorClient(zkAddress);
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
		return Constants.REGISTRY_CURATOR_NAME;
	}

	@Override
	public String getValue(String key) {
		return properties.getProperty(key);
	}

	@Override
	public String getServiceAddress(String serviceName) throws RegistryException {
		return getServiceAddress(serviceName, Constants.DEFAULT_GROUP);
	}

	public String getServiceAddress(String serviceName, String group) throws RegistryException {
		return getServiceAddress(serviceName, group, true);
	}

	public String getServiceAddress(String serviceName, String group, boolean fallbackDefaultGroup)
			throws RegistryException {
		try {
			String path = Utils.getServicePath(serviceName, group);
			String address = client.get(path);
			if (!StringUtils.isBlank(group)) {
				boolean needFallback = false;
				if (StringUtils.isBlank(address)) {
					needFallback = true;
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
						needFallback = true;
						logger.info("weight is 0 with address:" + address);
					}
				}
				if (fallbackDefaultGroup && needFallback) {
					logger.info("node " + path + " does not exist, fallback to default group");
					path = Utils.getServicePath(serviceName, Constants.DEFAULT_GROUP);
					address = client.get(path);
				}
			}
			return address;
		} catch (Exception e) {
			logger.error("failed to get service address for " + serviceName + "/" + group, e);
			throw new RegistryException(e);
		}
	}

	@Override
	public String getServiceAddress(String remoteAppkey, String serviceName, String group,
									boolean fallbackDefaultGroup) throws RegistryException {
		// mtthrift service, pigeon zk registry do nothing
		return "";
	}

	@Override
	public void registerService(String serviceName, String group, String serviceAddress, int weight)
			throws RegistryException {
		registerPersistentNode(serviceName, group, serviceAddress, weight);
	}

	void registerPersistentNode(String serviceName, String group, String serviceAddress, int weight)
			throws RegistryException {
		String weightPath = Utils.getWeightPath(serviceAddress);
		String servicePath = Utils.getServicePath(serviceName, group);
		try {
			if (weight >= 0) {
				client.set(weightPath, "" + weight);
			}
			if (client.exists(servicePath, false)) {
				Stat stat = new Stat();
				String addressValue = client.get(servicePath, stat);
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
					client.set(servicePath, StringUtils.join(addressList.iterator(), ","), stat.getVersion());
				}
			} else {
				client.create(servicePath, serviceAddress);
			}
			if (logger.isInfoEnabled()) {
				logger.info("registered service to persistent node: " + servicePath);
			}
		} catch (Throwable e) {
			if(e instanceof BadVersionException || e instanceof NodeExistsException) {
				try {
					Thread.sleep(500);
				} catch (InterruptedException ie) {
					//ignore
				}
				registerPersistentNode(serviceName, group, serviceAddress, weight);
			} else {
				logger.error("failed to register service to " + servicePath, e);
				throw new RegistryException(e);
			}

		}
	}

	@Override
	public void unregisterService(String serviceName, String serviceAddress) throws RegistryException {
		unregisterService(serviceName, Constants.DEFAULT_GROUP, serviceAddress);
	}

	@Override
	public void unregisterService(String serviceName, String group, String serviceAddress) throws RegistryException {
		unregisterPersistentNode(serviceName, group, serviceAddress);
	}

	public void unregisterPersistentNode(String serviceName, String group, String serviceAddress)
			throws RegistryException {
		String servicePath = Utils.getServicePath(serviceName, group);
		try {
			if (client.exists(servicePath, false)) {
				Stat stat = new Stat();
				String addressValue = client.get(servicePath, stat);
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
						client.set(servicePath, StringUtils.join(addressList.iterator(), ","), stat.getVersion());
					} else {
						List<String> children = client.getChildren(servicePath, false);
						if (CollectionUtils.isEmpty(children)) {
							if (delEmptyNode) {
								try {
									client.delete(servicePath);
								} catch (NoNodeException e) {
									logger.warn("Already deleted path:" + servicePath + ":" + e.getMessage());
								}
							} else {
								client.set(servicePath, "", stat.getVersion());
							}
						} else {
							logger.warn("Existing children [" + children + "] under path:" + servicePath);
							client.set(servicePath, "", stat.getVersion());
						}
					}
				}
				if (logger.isInfoEnabled()) {
					logger.info("unregistered service from " + servicePath);
				}
			}
		} catch (Throwable e) {
			if(e instanceof BadVersionException) {
				try {
					Thread.sleep(500);
				} catch (InterruptedException ie) {
					//ignore
				}
				unregisterPersistentNode(serviceName, group, serviceAddress);
			} else {
				logger.error("failed to unregister service from " + servicePath, e);
				throw new RegistryException(e);
			}
		}
	}

	@Override
	public int getServerWeight(String serverAddress) throws RegistryException {
		String path = Utils.getWeightPath(serverAddress);
		String strWeight;
		try {
			strWeight = client.get(path);
			int result = Constants.DEFAULT_WEIGHT;
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

	public CuratorClient getCuratorClient() {
		return client;
	}

	@Override
	public String getServerApp(String serverAddress) {
		String path = Utils.getAppPath(serverAddress);
		String strApp;
		try {
			strApp = client.get(path);
			if (strApp == null) {
				return "";
			}
			return strApp;
		} catch (Throwable e) {
			logger.error("failed to get app for " + serverAddress);
			return "";
		}
	}

	@Override
	public void setServerApp(String serverAddress, String app) {
		String path = Utils.getAppPath(serverAddress);
		if (StringUtils.isNotBlank(app)) {
			try {
				client.set(path, app);
			} catch (Throwable e) {
				logger.error("failed to set app of " + serverAddress + " to " + app);
			}
		}
	}

	public void unregisterServerApp(String serverAddress) {
		String path = Utils.getAppPath(serverAddress);
		try {
			if (client.exists(path, false)) {
				client.delete(path);
			}
		} catch (Throwable e) {
			logger.error("failed to delete app:" + path + ", caused by:" + e.getMessage());
		}
	}

	@Override
	public void setServerVersion(String serverAddress, String version) {
		String path = Utils.getVersionPath(serverAddress);
		if (StringUtils.isNotBlank(version)) {
			try {
				client.set(path, version);
			} catch (Throwable e) {
				logger.error("failed to set version of " + serverAddress + " to " + version);
			}
		}
	}

	@Override
	public String getServerVersion(String serverAddress) {
		String path = Utils.getVersionPath(serverAddress);
		try {
			return client.get(path);
		} catch (Throwable e) {
			logger.error("failed to get version for " + serverAddress);
			return "";
		}
	}

	public void unregisterServerVersion(String serverAddress) {
		String path = Utils.getVersionPath(serverAddress);
		try {
			if (client.exists(path, false)) {
				client.delete(path);
			}
		} catch (Throwable e) {
			logger.error("failed to delete version:" + path + ", caused by:" + e.getMessage());
		}
	}

	@Override
	public String getStatistics() {
		return getName() + ":" + client.getStatistics();
	}

	@Override
	public void setServerService(String serviceName, String group, String hosts) throws RegistryException {
		String servicePath = Utils.getServicePath(serviceName, group);

		try {
			client.set(servicePath, hosts);
		} catch (Throwable e) {
			logger.error("failed to set service hosts of " + serviceName + " to " + hosts);
			throw new RegistryException(e);
		}
	}

	@Override
	public void delServerService(String serviceName, String group) throws RegistryException {
		String servicePath = Utils.getServicePath(serviceName, group);

		try {
			List<String> children = client.getChildren(servicePath);

			if (children != null && children.size() > 0) {
				client.set(servicePath, "");
			} else {
				client.delete(servicePath);
			}
		} catch (Throwable e) {
			logger.error("failed to delete service hosts of " + serviceName);
			throw new RegistryException(e);
		}
	}

	@Override
	public void setHostsWeight(String serviceName, String group, String hosts, int weight) throws RegistryException {

		for (String host : hosts.split(",")) {
			setServerWeight(host, weight);
		}
	}

	@Override
	public void updateHeartBeat(String serviceAddress, Long heartBeatTimeMillis) {
		try {
			String heartBeatPath = Utils.getHeartBeatPath(serviceAddress);
			client.set(heartBeatPath, heartBeatTimeMillis);
		} catch (Throwable e) {
			logger.fatal("failed to update heartbeat", e);
		}
	}

	@Override
	public void deleteHeartBeat(String serviceAddress) {
		try {
			String heartBeatPath = Utils.getHeartBeatPath(serviceAddress);
			client.delete(heartBeatPath);
		} catch (Throwable e) {
			logger.fatal("failed to delete heartbeat", e);
		}
	}

	@Override
	public boolean isSupportNewProtocol(String serviceAddress) throws RegistryException {
		String version = getServerVersion(serviceAddress);

		if (StringUtils.isBlank(version)) {
			version = "";
		}

		return VersionUtils.isThriftSupported(version);
	}

	@Override
	public boolean isSupportNewProtocol(String serviceAddress, String serviceName) throws RegistryException {
		try {
			String protocolPath = Utils.getProtocolPath(serviceAddress);
			String info = client.get(protocolPath);

			if (info != null) {
				Map<String, Boolean> infoMap = Utils.getProtocolInfoMap(info);

				if(infoMap.containsKey(serviceName)) {
					return infoMap.get(serviceName);
				}

			}

			return false;

		} catch (Throwable e) {
			logger.error("failed to get protocol:" + serviceName
					+ "of host:" + serviceAddress + ", caused by:" + e.getMessage());
			throw new RegistryException(e);
		}
	}

	@Override
	public void setSupportNewProtocol(String serviceAddress, String serviceName, boolean support)
			throws RegistryException {
		try {
			String protocolPath = Utils.getProtocolPath(serviceAddress);
			Stat stat = new Stat();
			String info = client.get(protocolPath, stat);

			if(info != null) {
				Map<String, Boolean> infoMap = Utils.getProtocolInfoMap(info);
				infoMap.put(serviceName, support);
				client.set(protocolPath, Utils.getProtocolInfo(infoMap), stat.getVersion());
			} else {
				Map<String, Boolean> infoMap = ImmutableMap.of(serviceName, support);
				client.create(protocolPath, Utils.getProtocolInfo(infoMap));
			}

		} catch (Throwable e) {
			if (e instanceof BadVersionException || e instanceof NodeExistsException) {
				try {
					Thread.sleep(500);
				} catch (InterruptedException ie) {
					//ignore
				}
				setSupportNewProtocol(serviceAddress, serviceName, support);
			} else {
				logger.error("failed to set protocol:" + serviceName
						+ "of host:" + serviceAddress + " to:" + support
						+ ", caused by:" + e.getMessage());
				throw new RegistryException(e);
			}

		}
	}

	@Override
	public void unregisterSupportNewProtocol(String serviceAddress, String serviceName,
											 boolean support) throws RegistryException {
		try {
			String protocolPath = Utils.getProtocolPath(serviceAddress);
			Stat stat = new Stat();
			String info = client.get(protocolPath, stat);

			if(info != null) {
				Map<String, Boolean> infoMap = Utils.getProtocolInfoMap(info);
				infoMap.remove(serviceName);

				if (infoMap.size() == 0 && delEmptyNode) {
					client.delete(protocolPath);
				} else {
					client.set(protocolPath, Utils.getProtocolInfo(infoMap), stat.getVersion());
				}

			}

		} catch (Throwable e) {
			if (e instanceof BadVersionException || e instanceof NodeExistsException) {
				try {
					Thread.sleep(500);
				} catch (InterruptedException ie) {
					//ignore
				}
				unregisterSupportNewProtocol(serviceAddress, serviceName, support);
			} else {
				logger.error("failed to del protocol:" + serviceName
						+ "of host:" + serviceAddress + ", caused by:" + e.getMessage());
				throw new RegistryException(e);
			}

		}
	}
}
