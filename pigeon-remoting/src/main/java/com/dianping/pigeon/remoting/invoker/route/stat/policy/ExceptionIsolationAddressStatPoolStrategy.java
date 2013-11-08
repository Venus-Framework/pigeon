/**
 * Dianping.com Inc.
 * Copyright (c) 2005-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.invoker.route.stat.policy;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.dianping.pigeon.monitor.Log4jLoader;
import com.dianping.pigeon.remoting.invoker.route.stat.DpsfAddressStatPool;
import com.dianping.pigeon.remoting.invoker.route.stat.DpsfAddressStatPool.AddressStat;
import com.dianping.pigeon.remoting.invoker.route.stat.support.AddressConstant;

/**
 * 
 * 
 * @author jianhuihuang
 * @version $Id: ExceptionIsolationAddressStatPoolStrategy.java, v 0.1 2013-6-21
 *          下午3:06:54 jianhuihuang Exp $
 */
public class ExceptionIsolationAddressStatPoolStrategy implements DpsfAddressStatPoolStrategy {

	protected Object lockObj = new Object();

	private static final Logger logger = Log4jLoader.getLogger(ExceptionIsolationAddressStatPoolStrategy.class

	.getName());

	// TODO 需要测试一下，隔离的适合需要单独启动线程来做测试验证.....
	// TODO 这个参数需要配置在loin里面去。
	private static final float factorValue = 2; // 最大的异常数不能大于平均数的10倍。

	public void analysis(DpsfAddressStatPool addressStatPool, Map<String, AddressStat> ip2stat) {

		if (logger.isInfoEnabled()) {
			logger.info("analysis the exception isolation address stat pool strategy.....");
		}

		long sum = 0L;
		for (Iterator<String> it = ip2stat.keySet().iterator(); it.hasNext();) {
			sum += ip2stat.get(it.next()).exceptions.longValue();
		}
		float avgConn = 0;
		if (ip2stat.size() != 0) {
			avgConn = sum / (float) ip2stat.size();
		}

		float limit = factorValue * avgConn;
		List<String> tempInsulateIps = new ArrayList<String>();
		for (Iterator<String> it = ip2stat.keySet().iterator(); it.hasNext();) {
			String ip = it.next();
			if (ip2stat.get(ip).exceptions.floatValue() > limit) {
				tempInsulateIps.add(ip);
			}
		}

		List<String> oldInsulateIps = addressStatPool.getInsulateIps(getStrategyName());
		List<String> tempOldInsulateIps = new ArrayList<String>();
		for (String ip : oldInsulateIps) {
			AddressStat addressStat = ip2stat.get(ip);
			if (System.currentTimeMillis() - addressStat.invoke_timeMillis < 10000)// 10s的时间移除一次。
			{
				tempOldInsulateIps.add(ip);
			}
		}
		tempInsulateIps.addAll(tempOldInsulateIps);
		addressStatPool.setInsulateIps(getStrategyName(), tempInsulateIps);
		// TODO, 需要把原来的隔离IP清除掉。
		// addressStatPool.setInsulateIps(getStrategyName(),
		// new ArrayList<String>());
		if (tempInsulateIps.size() > 0) {
			StringBuilder sb = new StringBuilder();
			sb.append("[").append(getStrategyName()).append(",").append(factorValue).append(",")
					.append(addressStatPool.getAppName()).append(",").append(tempInsulateIps.size()).append("/")
					.append(addressStatPool.getAddressStats().size()).append("][(");
			for (int i = 0; i < tempInsulateIps.size(); i++) {
				if (i == tempInsulateIps.size() - 1) {
					sb.append(tempInsulateIps.get(i));
				} else {
					sb.append(tempInsulateIps.get(i) + ",");
				}
			}
			sb.append(")]");
			logger.info(sb.toString());
		}

	}

	public String getStrategyName() {
		return AddressConstant.ISOLATION_EXCEPTION;
	}

}
