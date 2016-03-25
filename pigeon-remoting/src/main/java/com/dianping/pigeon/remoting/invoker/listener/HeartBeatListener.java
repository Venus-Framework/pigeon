/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.invoker.listener;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import com.dianping.pigeon.registry.region.RegionManager;
import org.apache.logging.log4j.Logger;

import com.dianping.dpsf.protocol.DefaultRequest;
import com.dianping.pigeon.config.ConfigChangeListener;
import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.config.ConfigManagerLoader;
import com.dianping.pigeon.domain.HostInfo;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.monitor.Monitor;
import com.dianping.pigeon.monitor.MonitorLoader;
import com.dianping.pigeon.registry.RegistryManager;
import com.dianping.pigeon.remoting.common.codec.SerializerFactory;
import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.common.exception.ServiceStatusException;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.invoker.Client;
import com.dianping.pigeon.remoting.invoker.ClientManager;
import com.dianping.pigeon.remoting.invoker.callback.CallbackFuture;
import com.dianping.pigeon.remoting.invoker.domain.ConnectInfo;
import com.dianping.pigeon.remoting.invoker.util.InvokerUtils;
import com.dianping.pigeon.remoting.provider.ProviderBootStrap;
import com.dianping.pigeon.remoting.provider.Server;

public class HeartBeatListener implements Runnable, ClusterListener {

	private static final Logger logger = LoggerLoader.getLogger(HeartBeatListener.class);

	public static final String HEART_TASK_SERVICE = "HeartbeatService/";

	public static final String HEART_TASK_METHOD = "heartbeat";

	private static final RegionManager regionManager = RegionManager.INSTANCE;

	private Map<String, List<Client>> workingClients;

	private static AtomicLong heartBeatSeq = new AtomicLong();

	private static ConcurrentMap<String, HeartBeatStat> heartBeatStats = new ConcurrentHashMap<String, HeartBeatStat>();

	private final ClusterListenerManager clusterListenerManager = ClusterListenerManager.getInstance();

	private static ConfigManager configManager = ConfigManagerLoader.getConfigManager();

	private static long heartBeatDeadCount = configManager.getLongValue(Constants.KEY_HEARTBEAT_DEADTHRESHOLD,
			Constants.DEFAULT_HEARTBEAT_DEADCOUNT);
	private static long heartBeatHealthCount = configManager.getLongValue(Constants.KEY_HEARTBEAT_HEALTHTHRESHOLD,
			Constants.DEFAULT_HEARTBEAT_HEALTHCOUNT);
	private static boolean isHeartBeatAutoPickOff = configManager.getBooleanValue(Constants.KEY_HEARTBEAT_AUTOPICKOFF,
			Constants.DEFAULT_HEARTBEAT_AUTOPICKOFF);
	private static long interval = configManager.getLongValue(Constants.KEY_HEARTBEAT_INTERVAL,
			Constants.DEFAULT_HEARTBEAT_INTERVAL);
	private static int heartBeatTimeout = configManager.getIntValue(Constants.KEY_HEARTBEAT_TIMEOUT,
			Constants.DEFAULT_HEARTBEAT_TIMEOUT);
	private static float pickoffRatio = configManager.getFloatValue("pigeon.heartbeat.pickoffratio", 0.5f);
	private static boolean logPickOff = configManager.getBooleanValue("pigeon.heartbeat.logpickoff", true);
	private static final Monitor monitor = MonitorLoader.getMonitor();

	private static volatile Set<String> inactiveAddresses = new HashSet<String>();

	private static final Set<Integer> serverPorts = new HashSet<Integer>();

	public HeartBeatListener() {
		ConfigManagerLoader.getConfigManager().registerConfigChangeListener(new InnerConfigChangeListener());
	}

	private static class InnerConfigChangeListener implements ConfigChangeListener {

		@Override
		public void onKeyUpdated(String key, String value) {
			if (key.endsWith("pigeon.heartbeat.logpickoff")) {
				try {
					logPickOff = Boolean.valueOf(value);
				} catch (RuntimeException e) {
				}
			} else if (key.endsWith("pigeon.heartbeat.pickoffratio")) {
				try {
					pickoffRatio = Float.valueOf(value);
				} catch (RuntimeException e) {
				}
			} else if (key.endsWith(Constants.KEY_HEARTBEAT_TIMEOUT)) {
				try {
					heartBeatTimeout = Integer.valueOf(value);
				} catch (RuntimeException e) {
				}
			} else if (key.endsWith(Constants.KEY_HEARTBEAT_INTERVAL)) {
				try {
					interval = Long.valueOf(value);
				} catch (RuntimeException e) {
				}
			} else if (key.endsWith(Constants.KEY_HEARTBEAT_HEALTHTHRESHOLD)) {
				try {
					heartBeatHealthCount = Long.valueOf(value);
				} catch (RuntimeException e) {
				}
			} else if (key.endsWith(Constants.KEY_HEARTBEAT_DEADTHRESHOLD)) {
				try {
					heartBeatDeadCount = Long.valueOf(value);
				} catch (RuntimeException e) {
				}
			} else if (key.endsWith(Constants.KEY_HEARTBEAT_AUTOPICKOFF)) {
				try {
					isHeartBeatAutoPickOff = Boolean.valueOf(value);
				} catch (RuntimeException e) {
				}
			}
		}

		@Override
		public void onKeyAdded(String key, String value) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onKeyRemoved(String key) {
			// TODO Auto-generated method stub

		}
	}

	public void run() {
		long sleepTime = interval;
		while (!Thread.currentThread().isInterrupted()) {
			try {
				Thread.sleep(sleepTime);
				if (serverPorts.isEmpty()) {
					Collection<Server> servers = ProviderBootStrap.getServersMap().values();
					for (Server server : servers) {
						serverPorts.add(server.getPort());
					}
				}
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
						if(client != null) {
							boolean enable = Constants.PROTOCOL_DEFAULT.equals(client.getProtocol())
									&& (RegistryManager.getInstance().getServiceWeight(client.getAddress()) > 0);
							if (enable) {
								if (client.isConnected()) {
									if (configManager.getLocalIp().equals(client.getHost())
											&& serverPorts.contains(client.getPort())) {
										continue;
									}
									sendHeartBeatRequest(client);
								} else {
									logger.info("[heartbeat] remove connect:" + client.getAddress());
									clusterListenerManager.removeConnect(client);
								}
							}
						}
					}
				}
				sleepTime = interval - (System.currentTimeMillis() - now);
			} catch (Throwable e) {
				logger.info("[heartbeat] task failed:", e);
			} finally {
				if (sleepTime < 1000) {
					sleepTime = 1000;
				}
			}
		}
	}

	private void sendHeartBeatRequest(Client client) {
		HeartBeatStat heartBeatStat = getHeartBeatStatWithCreate(client.getAddress());
		InvocationRequest heartRequest = createHeartRequest(client);
		try {
			InvocationResponse response = null;
			CallbackFuture future = new CallbackFuture();
			response = InvokerUtils.sendRequest(client, heartRequest, future);
			if (response == null) {
				response = future.get(heartBeatTimeout);
			}
			if (response != null) {
				processResponse(heartRequest, response, client);
			}
		} catch (Throwable e) {
			heartBeatStat.incrFailed();
			notifyHeartBeatStatChanged(client);
			if (client.isActive()) {
				logger.info("[heartbeat] send heartbeat to server[" + client.getAddress() + "] failed");
			}
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
		InvocationRequest request = new DefaultRequest(HEART_TASK_SERVICE + client.getAddress(), HEART_TASK_METHOD,
				null, SerializerFactory.SERIALIZE_HESSIAN, Constants.MESSAGE_TYPE_HEART, heartBeatTimeout, null);
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

	public void processResponse(InvocationRequest request, InvocationResponse response, Client client) {
		if (logger.isDebugEnabled()) {
			logger.debug("response:" + response);
			logger.debug("client:" + client);
		}
		HeartBeatStat heartStat = getHeartBeatStatWithCreate(client.getAddress());
		if (request.getSequence() == response.getSequence()) {
			heartStat.incrSucceed();
			notifyHeartBeatStatChanged(client);
		} else {
			logger.info("[heartbeat] inconsistent heartbeat sequence " + request.getSequence() + ":"
					+ response.getSequence() + " from:" + client);
		}
	}

	private void notifyHeartBeatStatChanged(Client client) {
		try {
			HeartBeatStat heartStat = heartBeatStats.get(client.getAddress());
			if (heartStat.succeedCounter.longValue() >= heartBeatHealthCount) {
				//TODO  给RegionManager的hostCache标记active为true,notify
				if(regionManager.isEnableRegionAutoSwitch()) {
					regionManager.getRegionHostHeartBeatStats().put(client.getAddress(), true);
				}
				if (!client.isActive()) {
					client.setActive(true);
					inactiveAddresses.remove(client.getAddress());
					logger.info("@service-activate:" + client + ", service:" + getServiceName(client)
							+ ", inactive addresses:" + inactiveAddresses);
				}
				heartStat.resetCounter();
			} else if (heartStat.failedCounter.longValue() >= heartBeatDeadCount) {
				//TODO  给RegionManager的hostCache标记active为false,notify
				if(regionManager.isEnableRegionAutoSwitch()) {
					regionManager.getRegionHostHeartBeatStats().put(client.getAddress(), false);
				}
				if (client.isActive()) {
					//TODO 考虑启动自动切换region的时候，去掉canPickOff的选项
					if (isHeartBeatAutoPickOff && canPickOff(client)) {
						client.setActive(false);
						inactiveAddresses.add(client.getAddress());
						logger.info("@service-deactivate:" + client + ", inactive addresses:" + inactiveAddresses);

						if (logPickOff) {
							ServiceStatusException ex = new ServiceStatusException("remote server " + client
									+ " unavailable");
							ex.setStackTrace(new StackTraceElement[] {});
							monitor.logError(ex);
						}
					} else {
						logger.info("@service-dieaway:" + client + ", inactive addresses:" + inactiveAddresses);
					}
				}
				heartStat.resetCounter();
			}
		} catch (Throwable e) {
			logger.error("[heartbeat] notify heartbeat stat changed failed", e);
		}
	}

	private String getServiceName(Client client) {
		for (Iterator<Entry<String, List<Client>>> iter = getWorkingClients().entrySet().iterator(); iter.hasNext();) {
			Entry<String, List<Client>> entry = iter.next();
			if (entry.getValue() != null && entry.getValue().contains(client)) {
				return entry.getKey();
			}
		}
		return "unknown";
	}

	private boolean canPickOff(Client client) {
		//TODO 测试 开启autoswitch的时候，直接返回true
		if(regionManager.isEnableRegionAutoSwitch())
			return regionManager.isEnableRegionAutoSwitch();

		Map<String, Set<HostInfo>> serviceHostInfos = ClientManager.getInstance().getServiceHosts();
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
				 * 目前自动摘除策略： 1. 确保1/2该Service的机器正常可用的前提下，可摘除探测到的不健康的机器 2.
				 * 特例：当只有两台机器时，确保该Service一台机器正常可用即可
				 */
				int leastAvailable = total != 2 ? total - (int) Math.floor(total * pickoffRatio) : 1;
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
					logger.info("can not pick off:" + client + ", working:" + working + ", least:" + leastAvailable
							+ ", total:" + total);
					return false;
				}
			}
		}
		logger.info("can pick off:" + client);
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

	public static boolean isActiveAddress(String address) {
		return !inactiveAddresses.contains(address);
	}

}