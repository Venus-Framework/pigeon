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
import com.dianping.pigeon.governor.util.Constants.Action;
import com.dianping.pigeon.registry.exception.RegistryException;
import com.dianping.pigeon.registry.zookeeper.Utils;
import com.dianping.pigeon.util.CollectionUtils;

public class CuratorRegistry {

	private static Logger logger = Logger.getLogger(CuratorRegistry.class);

	private ConfigManager configManager = ExtensionLoader.getExtension(ConfigManager.class);

	private CuratorClient client;

	private boolean inited = false;

	private ServiceOfflineListener serviceOfflineListener;

	private Map<String, List<String>> ephemeralAddresses = new HashMap<String, List<String>>();

	private Set<String> services = new HashSet<String>();

	private final boolean delEmptyNode = configManager.getBooleanValue("pigeon-governor.registry.delemptynode", false);

	private String env;

	private String address;

	private Action action;

	public String getEnv() {
		return env;
	}

	public void setEnv(String env) {
		this.env = env;
	}

	public Action getAction() {
		return action;
	}

	public void setAction(Action action) {
		this.action = action;
	}

	public Map<String, List<String>> getEphemeralAddresses() {
		return ephemeralAddresses;
	}

	public Set<String> getServices() {
		return services;
	}

	public void init(String env, String address, Action action) {
		if (!inited) {
			try {
				serviceOfflineListener = new ServiceOfflineListener(this);
				logger.warn(env + "#start to initialize zookeeper client:" + address);
				client = new CuratorClient(address, this);
				logger.warn(env + "#succeed to initialize zookeeper client:" + address);
				this.env = env;
				this.address = address;
				this.action = action;
			} catch (Exception ex) {
				logger.error(env + "#failed to initialize zookeeper client", ex);
			}
			inited = true;
		}
	}

	public String getName() {
		return "curator";
	}

	public List<String> getEphemeralServiceAddress(String serviceName, String group) throws Exception {
		String path = Utils.getEphemeralServicePath(serviceName, group);
		List<String> serverList = client.getChildren(path);
		return serverList;
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
							if (delEmptyNode) {
								try {
									client.delete(servicePath);
								} catch (NoNodeException e) {
									logger.warn(env + "#Already deleted path:" + servicePath + ":" + e.getMessage());
								}
							} else {
								client.set(servicePath, "");
							}
						} else {
							logger.warn(env + "#Existing children [" + children + "] under path:" + servicePath);
							client.set(servicePath, "");
						}
					}
				}
				logger.warn(env + "#[" + this.address + "]unregistered service from " + servicePath);
			}
		} catch (Throwable e) {
			logger.error(env + "#failed to unregister service from " + servicePath, e);
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

	public boolean exists(String path) throws RegistryException {
		try {
			return client.exists(path);
		} catch (Throwable e) {
			logger.error("failed to get exists status of node: " + path, e);
			throw new RegistryException(e);
		}
	}

	public void create(String path) throws RegistryException {
		try {
			client.create(path);
		} catch (Throwable e) {
			logger.error("failed to create path: " + path, e);
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
