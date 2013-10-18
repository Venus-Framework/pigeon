/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.invoker.route.stat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.dianping.pigeon.remoting.invoker.route.stat.policy.DpsfAddressStatPoolStrategy;
import com.dianping.pigeon.remoting.invoker.route.stat.policy.ExceptionIsolationAddressStatPoolStrategy;
import com.dianping.pigeon.remoting.invoker.route.stat.support.AddressStatUtil;

/**
 * 
 * @author jianhuihuang
 * 
 */
public class DpsfAddressStatPool {

	// 异步执行分析算法的间隔时间（设置的时间必须可以让60整除）
	private int analysisPeriodSec = 5;

	// 分析算法是否启动
	private AtomicBoolean analysisStarted = new AtomicBoolean(false);

	// 多应用情况下的所属应用
	private String belongto;

	// 该地址对应的appName
	private String appName;

	// IP到其统计的数据映射
	protected Map<String, AddressStat> addressStats = new ConcurrentHashMap<String, AddressStat>(50);

	// 地址池策略
	protected ConcurrentHashMap<String, DpsfAddressStatPoolStrategy> addressStatPoolStrategies = new ConcurrentHashMap<String, DpsfAddressStatPoolStrategy>(
			4);

	// 当前可用的地址
	private List<String> newAllAvailableAddresses = new ArrayList<String>();

	// 具体策略，如fase，least的对应稳定的IP列表
	protected Map<String, List<String>> stable_ips = new HashMap<String, List<String>>();

	// 具体策略exception，concurrency对应的隔离IP地址
	protected Map<String, List<String>> insulate_ips = new HashMap<String, List<String>>();

	// 互斥锁
	private Lock refreshLock = new ReentrantLock();

	// 管理地址池的地址池服务
	private DpsfAddressStatPoolService dpsfAddressStatPoolService;

	public DpsfAddressStatPool(DpsfAddressStatPoolService dpsfAddressStatPoolService) {
		this.dpsfAddressStatPoolService = AddressStatPoolServiceImpl.getInstance();
		// TODO,FIXME,应该在pigeon启动的时候设置最好！！
		addressStatPoolStrategies.put("exceptionIsolation", new ExceptionIsolationAddressStatPoolStrategy());

	}

	/**
	 * @return the belongto
	 */
	public String getBelongto() {
		return belongto;
	}

	/**
	 * @param belongto
	 *            the belongto to set
	 */
	public void setBelongto(String belongto) {
		this.belongto = belongto;
	}

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public void setStableIps(String strategy, List<String> ips) {
		stable_ips.put(strategy, ips);
	}

	public List<String> getStableIps(String strategy) {
		return stable_ips.get(strategy);
	}

	public void setInsulateIps(String strategy, List<String> ips) {
		insulate_ips.put(strategy, ips);
	}

	public List<String> getInsulateIps(String strategy) {
		return insulate_ips.get(strategy);
	}

	public Map<String, AddressStat> getAddressStats() {
		return this.addressStats;
	}

	public void setAllAvailableAddresses(List<String> addresses) {
		this.newAllAvailableAddresses = addresses;
	}

	public List<String> getAllAvaliableAddresses() {
		return this.newAllAvailableAddresses;
	}

	/**
	 * 执行调用时的操作，主要是地址并发量，递增该周期内的调用次数。
	 * 
	 * @param ip
	 * @return
	 */
	public AddressStat use(String ip) {

		AddressStat addressStat = addressStats.get(ip);
		// 应该不会发生
		if (addressStat == null) {
			return null;
		}
		// 并发数递增
		AddressStatUtil.countsIncrementAndGet(addressStat, 1);
		// 每分钟的调用次数递增
		AddressStatUtil.invokeNumsPerMinAddAndGet(addressStat, 1);
		return addressStat;
	}

	/**
	 * 调用完，释放该ip，计算并发数，调用耗时情况
	 * 
	 * @param ip
	 * @param duration
	 * @return
	 */
	public AddressStat release(String ip, long duration) {
		AddressStat addressStat = addressStats.get(ip);

		if (addressStat == null) {
			return null;
		}
		// 递减并发数
		AddressStatUtil.countsDecrementAndGet(addressStat);
		// 计算这次调用的耗时
		AddressStatUtil.timePerInvokeAddAndGet(addressStat, duration);
		return addressStat;
	}

	/**
	 * 记录该IP地址的错误情况
	 * 
	 * @param ip
	 * @return
	 * @see com.dianping.pigeon.route.stat.DpsfAddressStatPool.sofa.service.hsf.service.route.SofaAddressStatPool#error(java.lang.String)
	 */
	public AddressStat error(String ip) {
		AddressStat addressStat = addressStats.get(ip);
		if (addressStat == null) {
			return null;
		}
		// 异常数递增
		AddressStatUtil.exceptionIncrementAndGet(addressStat, 1);
		return addressStat;
	}

	/**
	 * 重置负载均衡的策略列表和隔离列表
	 * 
	 * @param balancePolicies
	 *            负载均衡策略
	 * @param isolationPolicies
	 *            隔离列表策略ß
	 */
	public void reset(String[] balancePolicies, String[] isolationPolicies) {
		refreshLock.lock(); // 避免多个线程同时reset
		try {
			for (int i = 0; i < balancePolicies.length; i++) {
				stable_ips.put(balancePolicies[i], newAllAvailableAddresses);
			}
			for (int i = 0; i < isolationPolicies.length; i++) {
				insulate_ips.put(isolationPolicies[i], new ArrayList<String>());
			}
			ConcurrentHashMap<String/* ip */, AddressStat> addressStatsCopy = new ConcurrentHashMap<String, AddressStat>(
					50);
			// 找出新增加的地址列表
			// 防止在迭代过程中newAllAvailableAddresses修改，先copy出来
			List<String> availableAddresses = this.newAllAvailableAddresses;

			Map<String, AddressStat> copy = new ConcurrentHashMap<String, AddressStat>(addressStats);
			for (Iterator<String> iterator = availableAddresses.iterator(); iterator.hasNext();) {
				String ip = iterator.next();
				if (copy.containsKey(ip)) {
					addressStatsCopy.put(ip, copy.get(ip));
				} else {
					addressStatsCopy.put(ip, new AddressStat());
				}
			}
			// 应用切换
			this.addressStats = addressStatsCopy;
		} finally {
			refreshLock.unlock();
		}
	}

	public DpsfAddressStatPoolStrategy getStrategy(String policy) {
		return addressStatPoolStrategies.get(policy);
	}

	public void addStrategy(String policy, DpsfAddressStatPoolStrategy strategy) {
		addressStatPoolStrategies.putIfAbsent(policy, strategy);
	}

	/**
	 * 启动各个分析算法
	 * 
	 * @param analysisScheduler
	 */
	public void startAnalysis(ScheduledThreadPoolExecutor analysisScheduler) {

		class AnalysisRunner implements Runnable {

			// 应用策略地址池
			private DpsfAddressStatPool addressStatPool;
			// 目前执行了几次
			private int count;
			// 一分钟可以执行几次
			private int times;

			public AnalysisRunner(DpsfAddressStatPool addressStatPool, int interval) {
				this.addressStatPool = addressStatPool;
				this.times = 60 / interval;
			}

			public void run() {
				// copy一份数据，确保大家的算法是一份数据
				Map<String, AddressStat> map = addressStatPool.getAddressStats();
				Map<String, AddressStat> copyAddressStats = new ConcurrentHashMap<String, AddressStat>();
				Set<String> kset = map.keySet();
				if (kset != null) {
					Iterator<String> its = kset.iterator();
					while (its.hasNext()) {
						String key = its.next();
						AddressStat addressStat = map.get(key);
						AddressStat as = (AddressStat) addressStat.clone();
						copyAddressStats.put(key, as);
					}
					// 策略算法执行
					Iterator<String> it = addressStatPoolStrategies.keySet().iterator();
					while (it.hasNext()) {
						String strategy_name = it.next();
						DpsfAddressStatPoolStrategy strategy = addressStatPoolStrategies.get(strategy_name);
						strategy.analysis(addressStatPool, copyAddressStats);
					}
				}
				// 打印统计信息
				count++;
				if (count == times) {
					dpsfAddressStatPoolService.printAddressStatInfo(addressStatPool, true);
					count = 0;
				} else {
					dpsfAddressStatPoolService.printAddressStatInfo(addressStatPool, false);
				}
			}
		}

		if (analysisStarted.compareAndSet(false, true)) {
			// 上面已经copy了，不需要锁了。
			Calendar cal = new GregorianCalendar();
			analysisScheduler.scheduleAtFixedRate(new AnalysisRunner(this, analysisPeriodSec),
					(60 - cal.get(Calendar.SECOND)) * 1000, (long) analysisPeriodSec * 1000, TimeUnit.MILLISECONDS);
		}
	}

	/**
	 * 
	 * @author jianhuihuang
	 * 
	 */
	public static class AddressStat implements Cloneable {
		// 当前并发数
		public AtomicLong concurrent = new AtomicLong(0L);

		// 当前异常数
		public AtomicLong exceptions = new AtomicLong(0L);

		// 每分钟调用的总时间
		public AtomicLong invoke_time_per_min = new AtomicLong(0L);

		// 每分钟调用总数
		public AtomicLong invoke_count_per_min = new AtomicLong(0L);

		public long invoke_timeMillis;

		// 平均耗时=每分钟的调用总时间/每分钟的调用总数
		public long avg_time_per_request;

		// 上一分钟的invoke_count_per_min
		public long qpm;

		// qpm/60
		public long qps;

		public String toString() {
			return "(concurrent=" + concurrent + ",exceptions=" + exceptions + ",invoke_count_per_min="
					+ invoke_count_per_min + ",invoke_time_per_min=" + invoke_time_per_min + ",qpm=" + qpm + ",qps="
					+ qps + ")";
		}

		public Object clone() {
			try {
				return super.clone();
			} catch (CloneNotSupportedException e) {

			}
			AddressStat as = new AddressStat();
			as.exceptions = this.exceptions;
			as.invoke_count_per_min = this.invoke_count_per_min;
			as.invoke_time_per_min = this.invoke_time_per_min;
			as.qpm = this.qpm;
			as.qps = this.qps;
			return as;
		}
	}
}
