/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.invoker.route.stat.policy;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.remoting.invoker.route.stat.DpsfAddressStatPool;
import com.dianping.pigeon.remoting.invoker.route.stat.DpsfAddressStatPool.AddressStat;
import com.dianping.pigeon.remoting.invoker.route.stat.support.AddressConstant;

/**
 * 
 * 
 * @author jianhuihuang
 * 
 */
public class ConcurrencyIsolationAddressStatPoolStrategy implements DpsfAddressStatPoolStrategy {

	protected Object lockObj = new Object();

	private static final Logger logger = LoggerLoader.getLogger(ConcurrencyIsolationAddressStatPoolStrategy.class.getName());

	// TODO 这个参数需要配置在loin里面去。
	private static final float factorValue = 3; // 最大的并发不能大于平均数的3倍。

	public void analysis(DpsfAddressStatPool addressStatPool, Map<String, AddressStat> ip2stat) {
		long sum = 0L;
		for (Iterator<String> it = ip2stat.keySet().iterator(); it.hasNext();) {
			sum += ip2stat.get(it.next()).concurrent.longValue();
		}
		float avgConn = 0;
		if (ip2stat.size() != 0) {
			avgConn = sum / (float) ip2stat.size();
		}

		float limit = factorValue * avgConn;
		List<String> tempInsulateIps = new ArrayList<String>();
		for (Iterator<String> it = ip2stat.keySet().iterator(); it.hasNext();) {
			String ip = it.next();
			if (ip2stat.get(ip).concurrent.floatValue() > limit) {
				tempInsulateIps.add(ip);
			}
		}
		// 目前只是监控，不打开
		// addressStatPool.setInsulateIps(getStrategyName(), tempInsulateIps);
		addressStatPool.setInsulateIps(getStrategyName(), new ArrayList<String>());
		if (tempInsulateIps.size() > 0) {
			StringBuilder sb = new StringBuilder();
			sb.append("[").append(getStrategyName()).append(",").append(factorValue).append(",")
					.append(addressStatPool.getAppName()).append(",").append(tempInsulateIps.size()).append("/")
					.append(addressStatPool.getAddressStats().size()).append("][(");
			for (int i = 0; i < tempInsulateIps.size(); i++) {
				if (i == tempInsulateIps.size() - 1) {
					sb.append(tempInsulateIps.get(i) + "");
				} else {
					sb.append(tempInsulateIps.get(i) + ",");
				}
			}
			sb.append(")]");
			logger.info(sb.toString());
		}

	}

	public String getStrategyName() {
		return AddressConstant.ISOLATION_CONCURRENCY;
	}

}
