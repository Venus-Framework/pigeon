/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.invoker.listener;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.dianping.dpsf.protocol.DefaultRequest;
import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.domain.HostInfo;
import com.dianping.pigeon.extension.ExtensionLoader;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.registry.RegistryManager;
import com.dianping.pigeon.remoting.common.codec.SerializerFactory;
import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.invoker.Client;
import com.dianping.pigeon.remoting.invoker.ClientManager;
import com.dianping.pigeon.remoting.invoker.domain.ConnectInfo;
import com.dianping.pigeon.remoting.invoker.util.InvokerUtils;

public class HeartBeatListener implements Runnable, ClusterListener {

	private static final Logger logger = LoggerLoader.getLogger(HeartBeatListener.class);

	public static final String HEART_TASK_SERVICE = "http://service.dianping.com/piegonService/heartTaskService";

	public static final String HEART_TASK_METHOD = "heartBeat";

	private Map<String, List<Client>> workingClients;

	private static AtomicLong heartBeatSeq = new AtomicLong();

	private static ConcurrentMap<String, HeartBeatStat> heartBeatStats = new ConcurrentHashMap<String, HeartBeatStat>();

	private final ClusterListenerManager clusterListenerManager = ClusterListenerManager.getInstance();

	private ConfigManager configManager = ExtensionLoader.getExtension(ConfigManager.class);

	long heartBeatDeadCount = configManager.getLongValue(Constants.KEY_HEARTBEAT_DEADTHRESHOLD,
			Constants.DEFAULT_HEARTBEAT_DEADCOUNT);
	long heartBeatHealthCount = configManager.getLongValue(Constants.KEY_HEARTBEAT_HEALTHTHRESHOLD,
			Constants.DEFAULT_HEARTBEAT_HEALTHCOUNT);
	boolean isHeartBeatAutoPickOff = configManager.getBooleanValue(Constants.KEY_HEARTBEAT_AUTOPICKOFF,
			Constants.DEFAULT_HEARTBEAT_AUTOPICKOFF);
	String serviceNameSpace = configManager.getStringValue(Constants.KEY_SERVICE_NAMESPACE,
			Constants.DEFAULT_SERVICE_NAMESPACE);
	long interval = configManager.getLongValue(Constants.KEY_HEARTBEAT_INTERVAL, Constants.DEFAULT_HEARTBEAT_INTERVAL);
	long heartBeatTimeout = configManager.getLongValue(Constants.KEY_HEARTBEAT_TIMEOUT,
			Constants.DEFAULT_HEARTBEAT_TIMEOUT);

	public void run() {
		long sleepTime = interval;
		while (!Thread.currentThread().isInterrupted()) {
			try {
				Thread.sleep(sleepTime);
				long now = System.currentTimeMillis();
				// 检查正在工作的Clients是否完好
				if (this.getWorkingClients() != null) {
					Set<Client> clients = new HashSet<Client>();
					for (Entry<String, List<Client>> entry : this.getWorkingClients().entrySet()) {
						List<Client> clientList = entry.getValue();
						if (clientList != null) {
							clients.addAll(clientList);
						}
					}
					for (Client client : clients) {
						if (logger.isDebugEnabled()) {
							logger.debug("[heartbeat] checking service provider:" + client);
						}
						if (RegistryManager.getInstance().getServiceWeight(client.getAddress()) > 0) {
							if (client.isConnected()) {
								String connect = client.getAddress();
								if (!hasHeartBeatRequestExists(connect)) {
									sendHeartBeatRequest(client);
								} else {
									HeartBeatStat heartBeatStat = heartBeatStats.get(connect);
									InvocationRequest heartRequest = heartBeatStat.currentHeartRequest;
									if (isHeartRequestTimeout(heartRequest, heartBeatTimeout)) {
										heartBeatStat.incrFailed();
										notifyHeartBeatStatChanged(client);
										sendHeartBeatRequest(client);
									}
								}
							} else {
								logger.error("[heartbeat] remove connect:" + client.getAddress());
								clusterListenerManager.removeConnect(client);
							}
						}
					}
				}
				sleepTime = interval - (System.currentTimeMillis() - now);
			} catch (Exception e) {
				logger.error("[heartbeat] task failed", e);
			} finally {
				if (sleepTime < 1000) {
					sleepTime = 1000;
				}
			}
		}
	}

	private boolean hasHeartBeatRequestExists(String connect) {
		if (connect != null) {
			HeartBeatStat heartBeatStat = heartBeatStats.get(connect);
			return heartBeatStat != null && heartBeatStat.currentHeartRequest != null;
		}
		return false;
	}

	private boolean isHeartRequestTimeout(InvocationRequest heartRequest, long heartBeatTimeout) {
		return System.currentTimeMillis() - heartRequest.getCreateMillisTime() >= heartBeatTimeout;
	}

	private void sendHeartBeatRequest(Client client) {
		HeartBeatStat heartBeatStat = getHeartBeatStatWithCreate(client.getAddress());
		heartBeatStat.currentHeartRequest = null; // 在write之前需要先置空currentHeartRequest
		InvocationRequest heartRequest = createHeartRequest(client);
		try {
			InvocationResponse response = client.write(heartRequest);
			heartBeatStat.currentHeartRequest = heartRequest;
			if (response != null) {
				processResponse(response, client);
			}
		} catch (Exception e) {
			heartBeatStat.incrFailed();
			notifyHeartBeatStatChanged(client);
			logger.warn("[heartbeat] send heartbeat to server[" + client.getAddress() + "] failed. detail["
					+ e.getMessage() + "].");

		}
	}

	private HeartBeatStat getHeartBeatStatWithCreate(String connect) {
		HeartBeatStat heartBeatStat = heartBeatStats.get(connect);
		if (heartBeatStat == null) {
			HeartBeatStat newStat = new HeartBeatStat(connect);
			heartBeatStat = heartBeatStats.putIfAbsent(connect, newStat);
			if (heartBeatStat == null) {
				heartBeatStat = newStat;
			}
		}
		return heartBeatStat;
	}

	private InvocationRequest createHeartRequest(Client client) {
		InvocationRequest request = new DefaultRequest(HEART_TASK_SERVICE, HEART_TASK_METHOD, null,
				SerializerFactory.SERIALIZE_HESSIAN, Constants.MESSAGE_TYPE_HEART, 5000, null);
		request.setSequence(generateHeartSeq(client));
		request.setCreateMillisTime(System.currentTimeMillis());
		request.setCallType(Constants.CALLTYPE_REPLY);
		return request;
	}

	private long generateHeartSeq(Client client) {
		return heartBeatSeq.getAndIncrement();
	}

	@Override
	public void addConnect(ConnectInfo cmd, Client client) {
		if (logger.isInfoEnabled()) {
			logger.info("[heartbeat] add service provider:" + client);
			// logger.info("[heartbeat] current checking providers:" +
			// this.getWorkingClients());
		}
	}

	public void addConnect(ConnectInfo cmd) {
		if (logger.isInfoEnabled()) {
			logger.info("[heartbeat] add service provider:" + cmd);
			// logger.info("[heartbeat] current checking providers:" +
			// this.getWorkingClients());
		}
	}

	public void removeConnect(Client client) {
		if (logger.isInfoEnabled()) {
			logger.info("[heartbeat] remove service provider:" + client);
		}
		for (Iterator<Entry<String, List<Client>>> iter = workingClients.entrySet().iterator(); iter.hasNext();) {
			Entry<String, List<Client>> entry = iter.next();
			if (entry.getValue() != null && entry.getValue().contains(client)) {
				entry.getValue().remove(client);
			}
		}
		if (logger.isInfoEnabled()) {
			// logger.info("[heartbeat] current checking providers:" +
			// this.getWorkingClients());
		}
	}

	public void processResponse(InvocationResponse response, Client client) {
		if (logger.isDebugEnabled()) {
			logger.debug("response:" + response);
			logger.debug("client:" + client);
		}
		// 兼容老版本
		if (!InvokerUtils.isHeartErrorResponse(response)) {
			Object heartReturn = response.getReturn();
			if (heartReturn instanceof Integer && (Integer) heartReturn >= Constants.VERSION_150) {
				client.setActiveSetable(true);
			}
		}
		HeartBeatStat heartStat = getHeartBeatStatWithCreate(client.getAddress());
		if (heartStat.currentHeartRequest == null
				|| heartStat.currentHeartRequest.getSequence() == response.getSequence()) {
			heartStat.currentHeartRequest = null;
			heartStat.incrSucceed();
			notifyHeartBeatStatChanged(client);
		}
	}

	private void notifyHeartBeatStatChanged(Client client) {
		try {
			HeartBeatStat heartStat = heartBeatStats.get(client.getAddress());
			if (heartStat.succeedCounter.longValue() >= heartBeatHealthCount) {
				if (!client.isActive()) {
					client.setActive(true);
					logger.error("@service-activate:" + client.getAddress() + ", service:" + getServiceName(client));
				}
				heartStat.resetCounter();
			} else if (heartStat.failedCounter.longValue() >= heartBeatDeadCount) {
				if (client.isActive()) {
					if (isHeartBeatAutoPickOff && canPickOff(client)) {
						client.setActive(false);
						logger.error("@service-deactivate:" + client.getAddress());
					} else {
						logger.error("@service-dieaway:" + client.getAddress());

					}
				}
				heartStat.resetCounter();
			}
		} catch (Exception e) {
			logger.error("[heartbeat] notify heartbeat stat changed failed", e);
		}
	}

	private String getServiceName(Client client) {
		for (Iterator<Entry<String, List<Client>>> iter = getWorkingClients().entrySet().iterator(); iter.hasNext();) {
			Entry<String, List<Client>> entry = iter.next();
			if (entry.getValue() != null && entry.getValue().contains(client)) {
				return StringUtils.substringBetween(entry.getKey(), serviceNameSpace, "/");
			}
		}
		return "unknown";
	}

	private boolean canPickOff(Client client) {
		Map<String, Set<HostInfo>> serviceHostInfos = ClientManager.getInstance().getServiceHostInfos();
		if (serviceHostInfos.isEmpty()) {
			// never be here, otherwise no take off
			return false;
		}
		HostInfo hostInfo = new HostInfo(client.getHost(), client.getPort(), 1);
		for (Entry<String, Set<HostInfo>> hostsEntry : serviceHostInfos.entrySet()) {
			String serviceName = hostsEntry.getKey();
			Set<HostInfo> hostInfos = hostsEntry.getValue();
			if (hostInfos.contains(hostInfo)) {
				int total = hostInfos.size();
				/**
				 * 目前自动摘除策略： 1. 确保2/3该Service的机器正常可用的前提下，可摘除探测到的不健康的机器 2.
				 * 特例：当只有两台机器时，确保该Service一台机器正常可用即可
				 */
				int leastAvailable = total != 2 ? total - (int) Math.floor(total / 3) : 1;
				List<Client> workingClients_ = getWorkingClients().get(serviceName);
				int working = 0;
				if (workingClients_ != null) {
					for (Client workingClient : workingClients_) {
						int weight = RegistryManager.getInstance().getServiceWeight(workingClient.getAddress());
						if (workingClient.isActive() && weight > 0) {
							working++;
						}
					}
				}
				if (working <= leastAvailable) {
					return false;
				}
			}
		}
		logger.error("can pick off...");
		return true;
	}

	@Override
	public void doNotUse(String serviceName, String host, int port) {
		// 在ClientCache中才能知道Client是否需要被真正关闭
	}

	public Map<String, List<Client>> getWorkingClients() {
		return workingClients;
	}

	public void setWorkingClients(Map<String, List<Client>> workingClients) {
		this.workingClients = workingClients;
	}

	class HeartBeatStat {
		String address;
		InvocationRequest currentHeartRequest;
		AtomicLong succeedCounter = new AtomicLong(); // 连续成功计数器
		AtomicLong failedCounter = new AtomicLong(); // 连续失败计数器

		public HeartBeatStat(String address) {
			this.address = address;
		}

		public void incrSucceed() {
			failedCounter.set(0L);
			succeedCounter.incrementAndGet();
		}

		public void incrFailed() {
			succeedCounter.set(0L);
			failedCounter.incrementAndGet();
		}

		public void resetCounter() {
			succeedCounter.set(0L);
			failedCounter.set(0L);
		}
	}

}
