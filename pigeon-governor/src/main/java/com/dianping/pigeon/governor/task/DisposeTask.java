package com.dianping.pigeon.governor.task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.dianping.pigeon.governor.util.Constants.Action;
import com.dianping.pigeon.governor.util.Constants.Host;
import com.dianping.pigeon.registry.exception.RegistryException;
import com.dianping.pigeon.registry.util.Constants;
import com.dianping.pigeon.registry.zookeeper.CuratorClient;
import com.dianping.pigeon.registry.zookeeper.CuratorRegistry;
import com.dianping.pigeon.registry.zookeeper.Utils;

public class DisposeTask implements Runnable {

	private static final Logger logger = Logger.getLogger(DisposeTask.class);

	private HealthCheckManager manager;

	private HttpClient httpClient;

	public DisposeTask(HealthCheckManager manager) {
		this.manager = manager;
	}

	@Override
	public void run() {
		while (!Thread.interrupted()) {
			CheckTask result;
			try {
				result = manager.getResultQueue().take();
				processResult(result);
			} catch (RegistryException e) {
				logger.error("", e);
			} catch (InterruptedException e) {
				logger.warn("DisposeTask is interrupted", e);
			} catch (Throwable e) {
				logger.error("", e);
			}
		}
	}

	private void processResult(CheckTask task) throws RegistryException, InterruptedException {
		if (task == null)
			return;
		if (task.getHost().isAlive())
			return;

		if (task.getHost().getDeadCount() >= manager.getDeadThreshold()) {
			disposeAddress(task);
		} else {
			checkAgain(task);
		}
	}

	private void disposeAddress(CheckTask result) throws RegistryException, InterruptedException {
		switch (determineDisposeAction(result.getHost())) {
		case log:
			logAddress(result);
			return;
		case remove:
			removeAddress(result);
			return;
		case wait:
			Thread.sleep(20);
			manager.getResultQueue().add(result);
			return;
		case keep:
			if (logger.isDebugEnabled())
				logger.debug("keep dead server " + result.getHost());
			return;
		default:
			return;
		}
	}

	private Action determineDisposeAction(Host host) {
		int n = canRemoveHost(host.getService().getHostList(), host);
		if (n == 0)
			return Action.wait;
		if (n > 0)
			return manager.getAction(host.getService().getEnv());
		return Action.keep;
	}

	private String getApp(Host host) throws Exception {
		CuratorRegistry registry = (CuratorRegistry) manager.getRegistry(host.getService().getEnv());
		CuratorClient client = registry.getCuratorClient();
		String app = client.get(Constants.APP_PATH + Constants.PATH_SEPARATOR + host.getAddress());
		return app;
	}

	private boolean existsSameApp(List<Host> hostList, Host host) throws Exception {
		String app = getApp(host);
		if (StringUtils.isBlank(app)) {
			return false;
		} else {
			for (Host h : hostList) {
				if (!h.getAddress().equals(host.getAddress())) {
					String appOfHost = getApp(h);
					if (!StringUtils.isBlank(app) && app.equals(appOfHost)) {
						logger.info("will not be deleted, same app:" + app + " with other servers, dead server " + host
								+ ", in " + hostList);
						return true;
					}
				}
			}
			return false;
		}
	}

	/*
	 * 1. will not remove if only 1 host exists 2. will not remove if all hosts
	 * are dead 3. will remove if at least one host is alive
	 */
	private int canRemoveHost(List<Host> hostList, Host host) {
		int minHosts = manager.getMinhosts(host.getService().getEnv());
		if (minHosts > 0 && hostList.size() <= minHosts) {
			logger.info("will not be deleted, dead server " + host + ", in " + hostList);
			return -1;
		}
		boolean isChecking = false;
		if (host.getDeadCount() < manager.getDeadThreshold())
			isChecking = true;
		if (!isChecking) {
			int aliveCount = 0;
			for (Host h : hostList) {
				CheckTask t = new CheckTask(manager, h);
				if (t.isServerAlive()) {
					aliveCount++;
				}
			}
			if (minHosts > 0 && aliveCount < minHosts) {
				logger.info("will not be deleted, alive count:" + aliveCount + ", dead server " + host + ", in "
						+ hostList);
				return 0;
			}
			try {
				boolean exists = existsSameApp(hostList, host);
				if (exists) {
					return 0;
				}
			} catch (Exception e) {
			}
			return 1;
		}
		return 0;
	}

	private void logAddress(CheckTask task) {
		logger.info("log dead server " + task.getHost());
	}

	private void removeAddress(CheckTask task) throws RegistryException {
		Host host = task.getHost();
		int minHosts = manager.getMinhosts(host.getService().getEnv());
		CuratorRegistry registry = (CuratorRegistry) manager.getRegistry(host.getService().getEnv());
		boolean isSuccess = unregisterPersistentNode(host, registry.getCuratorClient(), minHosts);
		if (isSuccess) {
			host.getService().getHostList().remove(host);
			if (!host.getService().getUrl().startsWith("@HTTP@")) {
				notifyLionApi(task);
			}
		}
	}

	public boolean unregisterPersistentNode(Host host, CuratorClient client, int minHosts) throws RegistryException {
		String serviceName = host.getService().getUrl();
		String group = host.getService().getGroup();
		String serviceAddress = host.getAddress();
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
					if (minHosts > 0 && addressList.size() <= minHosts) {
						logger.info("will not be deleted, dead server " + serviceAddress + ", in " + addressList);
						return false;
					}
					if (!addressList.isEmpty()) {
						Collections.sort(addressList);
						client.set(servicePath, StringUtils.join(addressList.iterator(), ","));
						if (logger.isInfoEnabled()) {
							logger.info("unregistered:" + host);
						}
						return true;
					}
				}
			}
			return false;
		} catch (Throwable e) {
			logger.error("failed to unregister service from " + servicePath, e);
			throw new RegistryException(e);
		}
	}

	private void notifyLionApi(CheckTask task) {
		String url = generateUrl(task);
		try {
			String message = doHttpGet(url);
			if (message.startsWith("0|")) {
				logger.info("removed:" + task.getHost());
			} else {
				logger.error("failed to remove:" + task.getHost() + ", message: " + message);
			}
		} catch (IOException e) {
			logger.error("failed to remove:" + task.getHost(), e);
		}
	}

	private void checkAgain(CheckTask task) {
		manager.getWorkerPool().submit(task);
	}

	private String doHttpGet(String url) throws IOException {
		GetMethod get = new GetMethod(url);
		HttpClient httpClient = getHttpClient();
		try {
			httpClient.executeMethod(get);
			return get.getResponseBodyAsString();
		} finally {
			get.releaseConnection();
		}
	}

	private String generateUrl(CheckTask task) {
		Host host = task.getHost();
		StringBuilder sb = new StringBuilder("http://lionapi.dp:8080/service/unpublish?id=3");
		sb.append('&').append("env=").append(host.getService().getEnv().name());
		sb.append('&').append("service=").append(host.getService().getUrl());
		if (StringUtils.isNotBlank(host.getService().getGroup()))
			sb.append('&').append("group=").append(host.getService().getGroup());
		sb.append('&').append("ip=").append(host.getIp());
		sb.append('&').append("port=").append(host.getPort());
		sb.append('&').append("updatezk=false");
		return sb.toString();
	}

	private HttpClient getHttpClient() {
		if (httpClient == null) {
			HttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
			HttpConnectionManagerParams params = new HttpConnectionManagerParams();
			params.setMaxTotalConnections(500);
			params.setDefaultMaxConnectionsPerHost(10);
			params.setConnectionTimeout(3000);
			params.setTcpNoDelay(true);
			params.setSoTimeout(3000);
			params.setStaleCheckingEnabled(true);
			connectionManager.setParams(params);

			httpClient = new HttpClient(connectionManager);
		}
		return httpClient;
	}

}
