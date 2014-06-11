/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.invoker.route.balance;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.extension.ExtensionLoader;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.registry.RegistryManager;
import com.dianping.pigeon.registry.listener.RegistryEventListener;
import com.dianping.pigeon.registry.listener.ServiceProviderChangeEvent;
import com.dianping.pigeon.registry.listener.ServiceProviderChangeListener;
import com.dianping.pigeon.remoting.common.exception.InvalidParameterException;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.invoker.Client;
import com.dianping.pigeon.remoting.invoker.config.InvokerConfig;
import com.dianping.pigeon.remoting.invoker.domain.ConnectInfo;
import com.dianping.pigeon.remoting.invoker.listener.ClusterListener;
import com.dianping.pigeon.remoting.invoker.listener.ClusterListenerManager;
import com.dianping.pigeon.remoting.invoker.route.statistics.ServiceStatisticsChecker;
import com.dianping.pigeon.threadpool.DefaultThreadPool;
import com.dianping.pigeon.threadpool.ThreadPool;

public class LoadBalanceManager {

	private static final Logger logger = LoggerLoader.getLogger(LoadBalanceManager.class);

	private static Map<String, LoadBalance> loadBalanceMap = new HashMap<String, LoadBalance>();

	private static ConfigManager configManager = ExtensionLoader.getExtension(ConfigManager.class);

	private static String loadBalanceFromConfigServer = configManager.getStringValue(Constants.KEY_LOADBALANCE,
			RoundRobinLoadBalance.NAME);

	private static ConcurrentHashMap<String, Integer> weightFactors = new ConcurrentHashMap<String, Integer>();

	private static ConcurrentHashMap<String, Integer> weights = new ConcurrentHashMap<String, Integer>();

	private static int initialFactor = configManager.getIntValue("pigeon.loadbalance.initialFactor", 0);
	private static int defaultFactor = configManager.getIntValue("pigeon.loadbalance.defaultFactor", 100);
	private static long interval = configManager.getLongValue("pigeon.loadbalance.interval", 3000);
	private static int step = configManager.getIntValue("pigeon.loadbalance.step", 10);

	private static ThreadPool weightFacotrMaintainThreadPool = new DefaultThreadPool(
			"Pigeon-Client-Weight-Factor-Maintain-ThreadPool");

	private static ThreadPool serviceStatisticsCheckerThreadPool = new DefaultThreadPool(
			"Pigeon-Client-Service-Statistics-Checker-Thread");

	private static volatile int errorLogSeed = 0;

	/**
	 * 支持4种负责均衡方式： 1. Random LoadBalance：随机，按权重设置随机概率，在一个截面上碰撞的概率高，但调用量越大分布越均匀，
	 * 而且按概率使用权重后也比较均匀，有利于动态调整提供者权重。 (Finish)
	 * 
	 * 2. RoundRobin LoadBalance 轮循，按公约后的权重设置轮循比率，存在慢的提供者累积请求问题，
	 * 比如：第二台机器很慢，但没挂，当请求调到第二台时就卡在那，久而久之，所有请求都卡在调到第二台上。
	 * 
	 * 3. LeastActive LoadBalance： 最少活跃调用数，相同活跃数的随机，活跃数指调用前后计数差。
	 * 使慢的提供者收到更少请求，因为越慢的提供者的调用前后计数差会越大。 (Finish)
	 * 
	 * 4. ConsistentHash LoadBalance 一致性Hash，相同参数的请求总是发到同一提供者。
	 * 当某一台提供者挂时，原本发往该提供者的请求，基于虚拟节点，平摊到其它提供者，不会引起剧烈变动。
	 * 
	 * 5. LeastSuccess LoadBalance，
	 * 当前调用成功率最高的优先分配（为了避免负载不均与，成功率前80%再按照LeastActive的方式选择） (Finish)
	 */
	/**
	 * 
	 * 
	 * @param invokerConfig
	 * @param callType
	 * @return
	 */
	public static LoadBalance getLoadBalance(InvokerConfig<?> invokerConfig, int callType) {
		LoadBalance loadBalance = loadBalanceMap.get(invokerConfig.getLoadbalance());
		if (loadBalance != null) {
			return loadBalance;
		}

		String serviceId = invokerConfig.getUrl();
		if (invokerConfig.getGroup() != null) {
			serviceId = serviceId + ":" + invokerConfig.getGroup();
		}
		loadBalance = loadBalanceMap.get(serviceId);
		if (loadBalance != null) {
			return loadBalance;
		}

		if (loadBalanceFromConfigServer != null) {
			loadBalance = loadBalanceMap.get(loadBalanceFromConfigServer);
			if (loadBalance != null) {
				loadBalanceMap.put(invokerConfig.getLoadbalance(), loadBalance);
				return loadBalance;
			} else {
				logError("the loadbalance[" + loadBalanceFromConfigServer + "] is invalid, only support "
						+ loadBalanceMap.keySet() + ".", null);
			}
		}
		return loadBalance;
	}

	@SuppressWarnings("unchecked")
	public static void register(String serviceName, String group, Object loadBalance) {
		String serviceId = serviceName;
		if (group != null) {
			serviceId = serviceId + ":" + group;
		}
		if (loadBalanceMap.containsKey(serviceId)) {
			logger.warn("Duplicate loadbalance already registered with service[" + serviceId + "], replace it.");
		}
		LoadBalance loadBlanceObj = null;
		if (loadBalance instanceof LoadBalance) {
			loadBlanceObj = (LoadBalance) loadBalance;
		} else {
			if (loadBalance instanceof String) {
				if (!loadBalanceMap.containsKey(loadBalance)) {
					throw new InvalidParameterException("Loadbalance[" + loadBalance + "] registered by service["
							+ serviceId + "] is not supported.");
				}
				loadBlanceObj = loadBalanceMap.get(loadBalance);
			} else if (loadBalance instanceof Class) {
				Class<? extends LoadBalance> loadBalanceClass = (Class<? extends LoadBalance>) loadBalance;
				try {
					loadBlanceObj = loadBalanceClass.newInstance();
				} catch (Throwable e) {
					throw new InvalidParameterException("Register loadbalance[service=" + serviceId + ", class="
							+ loadBalance + "] failed.", e);
				}
			}
		}
		if (loadBlanceObj != null) {
			loadBalanceMap.put(serviceId, loadBlanceObj);
		}
	}

	public static int getEffectiveWeight(String clientAddress) {
		Integer w = weights.get(clientAddress);
		if (w == null) {
			w = 1;
		}
		Integer wf = weightFactors.get(clientAddress);
		if (wf == null) {
			return w * defaultFactor;
		} else {
			return w * wf.intValue();
		}
	}

	public static void init() {
		WeightFactorMaintainer weightFactorMaintainer = new WeightFactorMaintainer();
		RegistryEventListener.addListener(weightFactorMaintainer);
		ClusterListenerManager.getInstance().addListener(weightFactorMaintainer);
		weightFacotrMaintainThreadPool.execute(weightFactorMaintainer);
		ServiceStatisticsChecker serviceStatisticsChecker = new ServiceStatisticsChecker();
		serviceStatisticsCheckerThreadPool.execute(serviceStatisticsChecker);
	}

	private static void logError(String message, Throwable t) {
		if (errorLogSeed++ % 1000 == 0) {
			if (t != null) {
				logger.warn(message, t);
			} else {
				logger.warn(message);
			}
			errorLogSeed = 0;
		}
	}

	private static class WeightFactorMaintainer implements Runnable, ServiceProviderChangeListener, ClusterListener {

		public WeightFactorMaintainer() {
			if (initialFactor < 0) {
				initialFactor = 0;
			}
			if (interval < 0) {
				interval = 1000;
			}
			if (initialFactor > defaultFactor || step < 0) {
				throw new IllegalArgumentException("Invalid weight factor params");
			}
		}

		@Override
		public void run() {
			while (!Thread.interrupted()) {
				try {
					Thread.sleep(interval);
					adjustFactor();
				} catch (InterruptedException e) {
					break;
				}
			}
		}

		private void adjustFactor() {
			Iterator<Entry<String, Integer>> it = weightFactors.entrySet().iterator();
			while (it.hasNext()) {
				Entry<String, Integer> entry = it.next();
				if (entry.getValue() < defaultFactor) {
					int factor = Math.min(defaultFactor, entry.getValue() + step);
					entry.setValue(factor);
				}
			}
		}

		@Override
		public void providerAdded(ServiceProviderChangeEvent event) {
			addWeight(event.getConnect(), event.getWeight());
		}

		@Override
		public void providerRemoved(ServiceProviderChangeEvent event) {
			removeWeight(event.getConnect());
		}

		@Override
		public void hostWeightChanged(ServiceProviderChangeEvent event) {
			Integer originalWeight = weights.get(event.getConnect());
			weights.put(event.getConnect(), event.getWeight());
			if ((originalWeight == null || originalWeight.intValue() == 0) && event.getWeight() > 0) {
				weightFactors.put(event.getConnect(), initialFactor);
			}
		}

		@Override
		public void addConnect(ConnectInfo cmd) {
		}

		@Override
		public void addConnect(ConnectInfo cmd, Client client) {
			addWeight(cmd.getConnect(), RegistryManager.getInstance().getServiceWeight(cmd.getConnect()));
		}

		@Override
		public void removeConnect(Client client) {
			removeWeight(client.getAddress());
		}

		@Override
		public void doNotUse(String serviceName, String host, int port) {
			removeWeight(host + ":" + port);
		}
		
		private void addWeight(String address, int weight) {
			weights.put(address, weight);
			weightFactors.put(address, initialFactor);
		}
		
		private void removeWeight(String address) {
			weights.remove(address);
			weightFactors.remove(address);
		}

	}
}
