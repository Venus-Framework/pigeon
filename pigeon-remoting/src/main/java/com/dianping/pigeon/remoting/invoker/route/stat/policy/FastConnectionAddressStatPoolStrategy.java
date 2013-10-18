/**
 * Dianping.com Inc.
 * Copyright (c) 2005-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.invoker.route.stat.policy;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.dianping.pigeon.remoting.invoker.route.stat.DpsfAddressStatPool;
import com.dianping.pigeon.remoting.invoker.route.stat.DpsfAddressStatPool.AddressStat;
import com.dianping.pigeon.remoting.invoker.route.stat.support.AddressConstant;

/**
 * 
 * 
 * @author jianhuihuang
 * @version $Id: FastConnectionAddressStatPoolStrategy.java, v 0.1 2013-6-21
 *          下午3:07:01 jianhuihuang Exp $
 */
public class FastConnectionAddressStatPoolStrategy implements DpsfAddressStatPoolStrategy {

	// 平均调用时间
	private float avgInvockTime = 0.0f;

	// TODO

	private static final float factorValue = 2; // 链接的时间必须是小于2倍的平均时间

	private static final Logger logger = Logger.getLogger(FastConnectionAddressStatPoolStrategy.class.getName());

	public void analysis(DpsfAddressStatPool addressStatPool, Map<String, AddressStat> copyAddressStats) {
		if (copyAddressStats.size() == 0)
			return;
		List<AddressStat> arrayList = new ArrayList<AddressStat>(copyAddressStats.values());
		// 按照平均处理时间计算稳定和隔离列表
		long totalTime = 0L;
		long totalNum = 0L;
		for (AddressStat addressStat : arrayList) {
			totalTime += addressStat.invoke_time_per_min.longValue();
			totalNum += addressStat.invoke_count_per_min.longValue();
		}
		if (totalNum > 0) {
			this.avgInvockTime = totalTime / (totalNum * 1.0f);
		}
		List<String> tempStableIps = new ArrayList<String>();

		float limit = this.avgInvockTime * factorValue;
		for (Iterator<Entry<String, AddressStat>> iterator = copyAddressStats.entrySet().iterator(); iterator.hasNext();) {
			Entry<String, AddressStat> addressStatEntry = iterator.next();
			String key = addressStatEntry.getKey();
			AddressStat value = addressStatEntry.getValue();
			// 如果本周期内调用过，那就计算时间，否则就直接加入稳定列表
			if (value.invoke_count_per_min.floatValue() > 0) {
				float avg = value.invoke_time_per_min.floatValue() / value.invoke_count_per_min.floatValue();
				if (avg <= limit) {
					tempStableIps.add(key);
				}
			} else {
				tempStableIps.add(key);
			}
		}
		addressStatPool.setStableIps(getStrategyName(), tempStableIps);
		StringBuffer sb = new StringBuffer();
		sb.append("[").append(getStrategyName()).append(",").append(factorValue).append(",")
				.append(addressStatPool.getAppName()).append(",").append(tempStableIps.size()).append("/")
				.append(addressStatPool.getAddressStats().size()).append("][");
		for (int i = 0; i < tempStableIps.size(); i++) {
			if (i == tempStableIps.size() - 1) {
				sb.append(tempStableIps.get(i));
			} else {
				sb.append(tempStableIps.get(i) + ",");
			}
		}
		sb.append("]");
		logger.info(sb.toString());

	}

	public String getStrategyName() {
		return AddressConstant.BALACNE_FAST;
	}

}
