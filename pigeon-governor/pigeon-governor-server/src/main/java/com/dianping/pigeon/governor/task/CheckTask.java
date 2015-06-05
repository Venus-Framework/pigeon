package com.dianping.pigeon.governor.task;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import com.dianping.pigeon.log.LoggerLoader;
import org.apache.logging.log4j.Logger;

import com.dianping.dpsf.exception.NetTimeoutException;
import com.dianping.dpsf.protocol.DefaultRequest;
import com.dianping.pigeon.config.ConfigChangeListener;
import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.config.ConfigManagerLoader;
import com.dianping.pigeon.governor.util.Constants.Host;
import com.dianping.pigeon.remoting.common.codec.SerializerFactory;
import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.invoker.domain.CallbackFuture;
import com.dianping.pigeon.remoting.invoker.domain.ConnectInfo;
import com.dianping.pigeon.remoting.invoker.util.InvokerUtils;
import com.dianping.pigeon.remoting.netty.invoker.NettyClient;

public class CheckTask implements Runnable {

	private static final Logger logger = LoggerLoader.getLogger(CheckTask.class);

	private HealthCheckManager manager;
	private Host host;
	private static ConfigManager configManager = ConfigManagerLoader.getConfigManager();
	private static boolean logDeadServer = configManager.getBooleanValue("pigeon.governor.healthcheck.logdead", false);
	private static boolean logAliveServer = configManager
			.getBooleanValue("pigeon.governor.healthcheck.logalive", false);
	private static boolean checkAliveByResponse = configManager.getBooleanValue(
			"pigeon.governor.healthcheck.checkalivebyresponse", false);

	public CheckTask(HealthCheckManager manager, Host host) {
		this.manager = manager;
		this.host = host;
		configManager.registerConfigChangeListener(new ConfigChangeHandler());
	}

	class ConfigChangeHandler implements ConfigChangeListener {

		@Override
		public void onKeyUpdated(String key, String value) {
			if ("pigeon.governor.healthcheck.logdead".equals(key)) {
				logDeadServer = Boolean.parseBoolean(value);
			} else if ("pigeon.governor.healthcheck.logalive".equals(key)) {
				logAliveServer = Boolean.parseBoolean(value);
			} else if ("pigeon.governor.healthcheck.checkalivebyresponse".equals(key)) {
				checkAliveByResponse = Boolean.parseBoolean(value);
			}
		}

		@Override
		public void onKeyAdded(String key, String value) {
		}

		@Override
		public void onKeyRemoved(String key) {
		}

	}

	@Override
	public void run() {
		try {
			checkServer();
		} catch (InterruptedException e) {
			logger.warn("CheckTask is interrupted");
		} catch (Throwable e) {
			logger.error("", e);
		}
	}

	private void checkServer() throws InterruptedException {
		if (System.currentTimeMillis() - host.getLastCheckTime() > manager.getHostInterval()) {
			if (isServerAlive()) {
				host.setAlive(true);
				if (logAliveServer) {
					logger.info("alive:" + host);
				}
			} else {
				host.setAlive(false);
				if (logDeadServer) {
					logger.info("dead:" + host);
				}
				if (host.getDeadCount() < Integer.MAX_VALUE)
					host.increaseDeadCount();
			}
			host.updateCheckTime();
			manager.getResultQueue().add(this);
		} else {
			manager.getWorkerPool().submit(this);
			Thread.sleep(20);
		}
	}

	public boolean isValidAddress() {
		String invalidAddress = manager.getInvalidAddress(host.getService().getEnv());
		if (!StringUtils.isBlank(invalidAddress)) {
			String[] addressArray = invalidAddress.split("[|]");
			for (String addr : addressArray) {
				String prefix = addr;
				if (!prefix.endsWith(".")) {
					prefix = addr + ".";
				}
				if (host.getAddress().startsWith(prefix)) {
					return false;
				}
			}
		}
		return true;
	}

	public boolean isServerAlive() {
		boolean alive = false;
		if (!isValidAddress()) {
			return false;
		}
		if (!isPortAvailable()) {
			return false;
		}
		if (host.getService().getUrl().startsWith("@HTTP@")) {
			return true;
		}
		if (!host.isCheckResponse()) {
			return true;
		}
		if (checkAliveByResponse) {
			try {
				InvocationRequest request = createHealthCheckRequest();
				InvocationResponse response = getHealthCheckResponse(request);
				if (response != null) {
					alive = true;
					// if response.getReturn() is an exception, consider server
					// alive
					if (response.getReturn() instanceof Map) {
						Map result = (Map) response.getReturn();
						if (result != null) {
							if (logger.isDebugEnabled())
								logger.debug("server " + host + " response: " + result);

							// If group does not match, remove immediately
							if (result.containsKey("group")
									&& !isSameGroup(host.getService().getGroup(), (String) result.get("group"))) {
								alive = false;
								host.setDeadCount(Integer.MAX_VALUE);
							}
						}
					}
				}
			} catch (NetTimeoutException e) {
				logger.debug("server " + host + " timeout, dead count " + host.getDeadCount() + ": " + e.getMessage());
				alive = true;
			} catch (Throwable t) {
				logger.debug("error contacting server " + host + ", dead count " + host.getDeadCount(), t);
				alive = true;
			}
		} else {
			alive = true;
		}
		return alive;
	}

	private boolean isPortAvailable() {
		Socket socket = null;
		try {
			socket = new Socket();
			socket.setReuseAddress(true);
			SocketAddress sa = new InetSocketAddress(host.getIp(), host.getPort());
			socket.connect(sa, 3000);
			return true;
		} catch (IOException e) {
			return false;
		} finally {
			if (socket != null) {
				try {
					socket.close();
				} catch (IOException e) {
				}
			}
		}
	}

	private boolean isSameGroup(String group1, String group2) {
		if (StringUtils.isEmpty(group1))
			return StringUtils.isEmpty(group2);
		else
			return group1.equals(group2);
	}

	private InvocationRequest createHealthCheckRequest() {
		InvocationRequest request = new DefaultRequest("", "", null, SerializerFactory.SERIALIZE_HESSIAN,
				Constants.MESSAGE_TYPE_HEALTHCHECK, 3000, null);
		request.setSequence(1);
		request.setCreateMillisTime(System.currentTimeMillis());
		request.setCallType(Constants.CALLTYPE_REPLY);
		return request;
	}

	private InvocationResponse getHealthCheckResponse(InvocationRequest request) throws InterruptedException {
		NettyClient client = null;
		try {
			ConnectInfo connectInfo = new ConnectInfo(host.getService().getUrl(), host.getIp(), host.getPort(), 1);
			client = new NettyClient(connectInfo);
			client.connect();
			CallbackFuture future = new CallbackFuture();
			future.setRequest(request);
			future.setClient(client);
			InvokerUtils.sendRequest(client, request, future);
			InvocationResponse response = future.get(request.getTimeout());
			return response;
		} finally {
			try {
				client.close();
			} catch (Throwable t) {
			}
		}
	}

	public Host getHost() {
		return this.host;
	}

}
