/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.invoker.route.stat.policy;

import java.util.Map;

import com.dianping.pigeon.remoting.invoker.route.stat.DpsfAddressStatPool;
import com.dianping.pigeon.remoting.invoker.route.stat.DpsfAddressStatPool.AddressStat;

/**
 * 地址状态分析策略
 * 
 * @author jianhuihuang
 * 
 */
public interface DpsfAddressStatPoolStrategy {

	/**
	 * 
	 * @param addressStatPool
	 * @param copyAddressStats
	 */
	void analysis(DpsfAddressStatPool addressStatPool, Map<String, AddressStat> copyAddressStats);

	/**
	 * 
	 * @return
	 */
	String getStrategyName();
}
