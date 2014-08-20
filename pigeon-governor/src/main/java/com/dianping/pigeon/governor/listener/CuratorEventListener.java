package com.dianping.pigeon.governor.listener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.CuratorEvent;
import org.apache.curator.framework.api.CuratorListener;
import org.apache.log4j.Logger;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher.Event.EventType;

import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.extension.ExtensionLoader;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.registry.util.Constants;
import com.dianping.pigeon.registry.zookeeper.Utils;
import com.dianping.pigeon.util.CollectionUtils;

public class CuratorEventListener implements CuratorListener {

	private static Logger logger = LoggerLoader.getLogger(CuratorEventListener.class);

	private static final int ADDRESS = 1;
	private static final int WEIGHT = 2;
	private static final int EPHEMERAL_ADDRESS = 3;

	private ConfigManager configManager = ExtensionLoader.getExtension(ConfigManager.class);

	private CuratorRegistry registry;
	private CuratorClient client;

	private ServiceOfflineListener serviceOfflineListener = null;

	private Map<String, List<String>> ephemeralAddresses = new HashMap<String, List<String>>();

	public CuratorEventListener(CuratorRegistry registry, CuratorClient client) {
		this.registry = registry;
		this.serviceOfflineListener = registry.getServiceOfflineListener();
		this.client = client;
	}

	@Override
	public void eventReceived(CuratorFramework client, CuratorEvent curatorEvent) throws Exception {
		WatchedEvent event = (curatorEvent == null ? null : curatorEvent.getWatchedEvent());

		if (event == null
				|| (event.getType() != EventType.NodeCreated && event.getType() != EventType.NodeDeleted && event
						.getType() != EventType.NodeChildrenChanged)) {
			return;
		}

		if (logger.isInfoEnabled())
			logEvent(event);

		try {
			PathInfo pathInfo = parsePath(event.getPath());
			if (pathInfo == null) {
				logger.warn("Failed to parse path " + event.getPath());
				return;
			}

			if (pathInfo.type == EPHEMERAL_ADDRESS) {
				if (EventType.NodeCreated == event.getType()) {
					ephemeralAddressCreated(pathInfo);
				} else if (EventType.NodeDeleted == event.getType()) {
					this.client.watch(event.getPath());
				} else if (EventType.NodeChildrenChanged == event.getType()) {
					ephemeralAddressChanged(pathInfo);
				}
				String servicePath = Utils.getServicePath(pathInfo.path);
				ephemeralAddresses.put(servicePath, this.client.getChildren(servicePath));
			}
		} catch (Throwable e) {
			logger.error("Error in ZookeeperWatcher.process()", e);
			return;
		}
	}

	private void logEvent(WatchedEvent event) {
		StringBuilder sb = new StringBuilder();
		sb.append("zookeeper event received, type: ").append(event.getType()).append(", path: ")
				.append(event.getPath());
		logger.info(sb);
	}

	private void ephemeralAddressCreated(PathInfo pathInfo) throws Exception {

		String parentPath = Utils.getEphemeralServicePath(pathInfo.serviceName, pathInfo.group);
		client.watchChildren(parentPath);
	}

	private void ephemeralAddressChanged(PathInfo pathInfo) throws Exception {
		List<String> lastChildren = ephemeralAddresses.get(pathInfo.path);
		List<String> children = client.getChildren(pathInfo.path);
		List<String> removed = new ArrayList<String>();
		if (!CollectionUtils.isEmpty(lastChildren)) {
			removed.addAll(lastChildren);
		}
		if (!CollectionUtils.isEmpty(children)) {
			removed.removeAll(children);
		}
		logger.info("ephemeral service address changed, path " + pathInfo.path + ", removed:" + removed);
		for (String host : removed) {
			serviceOfflineListener.offline(pathInfo.serviceName, host, pathInfo.group);
		}
		String parentPath = Utils.getEphemeralServicePath(pathInfo.serviceName, pathInfo.group);
		client.watchChildren(parentPath);
	}

	public PathInfo parsePath(String path) {
		if (path == null)
			return null;

		PathInfo pathInfo = null;
		if (path.startsWith(Constants.SERVICE_PATH)) {
			pathInfo = new PathInfo(path);
			pathInfo.type = ADDRESS;
			pathInfo.serviceName = path.substring(Constants.SERVICE_PATH.length() + 1);
			int idx = pathInfo.serviceName.indexOf(Constants.PATH_SEPARATOR);
			if (idx != -1) {
				pathInfo.group = pathInfo.serviceName.substring(idx + 1);
				pathInfo.serviceName = pathInfo.serviceName.substring(0, idx);
			}
			pathInfo.serviceName = Utils.unescapeServiceName(pathInfo.serviceName);
			pathInfo.group = Utils.normalizeGroup(pathInfo.group);
		} else if (path.startsWith(Constants.WEIGHT_PATH)) {
			pathInfo = new PathInfo(path);
			pathInfo.type = WEIGHT;
			pathInfo.server = path.substring(Constants.WEIGHT_PATH.length() + 1);
		} else if (path.startsWith(Constants.EPHEMERAL_SERVICE_PATH)) {
			pathInfo = new PathInfo(path);
			pathInfo.type = EPHEMERAL_ADDRESS;
			pathInfo.serviceName = path.substring(Constants.EPHEMERAL_SERVICE_PATH.length() + 1);
			int idx = pathInfo.serviceName.indexOf('@');
			if (idx != -1) {
				pathInfo.group = pathInfo.serviceName.substring(idx + 1);
				pathInfo.serviceName = pathInfo.serviceName.substring(0, idx);
			}
			pathInfo.serviceName = Utils.unescapeServiceName(pathInfo.serviceName);
			pathInfo.group = Utils.normalizeGroup(pathInfo.group);
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
