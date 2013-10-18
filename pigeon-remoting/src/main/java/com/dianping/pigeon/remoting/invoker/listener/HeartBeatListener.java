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

import com.dianping.dpsf.component.DPSFRequest;
import com.dianping.dpsf.component.DPSFResponse;
import com.dianping.dpsf.protocol.DefaultRequest;
import com.dianping.pigeon.component.HostInfo;
import com.dianping.pigeon.registry.cache.WeightCache;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.common.util.ResponseUtils;
import com.dianping.pigeon.remoting.invoker.Client;
import com.dianping.pigeon.remoting.invoker.component.ConnectInfo;
import com.dianping.pigeon.remoting.invoker.config.InvokerConfigurer;
import com.dianping.pigeon.remoting.invoker.service.ClientManager;
import com.dianping.pigeon.serialize.SerializerFactory;

public class HeartBeatListener implements Runnable, ClusterListener {

	private static final Logger logger = Logger.getLogger(HeartBeatListener.class);

	private static final String HEART_TASK_SERVICE = "http://service.dianping.com/piegonService/heartTaskService";

	private static final String HEART_TASK_METHOD = "heartBeat";

	private Map<String, List<Client>> workingClients;

	private static AtomicLong heartBeatSeq = new AtomicLong();

	private static ConcurrentMap<String, HeartBeatStat> heartBeatStats = new ConcurrentHashMap<String, HeartBeatStat>();

	private final ClusterListenerManager clusterListenerManager = ClusterListenerManager.getInstance();

	public void run() {
		long sleepTime = InvokerConfigurer.getHeartBeatInterval();
		while (!Thread.currentThread().isInterrupted()) {
			try {
				Thread.sleep(sleepTime);
				long now = System.currentTimeMillis();
				long heartBeatTimeout = InvokerConfigurer.getHeartBeatTimeout();
				// 检查正在工作的Clients是否完好
				if (this.workingClients != null) {
					Set<Client> clientSet = new HashSet<Client>();
					for (Entry<String, List<Client>> entry : this.workingClients.entrySet()) {
						List<Client> clientList = entry.getValue();
						if (clientList != null) {
							clientSet.addAll(clientList);
						}
					}
					for (Client client : clientSet) {
						if (client.isConnected()) {
							String connect = client.getAddress();
							if (!hasHeartBeatRequestExists(connect)) {
								sendHeartBeatRequest(client);
							} else {
								HeartBeatStat heartBeatStat = heartBeatStats.get(connect);
								DPSFRequest heartRequest = heartBeatStat.currentHeartRequest;
								if (isHeartRequestTimeout(heartRequest, heartBeatTimeout)) {
									heartBeatStat.incrFailed();
									notifyHeartBeatStatChanged(client);
									sendHeartBeatRequest(client);
								}
							}
						} else {
							logger.error("heart beat task,remove connect:" + client.getAddress() + ",servicename:"
									+ client.getServiceName());
							clusterListenerManager.removeConnect(client);
						}
					}
				}
				sleepTime = InvokerConfigurer.getHeartBeatInterval() - (System.currentTimeMillis() - now);
			} catch (Exception e) {
				logger.error("Do heartbeat task failed, detail[" + e.getMessage() + "].", e);
			} finally {
				if (sleepTime < 1000) {
					sleepTime = 1000;
				}
			}
		}
	}

	private boolean hasHeartBeatRequestExists(String connect) {
		HeartBeatStat heartBeatStat = heartBeatStats.get(connect);
		return heartBeatStat != null && heartBeatStat.currentHeartRequest != null;
	}

	private boolean isHeartRequestTimeout(DPSFRequest heartRequest, long heartBeatTimeout) {
		return System.currentTimeMillis() - heartRequest.getCreateMillisTime() >= heartBeatTimeout;
	}

	private void sendHeartBeatRequest(Client client) {
		try {
			HeartBeatStat heartStat = getHeartBeatStatWithCreate(client.getAddress());
			heartStat.currentHeartRequest = null; // 在write之前需要先置空currentHeartRequest
			DPSFRequest heartRequest = createHeartRequest(client);
			client.write(heartRequest);
			heartStat.currentHeartRequest = heartRequest;
		} catch (Exception e) {
			logger.warn("Send heartbeat to server[" + client.getAddress() + "] failed. detail[" + e.getMessage() + "].");
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

	private DPSFRequest createHeartRequest(Client client) {
		DPSFRequest request = new DefaultRequest(HEART_TASK_SERVICE, HEART_TASK_METHOD, null,
				SerializerFactory.SERIALIZE_HESSIAN, Constants.MESSAGE_TYPE_HEART, 30000, null);
		request.setSequence(generateHeartSeq(client));
		request.setCreateMillisTime(System.currentTimeMillis());
		request.setCallType(Constants.CALLTYPE_REPLY);
		return request;
	}

	private long generateHeartSeq(Client client) {
		return heartBeatSeq.getAndIncrement();
	}

	public void addConnect(ConnectInfo cmd) {

	}

	public void removeConnect(Client client) {
	}

	public void processResponse(DPSFResponse response, Client client) {
		if (logger.isInfoEnabled()) {
			logger.info("response is" + response);
			logger.info("client is" + client);
		}
		// 兼容老版本
		if (!ResponseUtils.isHeartErrorResponse(response)) {
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
			if (heartStat.succeedCounter.longValue() >= InvokerConfigurer.getHeartBeatHealthCount()) {
				if (!client.isActive()) {
					client.setActive(true);
					logger.error("@service-activate:" + client.getAddress() + ", servicename:" + getServiceName(client));
				}
				heartStat.resetCounter();
			} else if (heartStat.failedCounter.longValue() >= InvokerConfigurer.getHeartBeatDeadCount()) {
				if (client.isActive()) {
					if (InvokerConfigurer.isHeartBeatAutoPickOff() && canPickOff(client)) {
						client.setActive(false);
						logger.error("@service-deactivate:" + client.getAddress());
					} else {
						logger.error("@service-dieaway:" + client.getAddress());

					}
				}
				heartStat.resetCounter();
			}
		} catch (Exception e) {
			logger.error("Notify heartbeat stat changed failed, detail[" + e.getMessage() + "].", e);
		}
	}

	private String getServiceName(Client client) {
		for (Iterator<Entry<String, List<Client>>> iter = workingClients.entrySet().iterator(); iter.hasNext();) {
			Entry<String, List<Client>> entry = iter.next();
			if (entry.getValue() != null && entry.getValue().contains(client)) {
				return StringUtils.substringBetween(entry.getKey(), InvokerConfigurer.getServiceNameSpace(), "/");
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
				List<Client> workingClients_ = workingClients.get(serviceName);
				int working = 0;
				if (workingClients_ != null) {
					for (Client workingClient : workingClients_) {
						int weight = WeightCache.getInstance().getWeightWithDefault(serviceName,
								workingClient.getAddress());
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
	public void addConnect(ConnectInfo cmd, Client client) {
	}

	@Override
	public void doNotUse(String serviceName, String host, int port) {
		// 在ClientCache中才能知道Client是否需要被真正关闭
	}

	public void setWorkingClients(Map<String, List<Client>> workingClients) {
		this.workingClients = workingClients;
	}

	class HeartBeatStat {
		String address;
		DPSFRequest currentHeartRequest;
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
