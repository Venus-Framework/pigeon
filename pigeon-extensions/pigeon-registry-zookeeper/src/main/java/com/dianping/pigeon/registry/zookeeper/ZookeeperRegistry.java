/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.registry.zookeeper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooDefs.Ids;

import com.dianping.pigeon.monitor.Log4jLoader;
import com.dianping.pigeon.registry.Registry;
import com.dianping.pigeon.registry.RegistryMeta;
import com.dianping.pigeon.registry.exception.RegistryException;
import com.dianping.pigeon.registry.listener.DefaultServiceChangeListener;
import com.dianping.pigeon.registry.listener.ServiceChangeListener;
import com.dianping.pigeon.registry.util.Constants;

public class ZookeeperRegistry implements Registry {

	private static Logger logger = Log4jLoader.getLogger(ZookeeperRegistry.class);
	
	private ZooKeeperWrapper zkClient;
	private ZookeeperWatcher zkWatcher;

	private ServiceChangeListener serviceChangeListener = new DefaultServiceChangeListener();

	private boolean isInit = false;
	private int timeout = 60000;

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
					this.zkClient.create(Constants.SERVICE_PATH, new byte[0], Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
				}
				if (this.zkClient.exists(Constants.WEIGHT_PATH, false) == null) {
					this.zkClient.create(Constants.WEIGHT_PATH, new byte[0], Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
				}
				if (this.zkClient.exists(Constants.REGISTRY_PATH, false) == null) {
					this.zkClient.create(Constants.REGISTRY_PATH, new byte[0], Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
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
					logger.info("Get value from zookeeper " + 
				                "path: " + path + "  value: " + value);
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
		if(!Utils.isEmpty(group) && !zkExists(path)) {
			logger.info(path + " does not exist. Fallback to default group");
			path = Utils.getServicePath(serviceName, Constants.DEFAULT_GROUP);
		}
		String address = getZkValue(path);
		if(address != null) {
			if (logger.isInfoEnabled()) {
				logger.info("get service address from zookeeper, service name:"
						+ path + "  address:" + address);
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
	public void publishService(String serviceName, String serviceAddress) throws RegistryException {
		publishService(serviceName, Constants.DEFAULT_GROUP, serviceAddress, Constants.DEFAULT_WEIGHT_INT);
	}

	@Override
	public void publishService(String serviceName, String group, String serviceAddress, int weight) throws RegistryException {
		String result = publishService2Zookeeper(serviceName, group, serviceAddress, weight);
		String path = Utils.getServicePath(serviceName, group);
		logger.info("published service to registry path: " + path + " value: " + result);
	}
	
	private String publishService2Zookeeper(String serviceName, String group, String serviceAddress, int weight) throws RegistryException {
		String weightPath = Utils.getWeightPath(serviceAddress);
		String servicePath = Utils.getServicePath(serviceName, group);
		try {
			// 1. Register weight
			zkClient.updateData(weightPath, ""+weight);
			// 2. Register address
			if (zkClient.exists(servicePath, false) != null) {
				String addressValue = new String(zkClient.getData(servicePath, false, null), Constants.CHARSET);
				String[] addressArray = addressValue.split(",");
				List<String> addressList = new ArrayList<String>();
				for (String addr : addressArray) {
					if (StringUtils.isNotBlank(addr) && !addressList.contains(addr)) {
						addressList.add(addr);
					}
				}
				if (!addressList.contains(serviceAddress)) {
					addressList.add(serviceAddress);
					Collections.sort(addressList);
					zkClient.updateData(servicePath, StringUtils.join(addressList, ","));
				}
			} else {
				zkClient.updateData(servicePath, serviceAddress);
			}
			return serviceAddress;
		} catch (Exception e) {
			logger.error("error while publishing service to registry:" + serviceName, e);
			throw new RegistryException(e);
		}
	}

	@Override
	public int getServiceWeigth(String serviceAddress) throws RegistryException {
		String path = Utils.getWeightPath(serviceAddress);
		String strWeight = getZkValue(path);
		Integer result = 1;
		if (strWeight != null) {
			result = Integer.parseInt(strWeight);
		}
		return result;
	}

	@Override
	public RegistryMeta getRegistryMeta(String serviceAddress) throws RegistryException {
		try {
			String path = Utils.getRegistryPath(serviceAddress);
			if(zkClient.exists(path, false) == null) {
				return RegistryMeta.DEFAULT_REGISTRY_META;
			}
			
			RegistryMeta registryMeta = new RegistryMeta();
			path = Utils.getRegistryPath(serviceAddress, Constants.KEY_GROUP);
			String value = new String(zkClient.getData(path, false, null), Constants.CHARSET);
			if(!Utils.isEmpty(value))
				registryMeta.setGroup(value);
			else
				registryMeta.setGroup(Constants.DEFAULT_GROUP);
			
			path = Utils.getRegistryPath(serviceAddress, Constants.KEY_WEIGHT);
			value = new String(zkClient.getData(path, false, null), Constants.CHARSET);
			if(!Utils.isEmpty(value)) {
				int weight = Integer.parseInt(value);
				registryMeta.setWeight(weight);
			} else 
				registryMeta.setWeight(Constants.DEFAULT_WEIGHT_INT);
			
			path = Utils.getRegistryPath(serviceAddress, Constants.KEY_AUTO_REGISTER);
			value = new String(zkClient.getData(path, false, null), Constants.CHARSET);
			if(!Utils.isEmpty(value)) {
				boolean autoRegister = Boolean.parseBoolean(value);
				registryMeta.setAutoRegister(autoRegister);
			}
			else 
				registryMeta.setAutoRegister(Constants.DEFAULT_AUTO_REGISTER_BOOL);
			
			logger.info("Registry meta for " + serviceAddress + " is " + registryMeta);
			return registryMeta;
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

}
