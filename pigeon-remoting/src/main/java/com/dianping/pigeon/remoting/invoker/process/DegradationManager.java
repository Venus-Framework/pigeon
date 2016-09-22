/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.invoker.process;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import com.dianping.pigeon.log.Logger;
import org.springframework.util.CollectionUtils;

import com.dianping.dpsf.exception.NetTimeoutException;
import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.config.ConfigManagerLoader;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.monitor.Monitor;
import com.dianping.pigeon.monitor.MonitorLoader;
import com.dianping.pigeon.remoting.common.exception.RejectedException;
import com.dianping.pigeon.remoting.invoker.domain.InvokerContext;
import com.dianping.pigeon.remoting.invoker.exception.RemoteInvocationException;
import com.dianping.pigeon.remoting.invoker.exception.RequestTimeoutException;
import com.dianping.pigeon.remoting.invoker.exception.ServiceUnavailableException;
import com.dianping.pigeon.remoting.invoker.route.quality.RequestQualityManager;
import com.dianping.pigeon.remoting.invoker.route.quality.RequestQualityManager.Quality;
import com.dianping.pigeon.threadpool.DefaultThreadFactory;

/**
 * @author xiangwu
 * 
 */
public enum DegradationManager {

	INSTANCE;

	private static final Logger logger = LoggerLoader.getLogger(DegradationManager.class);
	private static final ConfigManager configManager = ConfigManagerLoader.getConfigManager();
	private static ConcurrentHashMap<String, ConcurrentHashMap<Integer, Count>> requestSecondCountMap = new ConcurrentHashMap<String, ConcurrentHashMap<Integer, Count>>();
	private static volatile Map<String, Count> requestCountMap = null;
	private static final String KEY_DEGRADE_FORCE = "pigeon.invoker.degrade.force";
	private static final String KEY_DEGRADE_FAILURE = "pigeon.invoker.degrade.failure";
	private static final String KEY_DEGRADE_AUTO = "pigeon.invoker.degrade.auto";
	private static final String KEY_DEGRADE_RECOVER_PERCENT = "pigeon.invoker.degrade.recover.percent";
	private static final String KEY_DEGRADE_RECOVER_INTERVAL = "pigeon.invoker.degrade.recover.interval";
	private static final String KEY_DEGRADE_THRESHOLD_INVOKE = "pigeon.invoker.degrade.threshold.invoke";
	private static final String KEY_DEGRADE_THRESHOLD_TOTAL = "pigeon.invoker.degrade.threshold.total";
	private static final String KEY_DEGRADE_PERCENT_MAX = "pigeon.invoker.degrade.percent.max";
	private static final String KEY_DEGRADE_CHECK_SECONDS = "pigeon.invoker.degrade.check.seconds";
	private static final String KEY_DEGRADE_CHECK_INTERVAL = "pigeon.invoker.degrade.check.interval";
	private static final ExecutorService checkThreadPool = Executors.newFixedThreadPool(1, new DefaultThreadFactory(
			"Pigeon-Client-Degrade-Checker"));
	private static final Random random = new Random();
	private final Monitor monitor = MonitorLoader.getMonitor();

	private DegradationManager() {
	}

	static {
		configManager.getBooleanValue(KEY_DEGRADE_FORCE, false);
		configManager.getBooleanValue(KEY_DEGRADE_AUTO, false);
		configManager.getIntValue(KEY_DEGRADE_THRESHOLD_TOTAL, 100);
		configManager.getIntValue(KEY_DEGRADE_THRESHOLD_INVOKE, 2);
		configManager.getFloatValue(KEY_DEGRADE_RECOVER_PERCENT, 1);
		configManager.getIntValue(KEY_DEGRADE_RECOVER_INTERVAL, 10);
		configManager.getFloatValue(KEY_DEGRADE_PERCENT_MAX, 99.90f);
		configManager.getIntValue(KEY_DEGRADE_CHECK_SECONDS, 10);
		configManager.getIntValue(KEY_DEGRADE_CHECK_INTERVAL, 2);
		checkThreadPool.execute(new Checker());
	}

	public String getRequestUrl(InvokerContext context) {
		return context.getInvokerConfig().getUrl() + "#" + context.getMethodName();
	}

	public boolean needDegrade(InvokerContext context) {
		if (configManager.getBooleanValue(KEY_DEGRADE_FORCE, false)) {
			return true;
		}

		if (configManager.getBooleanValue(KEY_DEGRADE_FAILURE, false)) {
			return false;
		}

		if (configManager.getBooleanValue(KEY_DEGRADE_AUTO, false)) {
			if (!CollectionUtils.isEmpty(requestCountMap)) {
				String requestUrl = getRequestUrl(context);
				Count count = requestCountMap.get(requestUrl);
				if (count != null) {
					if (count.getTotalValue() >= configManager.getIntValue(KEY_DEGRADE_THRESHOLD_TOTAL, 100)) {
						if ((count.getTotalValue() - count.getDegradedValue()) > configManager.getIntValue(
								KEY_DEGRADE_THRESHOLD_INVOKE, 2)
								&& count.getFailedPercent() < configManager.getFloatValue(KEY_DEGRADE_RECOVER_PERCENT,
										1)) {
							return random(count.getDegradedPercent()
									- configManager.getIntValue(KEY_DEGRADE_RECOVER_INTERVAL, 10));
						} else if (count.getFailedPercent() >= configManager.getFloatValue(KEY_DEGRADE_RECOVER_PERCENT,
								1)) {
							return random(configManager.getFloatValue(KEY_DEGRADE_PERCENT_MAX, 99.90f));
						}
					}
				}
			}
		}
		return false;
	}

	private boolean random(float percent) {
		return random.nextInt(10000) < percent * 100;
	}

	public void addFailedRequest(InvokerContext context, Throwable t) {
		if (t instanceof ServiceUnavailableException || t instanceof NetTimeoutException
				|| t instanceof RequestTimeoutException || t instanceof RemoteInvocationException
				|| t instanceof RejectedException) {
			addRequest(context, t, false);
		}
	}

	public void addDegradedRequest(InvokerContext context) {
		addRequest(context, null, true);
	}

	private void addRequest(InvokerContext context, Throwable t, boolean degraded) {
		if (configManager.getBooleanValue(KEY_DEGRADE_AUTO, false)
				|| configManager.getBooleanValue(KEY_DEGRADE_FORCE, false)) {
			int currentSecond = Calendar.getInstance().get(Calendar.SECOND);
			String requestUrl = getRequestUrl(context);
			ConcurrentHashMap<Integer, Count> secondCount = requestSecondCountMap.get(requestUrl);
			if (secondCount == null) {
				secondCount = new ConcurrentHashMap<Integer, Count>();
				ConcurrentHashMap<Integer, Count> last = requestSecondCountMap.putIfAbsent(requestUrl, secondCount);
				if (last != null) {
					secondCount = last;
				}
			}
			Count count = secondCount.get(currentSecond);
			if (count == null) {
				count = new Count(0, 0, 0);
				Count last = secondCount.putIfAbsent(currentSecond, count);
				if (last != null) {
					count = last;
				}
			}
			count.total.incrementAndGet();
			if (t != null) {
				count.failed.incrementAndGet();
			}
			if (degraded) {
				count.degraded.incrementAndGet();
				context.setDegraded();
			}
		}
	}

	public boolean needFailureDegrade() {
		return configManager.getBooleanValue(KEY_DEGRADE_FAILURE, false);
	}

	public static class DegradeActionConfig implements Serializable {

		private static final long serialVersionUID = 1L;

		private String returnClass;
		private String componentClass;
		private String keyClass;
		private String valueClass;
		private String content;
		private boolean throwException = false;
		private boolean useMockClass = false;
		private boolean useGroovyScript = false;

		public boolean getUseGroovyScript() {
			return useGroovyScript;
		}

		public void setUseGroovyScript(boolean useGroovyScript) {
			this.useGroovyScript = useGroovyScript;
		}

		public boolean getUseMockClass() {
			return useMockClass;
		}

		public void setUseMockClass(boolean useMockClass) {
			this.useMockClass = useMockClass;
		}

		public boolean getThrowException() {
			return throwException;
		}

		public void setThrowException(boolean throwException) {
			this.throwException = throwException;
		}

		public DegradeActionConfig() {
		}

		public String getReturnClass() {
			return returnClass;
		}

		public void setReturnClass(String returnClass) {
			this.returnClass = returnClass;
		}

		public String getComponentClass() {
			return componentClass;
		}

		public void setComponentClass(String componentClass) {
			this.componentClass = componentClass;
		}

		public String getKeyClass() {
			return keyClass;
		}

		public void setKeyClass(String keyClass) {
			this.keyClass = keyClass;
		}

		public String getValueClass() {
			return valueClass;
		}

		public void setValueClass(String valueClass) {
			this.valueClass = valueClass;
		}

		public String getContent() {
			return content;
		}

		public void setContent(String content) {
			this.content = content;
		}

	}

	private static class Count {
		private AtomicInteger failed = new AtomicInteger();
		private AtomicInteger total = new AtomicInteger();
		private AtomicInteger degraded = new AtomicInteger();

		public Count() {
		}

		public Count(int total, int failed, int degraded) {
			this.total.set(total);
			this.failed.set(failed);
			this.degraded.set(degraded);
		}

		public AtomicInteger getFailed() {
			return failed;
		}

		public int getFailedValue() {
			return failed.get();
		}

		public void setFailed(int failed) {
			this.failed.set(failed);
		}

		public AtomicInteger getDegraded() {
			return degraded;
		}

		public int getDegradedValue() {
			return degraded.get();
		}

		public void setDegraded(int degraded) {
			this.degraded.set(degraded);
		}

		public AtomicInteger getTotal() {
			return total;
		}

		public int getTotalValue() {
			return total.get();
		}

		public void setTotal(int total) {
			this.total.set(total);
		}

		public Count merge(Count count) {
			Count n = new Count();
			n.total.set(this.total.get() + count.total.get());
			n.failed.set(this.failed.get() + count.failed.get());
			n.degraded.set(this.degraded.get() + count.degraded.get());
			return n;
		}

		public float getFailedPercent() {
			int m = (total.get() - degraded.get());
			if (total.get() > 0 && m > 0) {
				return failed.get() * 100 / m;
			} else {
				return 0;
			}
		}

		public float getDegradedPercent() {
			if (total.get() > 0) {
				return degraded.get() * 100 / total.get();
			} else {
				return 0;
			}
		}

		public void clear() {
			total.set(0);
			failed.set(0);
			degraded.set(0);
		}
	}

	static class Checker implements Runnable {

		@Override
		public void run() {
			while (true) {
				try {
					Thread.sleep(1000 * configManager.getIntValue(KEY_DEGRADE_CHECK_INTERVAL, 2));
					checkRequestSecondCount();
				} catch (Exception e) {
					logger.error("", e);
				}
			}
		}

		private void checkRequestSecondCount() {
			Map<String, Count> countMap = new ConcurrentHashMap<String, Count>();
			final int recentSeconds = configManager.getIntValue(KEY_DEGRADE_CHECK_SECONDS, 10);
			final int currentSecond = Calendar.getInstance().get(Calendar.SECOND);

			for (String url : requestSecondCountMap.keySet()) {
				Map<Integer, Count> secondCount = requestSecondCountMap.get(url);
				int total = 0, failed = 0, degraded = 0;
				for (int i = 1; i <= recentSeconds; i++) {
					int prevSec = currentSecond - i;
					prevSec = prevSec >= 0 ? prevSec : prevSec + 60;
					Count ct = secondCount.get(prevSec);
					if (ct != null) {
						total += ct.getTotalValue();
						failed += ct.getFailedValue();
						degraded += ct.getDegradedValue();
					}
				}
				countMap.put(url, new Count(total, failed, degraded));
				// clear previous seconds
				for (int i = recentSeconds + 1; i <= recentSeconds + 20; i++) {
					int prevSec = currentSecond - i;
					prevSec = prevSec >= 0 ? prevSec : prevSec + 60;
					Count ct = secondCount.get(prevSec);
					if (ct != null) {
						ct.clear();
					}
				}
			}
			Map<String, Count> old = requestCountMap;
			requestCountMap = countMap;
			if (old != null) {
				old.clear();
				old = null;
			}

			// 复用降级统计和清空的线程，用于服务质量统计和清空（窗口默认为10秒）
			ConcurrentHashMap<String, ConcurrentHashMap<String, ConcurrentHashMap<Integer, Quality>>> addrReqUrlSecondQualities = RequestQualityManager.INSTANCE
					.getAddrReqUrlSecondQualities();
			ConcurrentHashMap<String, ConcurrentHashMap<String, Quality>> addrReqUrlQualities = new ConcurrentHashMap<String, ConcurrentHashMap<String, Quality>>();

			for (String address : addrReqUrlSecondQualities.keySet()) {
				ConcurrentHashMap<String, ConcurrentHashMap<Integer, Quality>> reqUrlSecondQualities = addrReqUrlSecondQualities
						.get(address);

				ConcurrentHashMap<String, Quality> reqUrlQualities = new ConcurrentHashMap<String, Quality>();
				for (String requestUrl : reqUrlSecondQualities.keySet()) {
					ConcurrentHashMap<Integer, Quality> secondQualities = reqUrlSecondQualities.get(requestUrl);
					int total = 0, failed = 0;
					for (int i = 1; i <= recentSeconds; i++) {
						int prevSec = currentSecond - i;
						prevSec = prevSec >= 0 ? prevSec : prevSec + 60;
						Quality quality = secondQualities.get(prevSec);

						if (quality != null) {
							total += quality.getTotalValue();
							failed += quality.getFailedValue();
						}
					}

					reqUrlQualities.put(requestUrl, new Quality(total, failed));

					// clear previous seconds
					for (int i = recentSeconds + 1; i <= recentSeconds + 20; i++) {
						int prevSec = currentSecond - i;
						prevSec = prevSec >= 0 ? prevSec : prevSec + 60;
						Quality quality = secondQualities.get(prevSec);

						if (quality != null) {
							quality.clear();
						}
					}
				}

				addrReqUrlQualities.put(address, reqUrlQualities);
			}

			RequestQualityManager.INSTANCE.setAddrReqUrlQualities(addrReqUrlQualities);
		}

	}
}
