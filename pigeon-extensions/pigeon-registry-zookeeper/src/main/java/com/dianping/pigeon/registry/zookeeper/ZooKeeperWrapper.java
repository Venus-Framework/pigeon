/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.registry.zookeeper;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.KeeperException.ConnectionLossException;
import org.apache.zookeeper.KeeperException.SessionExpiredException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Stat;

import com.dianping.pigeon.log.LoggerLoader;

public class ZooKeeperWrapper {

	private static Logger logger = LoggerLoader.getLogger(ZooKeeperWrapper.class);
	private ZooKeeper zk;

	private String addresses;
	private int timeout;
	private Watcher watcher;

	private Map<String, Watcher> watcherMap = new ConcurrentHashMap<String, Watcher>();

	protected ZooKeeperWrapper() {
	}

	public ZooKeeperWrapper(String addresses, int timeout, Watcher watcher) throws IOException, InterruptedException {
		this.addresses = addresses;
		this.timeout = timeout;
		this.watcher = watcher;
		WatcherWrapper watcherWrapper = new WatcherWrapper(watcher);
		this.zk = new ZooKeeper(addresses, timeout, watcherWrapper);
		watcherWrapper.waitUntilConnected();
	}

	private synchronized void init(ZooKeeper zk) throws IOException, KeeperException, InterruptedException {
		if (zk == this.zk) {
			WatcherWrapper watcherWrapper = new WatcherWrapper(watcher);
			this.zk = new ZooKeeper(addresses, timeout, watcherWrapper);
			watcherWrapper.waitUntilConnected();

			for (Entry<String, Watcher> entry : watcherMap.entrySet()) {
				this.zk.getData(entry.getKey(), entry.getValue(), null);
			}
		}
	}

	public Stat exists(String path, boolean watch) throws KeeperException, InterruptedException, IOException {
		Stat stat = null;
		ZooKeeper zk_ = this.zk;
		try {
			stat = zk_.exists(path, watch);
		} catch (SessionExpiredException see) {
			logger.error("OP:exists1---" + see.getMessage(), see);
			zk_.close();
			init(zk_);
			stat = this.zk.exists(path, watch);
		} catch (ConnectionLossException cle) {
			logger.error("OP:exists1---" + cle.getMessage(), cle);
			zk_.close();
			init(zk_);
			stat = this.zk.exists(path, watch);
		}
		return stat;
	}

	public Stat exists(String path, Watcher watcher) throws KeeperException, InterruptedException, IOException {
		Stat stat = null;
		ZooKeeper zk_ = this.zk;
		try {
			stat = zk_.exists(path, watcher);
			if (watcher != null)
				this.watcherMap.put(path, watcher);
		} catch (SessionExpiredException see) {
			logger.error("OP:exists2---" + see.getMessage(), see);
			zk_.close();
			init(zk_);
			stat = this.zk.exists(path, watcher);
		} catch (ConnectionLossException cle) {
			logger.error("OP:exists2---" + cle.getMessage(), cle);
			zk_.close();
			init(zk_);
			stat = this.zk.exists(path, watcher);
		}
		return stat;
	}

	public String createIfNotExisted(String path, byte[] data, List<ACL> acl, CreateMode createMode)
			throws KeeperException, InterruptedException, IOException {
		String[] pathArray = path.split("/");
		StringBuilder pathStr = new StringBuilder();
		for (int i = 0; i < pathArray.length - 1; i++) {
			String p = pathArray[i];
			if (StringUtils.isNotBlank(p)) {
				pathStr.append("/").append(p);
				if (exists(pathStr.toString(), false) == null) {
					create(pathStr.toString(), new byte[0], Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
				}
			}
		}
		return create(path, data, acl, createMode);
	}

	public String create(String path, byte[] data, List<ACL> acl, CreateMode createMode) throws KeeperException,
			InterruptedException, IOException {
		ZooKeeper zk_ = this.zk;
		try {
			return zk_.create(path, data, acl, createMode);
		} catch (SessionExpiredException see) {
			logger.error("OP:create---" + see.getMessage(), see);
			zk_.close();
			init(zk_);
			return this.zk.create(path, data, acl, createMode);
		} catch (ConnectionLossException cle) {
			logger.error("OP:create---" + cle.getMessage(), cle);
			zk_.close();
			init(zk_);
			return this.zk.create(path, data, acl, createMode);
		}
	}

	public boolean updateData(String path, String data) throws IOException, KeeperException, InterruptedException {
		byte[] bytes = data.getBytes("UTF-8");
		if (exists(path, false) == null) {
			createIfNotExisted(path, bytes, Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
		} else {
			setData(path, bytes, -1);
		}
		return false;
	}

	public byte[] getData(String path, Watcher watcher, Stat stat) throws KeeperException, InterruptedException,
			IOException {
		ZooKeeper zk_ = this.zk;
		try {
			byte[] data = zk_.getData(path, watcher, stat);
			if (watcher != null) {
				this.watcherMap.put(path, watcher);
			}
			return data;
		} catch (SessionExpiredException see) {
			logger.error("OP:getData1---" + see.getMessage(), see);
			zk_.close();
			init(zk_);
			return this.zk.getData(path, watcher, stat);
		} catch (ConnectionLossException cle) {
			logger.error("OP:getData1---" + cle.getMessage(), cle);
			zk_.close();
			init(zk_);
			return this.zk.getData(path, watcher, stat);
		}
	}

	public byte[] getData(String path, boolean watch, Stat stat) throws KeeperException, InterruptedException,
			IOException {
		ZooKeeper zk_ = this.zk;
		try {
			return zk_.getData(path, watch, stat);
		} catch (SessionExpiredException see) {
			logger.error("OP:getData2---" + see.getMessage(), see);
			zk_.close();
			init(zk_);
			return this.zk.getData(path, watch, stat);
		} catch (ConnectionLossException cle) {
			logger.error("OP:getData2---" + cle.getMessage(), cle);
			zk_.close();
			init(zk_);
			return this.zk.getData(path, watch, stat);
		}
	}

	public Stat setData(String path, byte[] data, int version) throws KeeperException, InterruptedException,
			IOException {
		ZooKeeper zk_ = this.zk;
		try {
			return zk_.setData(path, data, version);
		} catch (SessionExpiredException see) {
			logger.error("OP:setData---" + see.getMessage(), see);
			zk_.close();
			init(zk_);
			return this.zk.setData(path, data, version);
		} catch (ConnectionLossException cle) {
			logger.error("OP:setData---" + cle.getMessage(), cle);
			zk_.close();
			init(zk_);
			return this.zk.setData(path, data, version);
		}
	}

	public List<String> getChildren(String path, boolean watch) throws KeeperException, InterruptedException,
			IOException {
		ZooKeeper zk_ = this.zk;
		try {
			return zk_.getChildren(path, watch);
		} catch (SessionExpiredException see) {
			logger.error("OP:getChildren---" + see.getMessage(), see);
			zk_.close();
			init(zk_);
			return this.zk.getChildren(path, watch);
		} catch (ConnectionLossException cle) {
			logger.error("OP:getChildren---" + cle.getMessage(), cle);
			zk_.close();
			init(zk_);
			return this.zk.getChildren(path, watch);
		}
	}

	public void delete(String path, int version) throws KeeperException, InterruptedException, IOException {
		ZooKeeper zk_ = this.zk;
		try {
			zk_.delete(path, version);
		} catch (SessionExpiredException see) {
			logger.error("OP:delete---" + see.getMessage(), see);
			zk_.close();
			init(zk_);
			this.zk.delete(path, version);
		} catch (ConnectionLossException cle) {
			logger.error("OP:delete---" + cle.getMessage(), cle);
			zk_.close();
			init(zk_);
			this.zk.delete(path, version);
		}
	}

	public void removeWatcher(String path) {
		this.watcherMap.remove(path);
	}

	public synchronized void sessionExpiredReConnect() throws IOException, KeeperException, InterruptedException {
		logger.info("Session Expired Reconnect!");
		ZooKeeper zk_ = this.zk;
		zk_.close();
		init(zk_);
	}

	/**
	 * @return the addresses
	 */
	public String getAddresses() {
		return addresses;
	}

	public class WatcherWrapper implements Watcher {

		private CountDownLatch latch;
		private Watcher wrappedWatcher;

		public WatcherWrapper(Watcher watcher) {
			this.latch = new CountDownLatch(1);
			this.wrappedWatcher = watcher;
		}

		@Override
		public void process(WatchedEvent event) {
			if (event.getState() == KeeperState.SyncConnected) {
				logger.info("Zookeeper connected");
				latch.countDown();
				return;
			}

			if (wrappedWatcher != null) {
				wrappedWatcher.process(event);
			}
		}

		public void waitUntilConnected() throws IOException, InterruptedException {
			if (!latch.await(30, TimeUnit.SECONDS)) {
				throw new IOException("Timeout while connecting to zookeeper");
			}
		}
	}

}
