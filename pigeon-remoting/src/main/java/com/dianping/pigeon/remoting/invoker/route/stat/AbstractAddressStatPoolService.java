/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.invoker.route.stat;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.dianping.pigeon.remoting.invoker.route.stat.DpsfAddressStatPool.AddressStat;
import com.dianping.pigeon.remoting.invoker.route.stat.policy.DpsfAddressStatPoolStrategy;

/**
 * 
 * @author jianhuihuang
 * 
 */
public abstract class AbstractAddressStatPoolService implements DpsfAddressStatPoolService {

	protected String app_name;

	protected ConcurrentHashMap<String, DpsfAddressStatPool> addressStatPools = new ConcurrentHashMap<String, DpsfAddressStatPool>(
			50);

	protected ConcurrentHashMap<String, DpsfAddressStatPoolStrategy> strategies = new ConcurrentHashMap<String, DpsfAddressStatPoolStrategy>();

	private SchedulerThreadPool analysisScheduler = new SchedulerThreadPool(5);

	private static final Logger logger = Logger.getLogger(AbstractAddressStatPoolService.class.getName());

	public Map<String, DpsfAddressStatPool> getAddressStatPools() {
		return this.addressStatPools;
	}

	public abstract List<Map.Entry<String, AddressStat>> sort(Map<String, AddressStat> orignal);

	public abstract void reset(DpsfAddressStatPool addressStatPool);

	protected void printAddressPoolStatLog(String appName, List<Entry<String, AddressStat>> sortedList) {

		if (sortedList.size() > 0) {
			StringBuilder sb = new StringBuilder("[");
			for (Iterator<Map.Entry<String, AddressStat>> it = sortedList.iterator(); it.hasNext();) {
				Map.Entry<String, AddressStat> entry = it.next();
				String ip = entry.getKey();
				AddressStat stat = entry.getValue();
				sb.append("(");
				sb.append(appName).append(",");
				sb.append(ip).append(",");
				sb.append(stat.exceptions.toString());

				sb.append("),");
			}
			sb.replace(sb.length() - 1, sb.length(), "]");
			logger.info(sb.toString());
		}

	}

	public void setStrategyService(DpsfAddressStatPoolStrategy strategy) {
		if (strategy != null) {
			this.strategies.putIfAbsent(strategy.getStrategyName(), strategy);
		}
	}

	public void unsetStrategyService(DpsfAddressStatPoolStrategy strategy) {
		if (strategy != null) {
			this.strategies.remove(strategy.getStrategyName());
		}
	}

	public void printAddressStatInfo(DpsfAddressStatPool addressStatPool, boolean resetAddrStat) {
		List<Map.Entry<String, AddressStat>> sortedList = sort(addressStatPool.getAddressStats());

		printAddressPoolStatLog(addressStatPool.getAppName(), sortedList);
		if (resetAddrStat)
			reset(addressStatPool);
	}

	public void addStrategy(String policy, DpsfAddressStatPool addressStatPool) {

		DpsfAddressStatPoolStrategy strategy = addressStatPool.getStrategy(policy);
		if (strategy == null) {

			DpsfAddressStatPoolStrategy statStrategy = strategies.get(policy);
			if (statStrategy == null) {
				StringBuffer buffer = new StringBuffer();
				buffer.append("未找到").append(policy).append("动态的路由策略，请检查相关配置");
				logger.error(buffer.toString());
				return;
			}
			addressStatPool.addStrategy(policy, statStrategy);
		}
	}

	public DpsfAddressStatPool getAddressStatPool(String appName) {
		DpsfAddressStatPool addressStatPool = null;
		if (StringUtils.isNotBlank(appName)) {

			addressStatPool = addressStatPools.get(appName);
			if (addressStatPool == null) {
				addressStatPool = new DpsfAddressStatPool(this);
				addressStatPool.setAppName(appName);
				addressStatPool.setBelongto(this.app_name);

				addressStatPools.putIfAbsent(appName, addressStatPool);
			}
		}
		return addressStatPool;
	}

	public List<String> getStableIps(String appName, String policy) {
		DpsfAddressStatPool addressStatPool = addressStatPools.get(appName);
		if (addressStatPool != null) {
			return addressStatPool.getStableIps(policy);
		}
		return null;
	}

	public List<String> getInsulateIps(String appName, String policy) {
		DpsfAddressStatPool addressStatPool = addressStatPools.get(appName);
		if (addressStatPool != null) {
			return addressStatPool.getInsulateIps(policy);
		}
		return null;
	}

	public ScheduledThreadPoolExecutor getScheduledThreadPoolExecutor() {
		return analysisScheduler;
	}

	protected static class SchedulerThreadPool extends ScheduledThreadPoolExecutor {
		public SchedulerThreadPool(int numThreads) {
			super(numThreads, new ThreadFactory() {
				public Thread newThread(Runnable r) {
					Thread thread = new Thread(r);
					thread.setDaemon(true);
					thread.setName("APT");
					return thread;
				}
			});
		}
	}

	public String getAppName() {
		return app_name;
	}
}
