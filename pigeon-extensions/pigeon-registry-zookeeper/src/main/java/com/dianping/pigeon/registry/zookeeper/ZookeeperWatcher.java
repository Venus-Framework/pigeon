package com.dianping.pigeon.registry.zookeeper;

import java.util.List;

import org.apache.log4j.Logger;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;

import com.dianping.pigeon.registry.RegistryManager;
import com.dianping.pigeon.registry.exception.RegistryException;
import com.dianping.pigeon.registry.util.Constants;

public class ZookeeperWatcher implements Watcher {

	private static Logger logger = Logger.getLogger(ZookeeperWatcher.class);
	
	private static final int ADDRESS = 1;
	private static final int WEIGHT = 2;
	
	private ZookeeperRegistry zookeeperRegistry;

	public ZookeeperWatcher(ZookeeperRegistry zookeeperRegistry) {
		this.zookeeperRegistry = zookeeperRegistry;
	}

	@Override
	public void process(WatchedEvent event) {
		if(event.getType() != EventType.NodeDataChanged &&
		   event.getType() != EventType.NodeCreated &&
		   event.getType() != EventType.NodeDeleted) {
			logger.info("Event type: " + event.getType() + ", state: " + event.getState());
			return;
		}
		
		try {
			logger.info("Event type: " + event.getType() + ", path: " + event.getPath());
			PathInfo pathInfo = parsePath(event.getPath());
			if(pathInfo == null) {
				logger.warn("Failed to parse path " + event.getPath());
				return;
			}
			
			if(pathInfo.type == ADDRESS) {
				addressChanged(pathInfo);
			} else if(pathInfo.type == WEIGHT) {
				// TODO Deal with weight add & delete when auto registering
				weightChanged(pathInfo);
			}
		} catch(Exception e) {
			logger.error("Error in ZookeeperWatcher.process()", e);
			return;
		}
	}
	
	/*
	 * 1. Get newest value from ZK and watch again
	 * 2. Determine if changed against cache
	 * 3. notify if changed
	 * 4. pay attention to group fallback notification
	 */
	private void addressChanged(PathInfo pathInfo) throws Exception {
		if(shouldNotify(pathInfo)) {
			String newValue = zookeeperRegistry.getZkValue(pathInfo.path);
			logger.info("Service address changed, path " + pathInfo.path + " value " + newValue);
			List<String[]> hostDetail = zookeeperRegistry.getServiceIpPortWeight(newValue);
			zookeeperRegistry.getServiceChangeListener().onServiceHostChange(pathInfo.serviceName, hostDetail);
		} else {
			// Watch again
			zookeeperRegistry.watchZkPath(pathInfo.path);
		}
	}

	private boolean shouldNotify(PathInfo pathInfo) throws Exception {
		String currentGroup = RegistryManager.getProperty(Constants.KEY_GROUP);
		currentGroup = Utils.normalizeGroup(currentGroup);
		if(currentGroup.equals(pathInfo.group))
			return true;
		if(Utils.isEmpty(currentGroup) && !Utils.isEmpty(pathInfo.group))
			return false;
		if(!Utils.isEmpty(currentGroup) && Utils.isEmpty(pathInfo.group)) {
			String servicePath = Utils.getServicePath(pathInfo.serviceName, currentGroup);
			return zookeeperRegistry.getZkClient().exists(servicePath, true) == null;
		}
		return false;
	}
	
	private void weightChanged(PathInfo pathInfo) throws RegistryException {
		String newValue = zookeeperRegistry.getZkValue(pathInfo.path);
		logger.info("Service weight changed, path " + pathInfo.path + " value " + newValue);
		int weight = Integer.parseInt(newValue);
		zookeeperRegistry.getServiceChangeListener().onHostWeightChange(pathInfo.server, weight);
	}
	
	public PathInfo parsePath(String path) {
		if(path == null)
			return null;
		
		PathInfo pathInfo = null;
		if(path.startsWith(Constants.SERVICE_PATH)) {
			pathInfo = new PathInfo(path);
			pathInfo.type = ADDRESS;
			pathInfo.serviceName = path.substring(Constants.SERVICE_PATH.length() + 1);
			int idx = pathInfo.serviceName.indexOf(Constants.PATH_SEPARATOR);
			if(idx != -1) {
				pathInfo.group = pathInfo.serviceName.substring(idx + 1);
				pathInfo.serviceName = pathInfo.serviceName.substring(0, idx);
			}
			pathInfo.serviceName = Utils.unescapeServiceName(pathInfo.serviceName);
			pathInfo.group = Utils.normalizeGroup(pathInfo.group);
		} else if(path.startsWith(Constants.WEIGHT_PATH)) {
			pathInfo = new PathInfo(path);
			pathInfo.type = WEIGHT;
			pathInfo.server = path.substring(Constants.WEIGHT_PATH.length() + 1);
		}
		return pathInfo;
	}
	
	class PathInfo {
		String path;
		String serviceName;
		String group;
		String server;
		int type;
		
		PathInfo(String path) {
			this.path = path;
		}
	}
}
