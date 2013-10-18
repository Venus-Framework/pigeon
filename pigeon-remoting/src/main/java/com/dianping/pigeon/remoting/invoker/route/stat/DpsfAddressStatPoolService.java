/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.invoker.route.stat;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * 
 * @author jianhuihuang
 * 
 */
public interface DpsfAddressStatPoolService {

	/**
	 * 
	 * @param policy
	 * @param addressStatPool
	 */
	void addStrategy(String policy, DpsfAddressStatPool addressStatPool);

	/**
	 * 
	 * @param appName
	 * @return
	 */
	DpsfAddressStatPool getAddressStatPool(String appName);

	/**
	 * 
	 * @return
	 */
	Map<String, DpsfAddressStatPool> getAddressStatPools();

	/**
	 * 
	 * @param appName
	 * @param policy
	 * @return
	 */
	List<String> getStableIps(String appName, String policy);

	/**
	 * 
	 * @param appName
	 * @param policy
	 * @return
	 */
	List<String> getInsulateIps(String appName, String policy);

	/**
	 * 
	 * @param addressStatPool
	 * @param resetAddrStat
	 */
	void printAddressStatInfo(DpsfAddressStatPool addressStatPool, boolean resetAddrStat);

	/**
	 * 
	 * @return
	 */
	ScheduledThreadPoolExecutor getScheduledThreadPoolExecutor();

	/**
	 * 
	 * @return
	 */
	String getAppName();
}
