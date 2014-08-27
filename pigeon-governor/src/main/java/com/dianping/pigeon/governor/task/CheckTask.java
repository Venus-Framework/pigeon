package com.dianping.pigeon.governor.task;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.dianping.dpsf.exception.NetTimeoutException;
import com.dianping.dpsf.protocol.DefaultRequest;
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

	private static final Logger logger = Logger.getLogger(CheckTask.class);

	private HealthCheckManager manager;
	private Host host;

	public CheckTask(HealthCheckManager manager, Host host) {
		this.manager = manager;
		this.host = host;
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
			} else {
				host.setAlive(false);
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

	private boolean isServerAlive() {
		boolean alive = false;
		try {
			if (!isPortAvailable()) {
				return false;
			}
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
			logger.error("server " + host + " timeout, dead count " + host.getDeadCount() + ": " + e.getMessage());
			alive = false;
		} catch (Throwable t) {
			logger.error("error contacting server " + host + ", dead count " + host.getDeadCount(), t);
			alive = false;
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
