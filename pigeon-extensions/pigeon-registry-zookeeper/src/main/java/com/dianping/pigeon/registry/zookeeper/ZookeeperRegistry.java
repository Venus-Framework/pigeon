/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.registry.zookeeper;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooDefs.Ids;

import com.dianping.pigeon.registry.Registry;
import com.dianping.pigeon.registry.exception.RegistryException;
import com.dianping.pigeon.registry.listener.ConfigChangeListener;
import com.dianping.pigeon.registry.listener.DefaultServiceChangeListener;
import com.dianping.pigeon.registry.listener.ServiceChangeListener;
import com.dianping.pigeon.registry.util.Constants;
import com.dianping.pigeon.registry.util.NumberUtils;
import com.dianping.pigeon.registry.zookeeper.watcher.PigeonHostWatcher;
import com.dianping.pigeon.registry.zookeeper.watcher.PigeonServiceWatcher;

public class ZookeeperRegistry implements Registry {

	private static Logger logger = Logger.getLogger(ZookeeperRegistry.class);
	Map<String, Integer> serviceAddressWeightMap;
	Map<String, String> serviceAddressCache;
	private ZooKeeperWrapper zk;

	private ServiceChangeListener serviceChangeListener = new DefaultServiceChangeListener();
	private PigeonHostWatcher pigeonHostWatcher;
	private PigeonServiceWatcher pigeonServiceWatcher;

	private static Map<String, StringValue> cache = new ConcurrentHashMap<String, StringValue>();
	private static Map<String, Long> timestampMap = new ConcurrentHashMap<String, Long>();

	private List<ConfigChangeListener> configChangeListeners = new ArrayList<ConfigChangeListener>(); // CopyOnWriteArrayList

	private boolean isInit = false;
	private int timeout = 60000;

	private static String zookeeperAddress = null;

	private static Registry registryCache = null;

	public static Registry getInstance() {
		return registryCache;
	}

	public static synchronized Registry getInstance(String address) {
		zookeeperAddress = address;
		registryCache = new ZookeeperRegistry();
		return registryCache;
	}

	public ZookeeperRegistry() {

	}

	private void _init() {
		if (!this.isInit) {
			logger.info("zookeeper address:" + zookeeperAddress);
			logger.info("timeout:" + timeout);

			try {
				this.zk = new ZooKeeperWrapper(zookeeperAddress, this.timeout, new ConfigWatcher());
				if (this.zk.exists(Constants.DP_PATH, false) == null) {
					this.zk.create(Constants.DP_PATH, new byte[0], Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
				}
				if (this.zk.exists(Constants.CONFIG_PATH, false) == null) {
					this.zk.create(Constants.CONFIG_PATH, new byte[0], Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
				}
				Thread t = new Thread(new CheckConfig());
				t.setDaemon(true);
				t.setName("Pigeon-Zookeeper-Check-Thread");
				t.start();
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				throw new RuntimeException("init zookeeper registry failed", e);
			}
			this.isInit = true;
		}
	}

	public String getValue(String key) throws RegistryException {
		StringValue value = cache.get(key);
		if (value == null) {
			String path = Constants.CONFIG_PATH + "/" + key;
			String timestampPath = path + "/" + Constants.CONFIG_TIMESTAMP;
			try {
				if (this.zk.exists(path, false) != null) {
					Watcher watcher = new ConfigDataWatcher(path, timestampPath, key);
					value = new StringValue(new String(this.zk.getData(path, watcher, null), Constants.CHARSET));
					cache.put(key, value);
					if (logger.isInfoEnabled()) {
						logger.info(">>>>>>>>>>>>getProperty key:" + key + "  value:" + value.getValue());
					}
					if (this.zk.exists(timestampPath, false) != null) {
						Long timestamp = NumberUtils.getLong(this.zk.getData(timestampPath, false, null));
						timestampMap.put(path, timestamp);
					}
				} else {
					cache.put(key, new StringValue(null));
					if (logger.isInfoEnabled()) {
						logger.info(">>>>>>>>>>>getProperty key:" + key + "   value is null*******");
					}
				}
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				throw new RegistryException(e);
			}
		}
		if (value != null) {
			return value.getValue();
		}
		return null;
	}

	public void init(Properties properties) {
		zookeeperAddress = properties.getProperty("registryServer");

		_init();

		serviceAddressWeightMap = new HashMap<String, Integer>();
		serviceAddressCache = new ConcurrentHashMap<String, String>();// TODO, FIXBUG
		pigeonHostWatcher = new PigeonHostWatcher(this);
		pigeonServiceWatcher = new PigeonServiceWatcher(this);

		try {
			if (this.zk.exists(Constants.DP_PATH, false) == null) {
				this.zk.create(Constants.DP_PATH, new byte[0], Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
			}
			if (this.zk.exists(Constants.SERVICE_PATH, false) == null) {
				this.zk.create(Constants.SERVICE_PATH, new byte[0], Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
			}
			if (this.zk.exists(Constants.WEIGHT_PATH, false) == null) {
				this.zk.create(Constants.WEIGHT_PATH, new byte[0], Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

	public ServiceChangeListener getServiceChangeListener() {
		return this.serviceChangeListener;
	}

	public String getWeightValue(String key) throws RegistryException {
		String path = Constants.WEIGHT_PATH + "/" + key;
		try {
			if (this.zk.exists(path, false) != null) {
				String value = new String(this.zk.getData(path, this.pigeonHostWatcher, null), Constants.CHARSET);
				if (logger.isInfoEnabled()) {
					logger.info("Pigeon Get Host Weight Value  From ZooKeeper Server! ZooKeeper Address :"
							+ this.zk.getAddresses() + " Key:" + key + "  Value:" + value);
				}
				return value;
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new RegistryException(e);
		}
		return null;
	}

	public String getServiceValue(String key) throws RegistryException {
		String path = Constants.SERVICE_PATH + "/" + key;
		try {
			if (this.zk.exists(path, false) != null) {
				String value = new String(this.zk.getData(path, this.pigeonServiceWatcher, null), Constants.CHARSET);
				if (logger.isInfoEnabled()) {
					logger.info("Pigeon Get Service Value From ZooKeeper Server! ZooKeeper Address :"
							+ this.zk.getAddresses() + " Key:" + key.replace(Constants.PLACEHOLD, "/") + "  Value:"
							+ value);
				}
				return value;
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new RegistryException(e);
		}
		return null;
	}

	// serviceAddress ���������1.1.1.1:8080,2.2.2.2:8080,
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

	class ConfigWatcher implements Watcher {
		@Override
		public void process(WatchedEvent event) {
			if (event.getState() == KeeperState.Expired) {
				if (logger.isInfoEnabled()) {
					logger.info("Session Expried init,Invoke ZooKeeperWrapper method!");
				}
				try {
					zk.sessionExpiredReConnect();
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				}
				return;
			}
		}
	}

	private class ConfigDataWatcher implements Watcher {

		private String path;
		private String timestampPath;
		private String key;

		public ConfigDataWatcher(String path, String timestampPath, String key) {
			this.path = path;
			this.timestampPath = timestampPath;
			this.key = key;
		}

		@Override
		public void process(WatchedEvent event) {
			if (event.getType() == EventType.NodeCreated || event.getType() == EventType.NodeDataChanged) {
				try {
					zk.removeWatcher(this.path);
					if (zk.exists(this.timestampPath, false) != null) {
						Long timestamp = NumberUtils.getLong(zk.getData(this.timestampPath, false, null));
						Long timestamp_ = timestampMap.get(this.path);
						if (timestamp_ == null || timestamp > timestamp_) {
							timestampMap.put(this.path, timestamp);
							byte[] data = zk.getData(this.path, this, null);
							if (data != null) {
								String value = new String(data, Constants.CHARSET);
								if (logger.isInfoEnabled()) {
									logger.info(">>>>>>>>>>>>pushProperty key:" + key + "  value:" + value);
								}
								StringValue sv = cache.get(this.key);
								if (sv == null) {
									sv = new StringValue(value);
									cache.put(this.key, sv);
								}
								synchronized (sv) {
									sv.value = value;
									if (configChangeListeners != null) {
										for (ConfigChangeListener change : configChangeListeners) {
											change.onChange(this.key, value);
										}
									}
								}
							}
						} else {
							zk.getData(this.path, this, null);
						}
					} else {
						zk.getData(this.path, this, null);
					}
				} catch (KeeperException e) {
					logger.error(e.getMessage(), e);
				} catch (InterruptedException e) {
					logger.error(e.getMessage(), e);
				} catch (UnsupportedEncodingException e) {
					logger.error(e.getMessage(), e);
				} catch (IOException e) {
					logger.error(e.getMessage(), e);
				}
			} else if (event.getType() == EventType.NodeDeleted) {
				cache.remove(this.key);
			}
		}

	}

	private class CheckConfig implements Runnable {

		private long lastTime = System.currentTimeMillis();

		@Override
		public void run() {
			int k = 0;
			while (true) {
				try {
					long now = System.currentTimeMillis();
					if (now - this.lastTime > 20000) {
						for (Entry<String, StringValue> entry : cache.entrySet()) {
							synchronized (entry.getValue()) {

								String path = Constants.CONFIG_PATH + "/" + entry.getKey();
								String timestampPath = path + "/" + Constants.CONFIG_TIMESTAMP;
								if (zk.exists(timestampPath, false) != null) {
									Long timestamp = NumberUtils.getLong(zk.getData(timestampPath, false, null));
									Long timestamp_ = timestampMap.get(path);
									if (timestamp_ == null || timestamp > timestamp_) {
										timestampMap.put(path, timestamp);
										if (zk.exists(path, false) != null) {
											String value = new String(zk.getData(path, false, null), Constants.CHARSET);

											if (!value.equals(entry.getValue().value)) {
												if (logger.isInfoEnabled()) {
													logger.info(">>>>>>>>>>>>syncProperty key:" + entry.getKey()
															+ "  value:" + value);
												}
												entry.getValue().value = value;
												if (configChangeListeners != null) {
													for (ConfigChangeListener change : configChangeListeners) {
														change.onChange(entry.getKey(), value);
													}
												}
											}
										}
									}
								}

							}
						}
						this.lastTime = now;
					} else {
						Thread.sleep(2000);
					}
					k = 0;
				} catch (Exception e) {
					k++;
					if (k > 3) {
						try {
							Thread.sleep(2000);
							k = 0;
						} catch (InterruptedException e1) {
							logger.error("", e1);
						}
					}
					logger.error(e.getMessage(), e);
				}
			}
		}
	}

	private static class StringValue {
		String value;

		StringValue(String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}
	}

	void reset() {
		cache.clear();
		timestampMap.clear();
		configChangeListeners.clear();
	}

	/**
	 * @param change
	 *            the change to set
	 */
	public void addConfigChangeListener(ConfigChangeListener configChangeListener) {
		this.configChangeListeners.add(configChangeListener);
	}

	/**
	 * @return the host
	 */
	public Map<String, Integer> getHost() {
		return serviceAddressWeightMap;
	}

	/**
	 * @return the service
	 */
	public Map<String, String> getService() {
		return serviceAddressCache;
	}

	private String replaceServiceName(String temp) {
		return temp.replace("/", Constants.PLACEHOLD);
	}

	@Override
	public String getServiceAddress(String serviceName) throws RegistryException {
		String keyReplace = this.replaceServiceName(serviceName);
		// TODO,ignore
//		String value = RegistryCache.getProperty(keyReplace);
//		if (value != null) {
//			if (logger.isInfoEnabled()) {
//				logger.info("get service address from zookeeper, service name:" + serviceName + ", address:" + value);
//			}
//			return value;
//		}
		if (serviceAddressCache.containsKey(keyReplace)) {
			if (logger.isInfoEnabled()) {
				logger.info("get service address from zookeeper, service name:" + serviceName + "  address:"
						+ serviceAddressCache.get(keyReplace));
			}
			return serviceAddressCache.get(keyReplace);
		} else {
			String result = this.getServiceValue(keyReplace);
			if (result != null) {
				serviceAddressCache.put(keyReplace, result);
			}
			return result;
		}
	}

	@Override
	public void publishServiceAddress(String serviceName, String serviceAddress) throws RegistryException {
		String keyReplace = this.replaceServiceName(serviceName);
		String result = publishServiceAddress2Zookeeper(keyReplace, serviceAddress);
		serviceAddressCache.put(keyReplace, result);
		logger.info("published service to registry:" + serviceName);
	}

	private String publishServiceAddress2Zookeeper(String serviceName, String serviceAddress) throws RegistryException {
		String path = Constants.SERVICE_PATH + "/" + serviceName;
		try {
			if (zk.exists(path, false) != null) {
				String addressValue = new String(zk.getData(path, false, null), Constants.CHARSET);
				String[] addressArray = addressValue.split(",");
				List<String> addressList = new ArrayList<String>();
				for (String ad : addressArray) {
					if (StringUtils.isNotBlank(ad) && !addressList.contains(ad)) {
						addressList.add(ad);
					}
				}
				if (!addressList.contains(serviceAddress)) {
					addressList.add(serviceAddress);
					Collections.sort(addressList);
					zk.updateData(path, StringUtils.join(addressList, ","));
				}
			} else {
				zk.updateData(path, serviceAddress);
			}
			return serviceAddress;
		} catch (Exception e) {
			logger.error("error while publishing service to registry:" + serviceName, e);
			throw new RegistryException(e);
		}
	}

	@Override
	public Integer getServiceWeigth(String serviceAddress) throws RegistryException {
		if (serviceAddressWeightMap.containsKey(serviceAddress)) {
			logger.info("get service weight from zookeeper, service address:" + serviceAddress + "  weight:"
					+ serviceAddressWeightMap.get(serviceAddress));
			return serviceAddressWeightMap.get(serviceAddress);
		} else {
			String strWeight = this.getWeightValue(serviceAddress);
			Integer result = 1;
			if (strWeight != null) {
				result = Integer.parseInt(strWeight);
			}
			serviceAddressWeightMap.put(serviceAddress, result);
			return result;
		}
	}

	@Override
	public String getName() {
		return "zookeeper";
	}

}
