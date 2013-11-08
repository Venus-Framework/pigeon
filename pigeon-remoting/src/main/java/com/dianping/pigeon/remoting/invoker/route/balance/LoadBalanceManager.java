/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.invoker.route.balance;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.dianping.pigeon.remoting.common.config.RemotingConfigurer;
import com.dianping.pigeon.remoting.invoker.component.InvokerMetaData;

/**
 * 
 * 
 * @author jianhuihuang,wuxiang
 * @version $Id: LoadBalanceManager.java, v 0.1 2013-6-19 下午4:26:28 jianhuihuang
 *          Exp $
 */
public class LoadBalanceManager {

	private static final Logger logger = Logger.getLogger(LoadBalanceManager.class);

	private static Map<String, LoadBalance> loadBalanceMap = new HashMap<String, LoadBalance>();

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
	 * @param metaData
	 * @param callType
	 * @return
	 */
	public static LoadBalance getLoadBalance(InvokerMetaData metaData, int callType) {
		LoadBalance loadBalance = loadBalanceMap.get(metaData.getLoadbalance());
		if (loadBalance != null) {
			return loadBalance;
		}

		String serviceId = metaData.getServiceName();
		if (metaData.getGroup() != null) {
			serviceId = serviceId + ":" + metaData.getGroup();
		}
		loadBalance = loadBalanceMap.get(serviceId);
		if (loadBalance != null) {
			return loadBalance;
		}

		String balanceConfigFromLion = RemotingConfigurer.getLoadBalance();
		if (balanceConfigFromLion != null) {
			LoadBalance balanceFromLion = loadBalanceMap.get(balanceConfigFromLion);
			if (balanceFromLion != null) {
				loadBalanceMap.put(metaData.getLoadbalance(), balanceFromLion);
				return balanceFromLion;
			} else {
				logError("Loadbalance[" + balanceConfigFromLion + "] set in lion is invalid, only support "
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
					throw new RuntimeException("Loadbalance[" + loadBalance + "] registered by service["
							+ serviceId + "] is not supported.");
				}
				loadBlanceObj = loadBalanceMap.get(loadBalance);
			} else if (loadBalance instanceof Class) {
				Class<? extends LoadBalance> loadBalanceClass = (Class<? extends LoadBalance>) loadBalance;
				try {
					loadBlanceObj = loadBalanceClass.newInstance();
				} catch (Exception e) {
					throw new RuntimeException("Register loadbalance[service=" + serviceId + ", class="
							+ loadBalance + "] failed.", e);
				}
			}
		}
		if (loadBlanceObj != null) {
			loadBalanceMap.put(serviceId, loadBlanceObj);
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

}
