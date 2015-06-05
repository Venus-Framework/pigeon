package com.dianping.pigeon.test.benchmark.message.zookeeper;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.CuratorEvent;
import org.apache.curator.framework.api.CuratorListener;
import com.dianping.pigeon.log.LoggerLoader;
import org.apache.logging.log4j.Logger;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher.Event.EventType;

import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.extension.ExtensionLoader;
import com.dianping.pigeon.registry.exception.RegistryException;
import com.dianping.pigeon.test.benchmark.status.StatusHolder;

public class CuratorEventListener implements CuratorListener {

	private static Logger logger = LoggerLoader.getLogger(CuratorEventListener.class);

	private static final int CATEGORY = 1;

	private ConfigManager configManager = ExtensionLoader.getExtension(ConfigManager.class);

	private CuratorClient client;

	public CuratorEventListener(CuratorClient client) {
		this.client = client;
	}

	@Override
	public void eventReceived(CuratorFramework client, CuratorEvent curatorEvent) throws Exception {
		WatchedEvent event = (curatorEvent == null ? null : curatorEvent.getWatchedEvent());

		if (event == null
				|| (event.getType() != EventType.NodeCreated && event.getType() != EventType.NodeDataChanged
						&& event.getType() != EventType.NodeDeleted && event.getType() != EventType.NodeChildrenChanged)) {
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

			if (pathInfo.type == CATEGORY) {
				categoryChanged(pathInfo);
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

	private void categoryChanged(PathInfo pathInfo) throws RegistryException {
		try {
			String value = client.get(pathInfo.path);
			logger.info("[" + Thread.currentThread().getName() + "]category changed, path " + pathInfo.path + " value "
					+ value);
			// client.watch(pathInfo.path);
		} catch (Exception e) {
			throw new RegistryException(e);
		} finally {
		}
	}

	public PathInfo parsePath(String path) {
		if (path == null)
			return null;

		PathInfo pathInfo = null;
		if (path.startsWith(ZookeeperTestService.CATEGORY_PATH)) {
			pathInfo = new PathInfo(path);
			pathInfo.type = CATEGORY;
		}
		return pathInfo;
	}

	class PathInfo {
		String path;
		int type;

		PathInfo(String path) {
			this.path = path;
		}
	}

}
