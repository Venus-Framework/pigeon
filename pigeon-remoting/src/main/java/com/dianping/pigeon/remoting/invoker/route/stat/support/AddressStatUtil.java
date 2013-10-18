/**
 * Dianping.com Inc.
 * Copyright (c) 2005-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.invoker.route.stat.support;

import com.dianping.pigeon.remoting.invoker.route.stat.DpsfAddressStatPool.AddressStat;

/**
 * 地址池属性操作
 * 
 * @author jianhuihuang
 * 
 */
public class AddressStatUtil {

	/**
	 * 递增并发数
	 * 
	 * @param addressStat
	 * @return
	 */
	public static AddressStat countsDecrementAndGet(AddressStat addressStat) {
		addressStat.concurrent.decrementAndGet();
		return addressStat;
	}

	/**
	 * 递减并发数
	 * 
	 * @param addressStat
	 * @param value
	 * @return
	 */
	public static AddressStat countsIncrementAndGet(AddressStat addressStat, long value) {
		addressStat.invoke_timeMillis = System.currentTimeMillis();
		addressStat.concurrent.addAndGet(value);
		return addressStat;
	}

	/**
	 * 递增每分钟调用次数
	 * 
	 * @param addressStat
	 * @param value
	 * @return
	 */
	public static AddressStat invokeNumsPerMinAddAndGet(AddressStat addressStat, long value) {
		addressStat.invoke_count_per_min.addAndGet(value);
		return addressStat;
	}

	/**
	 * 增加这次调用时间
	 * 
	 * @param addressStat
	 * @param value
	 * @return
	 */
	public static AddressStat timePerInvokeAddAndGet(AddressStat addressStat, long value) {
		addressStat.invoke_time_per_min.addAndGet(value);
		return addressStat;
	}

	/**
	 * 增加异常增加数
	 * 
	 * @param addressStat
	 * @param value
	 * @return
	 */
	public static AddressStat exceptionIncrementAndGet(AddressStat addressStat, long value) {
		addressStat.exceptions.addAndGet(value);
		return addressStat;
	}

}
