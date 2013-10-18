/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.invoker.route.stat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.dianping.pigeon.remoting.invoker.route.stat.DpsfAddressStatPool.AddressStat;

/**
 * 
 * @author jianhuihuang
 * 
 */
public class AddressStatPoolServiceImpl extends AbstractAddressStatPoolService {

	private static DpsfAddressStatPoolService dpsfAddressStatPoolService = new AddressStatPoolServiceImpl();

	public static DpsfAddressStatPoolService getInstance() {

		return dpsfAddressStatPoolService;
	}

	public List<Map.Entry<String, AddressStat>> sort(Map<String, AddressStat> orignal) {
		List<Map.Entry<String, AddressStat>> sortList = new ArrayList<Map.Entry<String, AddressStat>>(
				orignal.entrySet());
		Collections.sort(sortList, new Comparator<Map.Entry<String, AddressStat>>() {
			public int compare(Map.Entry<String, AddressStat> o1, Map.Entry<String, AddressStat> o2) {
				if (o2.getValue().concurrent.longValue() > o1.getValue().concurrent.longValue()) {
					return 1;
				} else if (o1.getValue().concurrent.longValue() > o2.getValue().concurrent.longValue()) {
					return -1;
				} else {
					return 0;
				}
			}
		});
		return sortList;
	}

	public void reset(DpsfAddressStatPool addressStatPool) {
		Set<String> keySet = new HashSet<String>();
		Map<String, AddressStat> addressStats = addressStatPool.getAddressStats();
		keySet.addAll(addressStats.keySet());
		for (Iterator<String> it = keySet.iterator(); it.hasNext();) {
			String ip = it.next();
			AddressStat addedAddressState = addressStats.get(ip);
			if (addedAddressState != null) {
				// qpm/qps/tpr
				addedAddressState.qpm = addedAddressState.invoke_time_per_min.get();
				addedAddressState.qps = addedAddressState.qpm / 60;
				long time = addedAddressState.invoke_time_per_min.get();
				long count = addedAddressState.qpm;
				if (count == 0) {
					count = 1;
				}
				addedAddressState.avg_time_per_request = time / count;
				addedAddressState.invoke_count_per_min.set(0L);
				addedAddressState.invoke_time_per_min.set(0L);
				addedAddressState.exceptions.set(0L);
			}
		}
	}
}
