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
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;

import com.dianping.pigeon.log.LoggerLoader;

import org.apache.logging.log4j.Logger;

import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.extension.ExtensionLoader;
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
import com.dianping.pigeon.remoting.invoker.route.statistics.CapacityChecker;
import com.dianping.pigeon.remoting.invoker.route.statistics.ServiceStatisticsHolder;
import com.dianping.pigeon.threadpool.DefaultThreadPool;
import com.dianping.pigeon.threadpool.ThreadPool;
import com.dianping.pigeon.util.ClassUtils;

public class LoadBalanceManager {

	private static final Logger logger = LoggerLoader.getLogger(LoadBalanceManager.class);

	private static Map<String, LoadBalance> loadBalanceMap = new ConcurrentHashMap<String, LoadBalance>();

	private static ConfigManager configManager = ExtensionLoader.getExtension(ConfigManager.class);

	public static final String DEFAULT_LOADBALANCE = configManager.getStringValue(Constants.KEY_LOADBALANCE,
			WeightedAutoawareLoadBalance.NAME);

	private static ConcurrentHashMap<String, WeightFactor> weightFactors = new ConcurrentHashMap<String, WeightFactor>();

	private static ConcurrentHashMap<String, Integer> weights = new ConcurrentHashMap<String, Integer>();

	private static int initialFactor = configManager.getIntValue("pigeon.loadbalance.initialFactor", 0);
	private static int defaultFactor = configManager.getIntValue("pigeon.loadbalance.defaultFactor", 100);
	private static long interval = configManager.getLongValue("pigeon.loadbalance.interval", 200);
	private static int step = configManager.getIntValue("pigeon.loadbalance.step", 1);
	private static String stepTicks = configManager
			.getStringValue(
					"pigeon.loadbalance.stepticks",
					"0:15;1:15;2:15;3:15;4:15;5:10;6:10;7:10;8:10;9:10;10:7;11:7;12:7;13:7;14:7;15:5;16:5;17:5;18:5;19:5;20:3;21:3;22:3;23:3;24:3;25:2;26:2;27:2;28:2;29:2");
	private static Map<Integer, Integer> stepTicksMap = new HashMap<Integer, Integer>();

	private static ThreadPool loadbalanceThreadPool = new DefaultThreadPool("Pigeon-Client-Loadbalance-ThreadPool");

	private static volatile int errorLogSeed = 0;

	/**
	 * 
	 * 
	 * @param invokerConfig
	 * @param callType
	 * @return
	 */
	public static LoadBalance getLoadBalance(InvokerConfig<?> invokerConfig, int callType) {
		String serviceId = getServiceId(invokerConfig.getUrl(), invokerConfig.getGroup());
		LoadBalance loadBalance = loadBalanceMap.get(serviceId);
		if (loadBalance != null) {
			return loadBalance;
		}
		loadBalance = loadBalanceMap.get(invokerConfig.getLoadbalance());
		if (loadBalance != null) {
			return loadBalance;
		}
		if (DEFAULT_LOADBALANCE != null) {
			loadBalance = loadBalanceMap.get(DEFAULT_LOADBALANCE);
			if (loadBalance != null) {
				loadBalanceMap.put(invokerConfig.getLoadbalance(), loadBalance);
				return loadBalance;
			} else {
				logError(
						"the loadbalance[" + DEFAULT_LOADBALANCE + "] is invalid, only support "
								+ loadBalanceMap.keySet() + ".", null);
			}
		}
		return loadBalance;
	}

	public static String getServiceId(String serviceName, String group) {
		String serviceId = serviceName;
		if (StringUtils.isNotBlank(group)) {
			serviceId = serviceId + ":" + group;
		}
		return serviceId;
	}

	@SuppressWarnings("unchecked")
	public static void register(String serviceName, String group, Object loadBalance) {
		String serviceId = getServiceId(serviceName, group);
		LoadBalance loadBlanceObj = null;
		if (loadBalance instanceof LoadBalance) {
			loadBlanceObj = (LoadBalance) loadBalance;
		} else if (loadBalance instanceof String && StringUtils.isNotBlank((String) loadBalance)) {
			if (!loadBalanceMap.containsKey(loadBalance)) {
				try {
					Class<? extends LoadBalance> loadbalanceClass = (Class<? extends LoadBalance>) ClassUtils
							.loadClass((String) loadBalance);
					loadBlanceObj = loadbalanceClass.newInstance();
				} catch (Throwable e) {
					throw new InvalidParameterException("failed to register loadbalance[service=" + serviceId
							+ ",class=" + loadBalance + "]", e);
				}
			} else {
				loadBlanceObj = loadBalanceMap.get(loadBalance);
			}
		} else if (loadBalance instanceof Class) {
			try {
				Class<? extends LoadBalance> loadbalanceClass = (Class<? extends LoadBalance>) loadBalance;
				loadBlanceObj = loadbalanceClass.newInstance();
			} catch (Throwable e) {
				throw new InvalidParameterException("failed to register loadbalance[service=" + serviceId + ",class="
						+ loadBalance + "]", e);
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
		WeightFactor wf = weightFactors.get(clientAddress);
		if (wf == null) {
			return w * defaultFactor;
		} else {
			return w * wf.getFactor();
		}
	}

	public static void destroy() throws Exception {
		if (loadbalanceThreadPool != null) {
			try {
				loadbalanceThreadPool.getExecutor().shutdown();
				loadbalanceThreadPool.getExecutor().awaitTermination(2, TimeUnit.SECONDS);
				loadbalanceThreadPool = null;
			} catch (InterruptedException e) {
				logger.warn("interrupted when shuting down the query executor:\n{}", e);
			}
		}
	}

	public static void init() {
		WeightFactorMaintainer weightFactorMaintainer = new WeightFactorMaintainer();
		RegistryEventListener.addListener(weightFactorMaintainer);
		ClusterListenerManager.getInstance().addListener(weightFactorMaintainer);
		loadbalanceThreadPool.execute(weightFactorMaintainer);
		CapacityChecker serviceStatisticsChecker = new CapacityChecker();
		loadbalanceThreadPool.execute(serviceStatisticsChecker);

		// initialize step ticks
		if (StringUtils.isNotBlank(stepTicks)) {
			try {
				String[] ticks = stepTicks.split(";");
				for (String tick : ticks) {
					if (StringUtils.isNotBlank(tick)) {
						String[] kv = tick.split(":");
						if (kv.length == 2) {
							stepTicksMap.put(Integer.valueOf(kv[0]), Integer.valueOf(kv[1]));
						}
					}
				}
			} catch (RuntimeException e) {
				logger.error("", e);
			}
		}
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

	public static class WeightFactor {
		private int factor;
		private int currentStepTicks;

		public WeightFactor(int initialFactor) {
			factor = initialFactor;
		}

		public int getFactor() {
			return factor;
		}

		public void setFactor(int factor) {
			this.factor = factor;
		}

		public int getCurrentStepTicks() {
			return currentStepTicks;
		}

		public void setCurrentStepTicks(int currentStepTicks) {
			this.currentStepTicks = currentStepTicks;
		}

		public String toString() {
			return factor + ":" + currentStepTicks;
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
				} catch (RuntimeException e) {
					logger.warn("error with weight factor maintainer:" + e.getMessage());
				}
			}
		}

		private void adjustFactor() {
			Iterator<Entry<String, WeightFactor>> it = weightFactors.entrySet().iterator();
			while (it.hasNext()) {
				Entry<String, WeightFactor> entry = it.next();
				WeightFactor weightFactor = entry.getValue();
				if (weightFactor.getFactor() < defaultFactor) {
					Integer ticks = stepTicksMap.get(weightFactor.getFactor());
					if (ticks == null) {
						ticks = 1;
					}
					int currentStepTicks = weightFactor.getCurrentStepTicks();
					weightFactor.setCurrentStepTicks(currentStepTicks + 1);
					if (weightFactor.getCurrentStepTicks() >= ticks) {
						int factor = Math.min(defaultFactor, weightFactor.getFactor() + step);
						weightFactor.setFactor(factor);
						weightFactor.setCurrentStepTicks(0);
					}
					entry.setValue(weightFactor);
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
				weightFactors.put(event.getConnect(), new WeightFactor(initialFactor));
			}
		}

		@Override
		public void addConnect(ConnectInfo cmd) {
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
			weightFactors.put(address, new WeightFactor(initialFactor));
		}

		private void removeWeight(String address) {
			weights.remove(address);
			weightFactors.remove(address);
			ServiceStatisticsHolder.removeCapacityBucket(address);
		}

	}

	public static Map<String, WeightFactor> getWeightFactors() {
		return weightFactors;
	}
}
