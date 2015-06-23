package com.dianping.pigeon.test.benchmark.message.zookeeper;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.retry.RetryNTimes;
import com.dianping.pigeon.log.LoggerLoader;
import org.apache.logging.log4j.Logger;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.data.Stat;

import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.extension.ExtensionLoader;

public class CuratorClient {

	private static final String CHARSET = "UTF-8";

	private static Logger logger = LoggerLoader.getLogger(CuratorClient.class);

	private ConfigManager configManager = ExtensionLoader.getExtension(ConfigManager.class);

	private CuratorFramework client;

	private int retries = configManager.getIntValue("messageserver.zookeeper.curator.retries", Integer.MAX_VALUE);

	private int retryInterval = configManager.getIntValue("messageserver.zookeeper.curator.retryinterval", 1000);

	private int sessionTimeout = configManager.getIntValue("messageserver.zookeeper.curator.sessiontimeout", 30 * 1000);

	private int connectionTimeout = configManager.getIntValue("messageserver.zookeeper.curator.connectiontimeout",
			15 * 1000);

	public CuratorClient(String zkAddress) throws Exception {
		client = CuratorFrameworkFactory.newClient(zkAddress, sessionTimeout, connectionTimeout, new RetryNTimes(
				retries, retryInterval));
		client.getConnectionStateListenable().addListener(new ConnectionStateListener() {
			@Override
			public void stateChanged(CuratorFramework client, ConnectionState newState) {
				logger.info("zookeeper state changed to " + newState);
				if (newState == ConnectionState.LOST) {
					while (true) {
						try {
							if (client.getZookeeperClient().blockUntilConnectedOrTimedOut()) {
								break;
							}
						} catch (InterruptedException e) {
							break;
						} catch (Exception e) {
							logger.error("error with zookeeper connection:" + e.getMessage());
						}
					}
				} else if (newState == ConnectionState.RECONNECTED) {
					// TODO
				}
			}
		});
		client.getCuratorListenable().addListener(new CuratorEventListener(this));
		client.start();
		client.getZookeeperClient().blockUntilConnectedOrTimedOut();
	}

	public String get(String path) throws Exception {
		try {
			if (exists(path)) {
				byte[] bytes = client.getData().watched().forPath(path);
				String value = new String(bytes, CHARSET);
				if (logger.isDebugEnabled()) {
					logger.debug("get value of node " + path + ", value " + value);
				}
				return value;
			} else {
				if (logger.isDebugEnabled()) {
					logger.debug("node " + path + " does not exist");
				}
				return null;
			}
		} finally {
		}
	}

	public void set(String path, Object value) throws Exception {
		try {
			byte[] bytes = (value == null ? new byte[0] : value.toString().getBytes(CHARSET));
			if (exists(path)) {
				client.setData().forPath(path, bytes);
				if (logger.isInfoEnabled()) {
					logger.info("set value of node " + path + " to " + value);
				}
			} else {
				client.create().creatingParentsIfNeeded().forPath(path, bytes);
				if (logger.isInfoEnabled()) {
					logger.info("create node " + path + " value " + value);
				}
			}
		} finally {
		}
	}

	public void create(String path) throws Exception {
		create(path, null);
	}

	public void create(String path, Object value) throws Exception {
		byte[] bytes = (value == null ? new byte[0] : value.toString().getBytes(CHARSET));
		client.create().creatingParentsIfNeeded().forPath(path, bytes);
		if (logger.isInfoEnabled()) {
			logger.info("create node " + path + " value " + value);
		}
	}

	public void createEphemeral(String path, String value) throws Exception {
		byte[] bytes = (value == null ? new byte[0] : value.toString().getBytes(CHARSET));
		client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(path, bytes);
		if (logger.isInfoEnabled()) {
			logger.info("create ephemeral node " + path + " value " + value);
		}
	}

	public void createEphemeral(String path) throws Exception {
		createEphemeral(path, null);
	}

	public boolean exists(String path) throws Exception {
		Stat stat = client.checkExists().watched().forPath(path);
		return stat != null;
	}

	public boolean exists(String path, boolean watch) throws Exception {
		Stat stat = watch ? client.checkExists().watched().forPath(path) : client.checkExists().forPath(path);
		return stat != null;
	}

	public List<String> getChildren(String path) throws Exception {
		try {
			List<String> children = client.getChildren().watched().forPath(path);
			if (logger.isDebugEnabled()) {
				logger.debug("get children of node " + path + ": " + StringUtils.join(children.iterator(), ','));
			}
			return children;
		} catch (KeeperException.NoNodeException e) {
			logger.debug("node " + path + " does not exist");
			return null;
		}
	}

	public void delete(String path) throws Exception {
		client.delete().forPath(path);
		if (logger.isInfoEnabled()) {
			logger.info("delete node " + path);
		}
	}

	public void watch(String path) throws Exception {
		client.checkExists().watched().forPath(path);
	}

	public void watchChildren(String path) throws Exception {
		if (exists(path))
			client.getChildren().watched().forPath(path);
	}

	public void close() {
		if (client != null) {
			client.close();
			client = null;
		}
	}

}
