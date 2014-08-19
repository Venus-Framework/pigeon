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
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.KeeperException.NoNodeException;
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
	private String address = null;

	private Properties properties;

	public ZookeeperRegistry() {
	}

	private void _init() {
		if (!this.isInit) {
			String zookeeperAddress = properties.getProperty(Constants.KEY_REGISTRY_ADDRESS);
			address = zookeeperAddress;
			logger.info("Zookeeper address " + zookeeperAddress);
			logger.info("Zookeeper timeout " + timeout);

			try {
				this.zkClient = new ZooKeeperWrapper(zookeeperAddress, timeout, null);
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
				this.zkWatcher = new ZookeeperWatcher(this);
			} catch (Throwable e) {
				logger.error("", e);
				throw new RuntimeException("Failed to initialize zookeeper registry", e);
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
		} catch (Throwable e) {
			logger.error("", e);
			throw new RegistryException(e);
		}
		return null;
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
		if (!StringUtils.isBlank(group) && getZkValue(path) == null) {
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
		} catch (Throwable e) {
			throw new RegistryException(e);
		}
	}

	public void watchZkPath(String path) throws RegistryException {
		try {
			zkClient.exists(path, zkWatcher);
		} catch (Throwable e) {
			throw new RegistryException(e);
		}
	}

	@Override
	public void registerService(String serviceName, String group, String serviceAddress, int weight)
			throws RegistryException {
		if (StringUtils.isBlank(group)) {
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
		} catch (Throwable e) {
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
			if (weight >= 0) {
				zkClient.updateData(weightPath, "" + weight);
			}
			// 2. Register address
			if (zkClient.exists(servicePath, false) != null) {
				String addressValue = new String(zkClient.getData(servicePath, false, null), Constants.CHARSET);
				String[] addressArray = addressValue.split(",");
				List<String> addressList = new ArrayList<String>();
				for (String addr : addressArray) {
					if (!StringUtils.isBlank(addr) && !addressList.contains(addr)) {
						addressList.add(addr);
					}
				}
				if (!addressList.contains(serviceAddress)) {
					addressList.add(serviceAddress);
					Collections.sort(addressList);
					zkClient.updateData(servicePath, StringUtils.join(addressList.iterator(), ","));
				}
			} else {
				zkClient.updateData(servicePath, serviceAddress);
			}
		} catch (Throwable e) {
			logger.error("error while registering service to registry:" + serviceName, e);
			throw new RegistryException(e);
		}
	}

	@Override
	public int getServerWeight(String serverAddress) throws RegistryException {
		String path = Utils.getWeightPath(serverAddress);
		String strWeight = getZkValue(path);
		int result = Constants.WEIGHT_DEFAULT;
		if (strWeight != null) {
			result = Integer.parseInt(strWeight);
		}
		return result;
	}

	@Override
	public String getName() {
		return "zookeeper-" + address;
	}

	@Override
	public void unregisterService(String serviceName, String serviceAddress) throws RegistryException {
		unregisterService(serviceName, Constants.DEFAULT_GROUP, serviceAddress);
	}

	@Override
	public void unregisterService(String serviceName, String group, String serviceAddress) throws RegistryException {
		if (StringUtils.isBlank(group)) {
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
					if (!StringUtils.isBlank(addr) && !addressList.contains(addr)) {
						addressList.add(addr);
					}
				}
				if (addressList.contains(serviceAddress)) {
					addressList.remove(serviceAddress);
					if (!addressList.isEmpty()) {
						Collections.sort(addressList);
						zkClient.updateData(servicePath, StringUtils.join(addressList.iterator(), ","));
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
		} catch (Throwable e) {
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
		} catch (Throwable e) {
			throw new RegistryException(e);
		}
	}

	@Override
	public Set<String> getReferencedServices() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<String> getRegisteredServices() {
		// TODO Auto-generated method stub
		return null;
	}

}
