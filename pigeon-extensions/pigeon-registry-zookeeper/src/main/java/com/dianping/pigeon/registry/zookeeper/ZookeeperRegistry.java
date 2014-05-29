/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.registry.zookeeper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.KeeperException.NoNodeException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.data.Stat;

import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.registry.Registry;
import com.dianping.pigeon.registry.exception.RegistryException;
import com.dianping.pigeon.registry.listener.DefaultServiceChangeListener;
import com.dianping.pigeon.registry.listener.ServiceChangeListener;
import com.dianping.pigeon.registry.util.Constants;
import com.dianping.pigeon.util.CollectionUtils;

public class ZookeeperRegistry implements Registry {

	private static Logger logger = LoggerLoader.getLogger(ZookeeperRegistry.class);

	private ZooKeeperWrapper zkClient;
	private ZookeeperWatcher zkWatcher;

	private ServiceChangeListener serviceChangeListener = new DefaultServiceChangeListener();

	private boolean isInit = false;
	private int timeout = 20000;

	private Properties properties;

	public ZookeeperRegistry() {
	}

	private void _init() {
		if (!this.isInit) {
			String zookeeperAddress = properties.getProperty(Constants.KEY_REGISTRY_ADDRESS);

			logger.info("Zookeeper address " + zookeeperAddress);
			logger.info("Zookeeper timeout " + timeout);

			try {
				this.zkClient = new ZooKeeperWrapper(zookeeperAddress, timeout, new ZkStateWatcher());
				if (this.zkClient.exists(Constants.DP_PATH, false) == null) {
					this.zkClient.create(Constants.DP_PATH, new byte[0], Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
				}
				if (this.zkClient.exists(Constants.SERVICE_PATH, false) == null) {
					this.zkClient.create(Constants.SERVICE_PATH, new byte[0], Ids.OPEN_ACL_UNSAFE,
							CreateMode.PERSISTENT);
				}
				if (this.zkClient.exists(Constants.WEIGHT_PATH, false) == null) {
					this.zkClient
							.create(Constants.WEIGHT_PATH, new byte[0], Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
				}
				if (this.zkClient.exists(Constants.REGISTRY_PATH, false) == null) {
					this.zkClient.create(Constants.REGISTRY_PATH, new byte[0], Ids.OPEN_ACL_UNSAFE,
							CreateMode.PERSISTENT);
				}
				this.zkWatcher = new ZookeeperWatcher(this);
			} catch (Exception e) {
				logger.error("", e);
				throw new RuntimeException("Failed to initialize zookeeper registery", e);
			}
			this.isInit = true;
		}
	}

	public void init(Properties properties) {
		this.properties = properties;
		_init();
	}

	public ServiceChangeListener getServiceChangeListener() {
		return this.serviceChangeListener;
	}

	public String getZkValue(String path) throws RegistryException {
		try {
			if (this.zkClient.exists(path, zkWatcher) != null) {
				String value = new String(this.zkClient.getData(path, false, null), Constants.CHARSET);
				if (logger.isInfoEnabled()) {
					logger.info("Get value from zookeeper " + "path: " + path + "  value: " + value);
				}
				return value;
			}
		} catch (Exception e) {
			logger.error("", e);
			throw new RegistryException(e);
		}
		return null;
	}

	// serviceAddress 规范为1.1.1.1:8080,2.2.2.2:8080,
	// FIXME refactor this code
	public List<String[]> getServiceIpPortWeight(String serviceAddress) {
		List<String[]> result = new ArrayList<String[]>();
		if (serviceAddress != null && serviceAddress.length() > 0) {
			String[] temp = serviceAddress.split(",");
			if (temp != null && temp.length > 0) {
				for (String total : temp) {
					String[] resultTemp = total.split(":");
					result.add(resultTemp);
				}
			}
		}
		return result;
	}

	public ZooKeeperWrapper getZkClient() {
		return zkClient;
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
		String path = Utils.getServicePath(serviceName, group);
		if (!Utils.isEmpty(group) && getZkValue(path) == null) {
			logger.info(path + " does not exist. Fallback to default group");
			path = Utils.getServicePath(serviceName, Constants.DEFAULT_GROUP);
		}
		String address = getZkValue(path);
		if (address != null) {
			if (logger.isInfoEnabled()) {
				logger.info("get service address from zookeeper, service name:" + path + "  address:" + address);
			}
		}
		return address;
	}

	private boolean zkExists(String path) throws RegistryException {
		try {
			return zkClient.exists(path, false) != null;
		} catch (Exception e) {
			throw new RegistryException(e);
		}
	}

	public void watchZkPath(String path) throws RegistryException {
		try {
			zkClient.exists(path, zkWatcher);
		} catch (Exception e) {
			throw new RegistryException(e);
		}
	}

	@Override
	public void registerService(String serviceName, String serviceAddress) throws RegistryException {
		registerService(serviceName, Constants.DEFAULT_GROUP, serviceAddress, Constants.WEIGHT_DEFAULT);
	}

	@Override
	public void registerService(String serviceName, String group, String serviceAddress, int weight)
			throws RegistryException {
		if (Utils.isBlank(group)) {
			group = Constants.DEFAULT_GROUP;
		}
		registerServiceToZookeeper(serviceName, group, serviceAddress, weight);
		if (logger.isInfoEnabled()) {
			String path = Utils.getServicePath(serviceName, group);
			logger.info("registered service to registry path: " + path);
		}
	}

	public void setServerWeight(String serverAddress, int weight) throws RegistryException {
		try {
			String weightPath = Utils.getWeightPath(serverAddress);
			zkClient.updateData(weightPath, "" + weight);
		} catch (Exception e) {
			logger.error("error while setting weight:" + serverAddress + " to " + weight, e);
			throw new RegistryException(e);
		}
	}

	private void registerServiceToZookeeper(String serviceName, String group, String serviceAddress, int weight)
			throws RegistryException {
		String weightPath = Utils.getWeightPath(serviceAddress);
		String servicePath = Utils.getServicePath(serviceName, group);
		try {
			// 1. Register weight
			zkClient.updateData(weightPath, "" + weight);
			// 2. Register address
			if (zkClient.exists(servicePath, false) != null) {
				String addressValue = new String(zkClient.getData(servicePath, false, null), Constants.CHARSET);
				String[] addressArray = addressValue.split(",");
				List<String> addressList = new ArrayList<String>();
				for (String addr : addressArray) {
					if (!Utils.isBlank(addr) && !addressList.contains(addr)) {
						addressList.add(addr);
					}
				}
				if (!addressList.contains(serviceAddress)) {
					addressList.add(serviceAddress);
					Collections.sort(addressList);
					zkClient.updateData(servicePath, Utils.join(addressList.iterator(), ","));
				}
			} else {
				zkClient.updateData(servicePath, serviceAddress);
			}
		} catch (Exception e) {
			logger.error("error while registering service to registry:" + serviceName, e);
			throw new RegistryException(e);
		}
	}

	@Override
	public int getServiceWeigth(String serviceAddress) throws RegistryException {
		String path = Utils.getWeightPath(serviceAddress);
		String strWeight = getZkValue(path);
		int result = Constants.WEIGHT_DEFAULT;
		if (strWeight != null) {
			result = Integer.parseInt(strWeight);
		}
		return result;
	}

	@Override
	public Properties getRegistryMeta(String serviceAddress) throws RegistryException {
		Properties props = new Properties();
		try {
			String path = Utils.getRegistryPath(serviceAddress);
			if (zkClient.exists(path, false) == null) {
				return props;
			}

			path = Utils.getRegistryPath(serviceAddress, Constants.KEY_GROUP);
			String value = new String(zkClient.getData(path, false, null), Constants.CHARSET);
			if (!Utils.isEmpty(value)) {
				props.put(Constants.KEY_GROUP, value);
			}

			path = Utils.getRegistryPath(serviceAddress, Constants.KEY_WEIGHT);
			value = new String(zkClient.getData(path, false, null), Constants.CHARSET);
			if (!Utils.isEmpty(value)) {
				int weight = Integer.parseInt(value);
				props.put(Constants.KEY_WEIGHT, weight);
			}

			path = Utils.getRegistryPath(serviceAddress, Constants.KEY_AUTO_REGISTER);
			value = new String(zkClient.getData(path, false, null), Constants.CHARSET);
			if (!Utils.isEmpty(value)) {
				boolean autoRegister = Boolean.parseBoolean(value);
				props.put(Constants.KEY_AUTO_REGISTER, autoRegister);
			}

			logger.info("Registry meta for " + serviceAddress + " is " + props);
			return props;
		} catch (Exception e) {
			logger.error("Failed to get regsitry meta for " + serviceAddress, e);
			throw new RegistryException(e);
		}
	}

	@Override
	public String getName() {
		return "zookeeper";
	}

	class ZkStateWatcher implements Watcher {
		@Override
		public void process(WatchedEvent event) {
			if (event.getState() == KeeperState.Expired) {
				if (logger.isInfoEnabled()) {
					logger.info("Zookeeper session expried");
				}
				try {
					zkClient.sessionExpiredReConnect();
					logger.info("Zookeeper session reconnected");
				} catch (Exception e) {
					logger.error("Failed to reconnect to zookeeper", e);
				}
				return;
			}
		}
	}

	@Override
	public void unregisterService(String serviceName, String serviceAddress) throws RegistryException {
		unregisterService(serviceName, Constants.DEFAULT_GROUP, serviceAddress);
	}

	@Override
	public void unregisterService(String serviceName, String group, String serviceAddress) throws RegistryException {
		if (Utils.isBlank(group)) {
			group = Constants.DEFAULT_GROUP;
		}
		unregisterServiceFromZookeeper(serviceName, group, serviceAddress);
		if (logger.isInfoEnabled()) {
			logger.info("unregistered [" + serviceAddress + "] from service:" + serviceName);
		}
	}

	private void unregisterServiceFromZookeeper(String serviceName, String group, String serviceAddress)
			throws RegistryException {
		// String weightPath = Utils.getWeightPath(serviceAddress);
		String servicePath = Utils.getServicePath(serviceName, group);
		try {
			// 1. Register weight
			// Stat statWeight = zkClient.exists(weightPath, false);
			// if (statWeight != null) {
			// try {
			// zkClient.delete(weightPath, statWeight.getVersion());
			// } catch (NoNodeException e) {
			// logger.warn("Already deleted path:" + weightPath + ":" +
			// e.getMessage());
			// }
			// }
			// 2. Register address
			Stat statService = zkClient.exists(servicePath, false);
			if (statService != null) {
				String addressValue = new String(zkClient.getData(servicePath, false, null), Constants.CHARSET);
				String[] addressArray = addressValue.split(",");
				List<String> addressList = new ArrayList<String>();
				for (String addr : addressArray) {
					if (!Utils.isBlank(addr) && !addressList.contains(addr)) {
						addressList.add(addr);
					}
				}
				if (addressList.contains(serviceAddress)) {
					addressList.remove(serviceAddress);
					if (!addressList.isEmpty()) {
						Collections.sort(addressList);
						zkClient.updateData(servicePath, Utils.join(addressList.iterator(), ","));
					} else {
						List<String> children = zkClient.getChildren(servicePath, false);
						if (CollectionUtils.isEmpty(children)) {
							try {
								zkClient.delete(servicePath, statService.getVersion());
							} catch (NoNodeException e) {
								logger.warn("Already deleted path:" + servicePath + ":" + e.getMessage());
							}
						} else {
							logger.warn("Existing children [" + children + "] under path:" + servicePath);
							zkClient.updateData(servicePath, "");
						}
					}
				}
			}
		} catch (Exception e) {
			logger.error("error while unregistering service from registry:" + serviceName, e);
			throw new RegistryException(e);
		}
	}

	@Override
	public List<String> getChildren(String key) throws RegistryException {
		try {
			return zkClient.getChildren(key, false);
		} catch (KeeperException e) {
			throw new RegistryException(e);
		} catch (InterruptedException e) {
			throw new RegistryException(e);
		} catch (IOException e) {
			throw new RegistryException(e);
		} catch (Exception e) {
			throw new RegistryException(e);
		}
	}

}
