package com.dianping.pigeon.remoting.provider.process.statistics;

import java.io.Serializable;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.dianping.pigeon.log.LoggerLoader;
import org.apache.logging.log4j.Logger;

import com.dianping.pigeon.config.ConfigManagerLoader;
import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.provider.process.RequestProcessorFactory;

public class ProviderCapacityBucket implements Serializable {
	private static final Logger logger = LoggerLoader.getLogger(ProviderCapacityBucket.class);

	private AtomicInteger requests = new AtomicInteger();

	private Map<Integer, AtomicInteger> totalRequestsInSecond = new ConcurrentHashMap<Integer, AtomicInteger>();

	private Map<Integer, AtomicInteger> totalRequestsInMinute = new ConcurrentHashMap<Integer, AtomicInteger>();

	public static final boolean enableMinuteStats = ConfigManagerLoader.getConfigManager().getBooleanValue(
			"pigeon.providerstat.minute.enable", true);

	public static final boolean enableThreadsStats = ConfigManagerLoader.getConfigManager().getBooleanValue(
			"pigeon.providerstat.threads.enable", true);

	private Map<Integer, Set<String>> totalThreadsInSecond = new ConcurrentHashMap<Integer, Set<String>>();

	public static void init() {
	}

	public ProviderCapacityBucket(String address) {
		preFillData();
	}

	public void flowIn(InvocationRequest request) {
		Calendar now = Calendar.getInstance();
		requests.incrementAndGet();
		int second = now.get(Calendar.SECOND);
		incrementTotalRequestsInSecond(second);
		if (enableThreadsStats && !Constants.PROCESS_MODEL_THREAD.equals(RequestProcessorFactory.PROCESS_TYPE)) {
			incrementTotalThreadsInSecond(second);
		}
		if (enableMinuteStats) {
			int minute = now.get(Calendar.MINUTE);
			incrementTotalRequestsInMinute(minute);
		}
	}

	public void flowOut(InvocationRequest request) {
		requests.decrementAndGet();
	}

	public int getCurrentRequests() {
		return requests.get();
	}

	public Map<Integer, AtomicInteger> getTotalRequestsInSecond() {
		return totalRequestsInSecond;
	}

	public int getRequestsInCurrentSecond() {
		Calendar cal = Calendar.getInstance();
		int lastSecond = cal.get(Calendar.SECOND);
		AtomicInteger counter = totalRequestsInSecond.get(lastSecond);
		return counter != null ? counter.get() : 0;
	}

	public int getRequestsInLastSecond() {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.SECOND, -1);
		int lastSecond = cal.get(Calendar.SECOND);
		AtomicInteger counter = totalRequestsInSecond.get(lastSecond);
		return counter != null ? counter.get() : 0;
	}

	public int getRequestsInLastMinute() {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MINUTE, -1);
		int lastMinute = cal.get(Calendar.MINUTE);
		lastMinute = lastMinute >= 0 ? lastMinute : lastMinute + 60;
		AtomicInteger counter = totalRequestsInMinute.get(lastMinute);
		return counter != null ? counter.get() : 0;
	}

	public Set<String> getThreadsInLastSecond() {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.SECOND, -1);
		int lastSecond = cal.get(Calendar.SECOND);
		lastSecond = lastSecond >= 0 ? lastSecond : lastSecond + 60;
		return totalThreadsInSecond.get(lastSecond);
	}

	private void incrementTotalRequestsInSecond(int second) {
		AtomicInteger counter = totalRequestsInSecond.get(second);
		if (counter != null) {
			counter.incrementAndGet();
		} else {
			logger.warn("Impossible case happended, second[" + second + "]'s request counter is null.");
		}
	}

	private void incrementTotalRequestsInMinute(int minute) {
		AtomicInteger counter = totalRequestsInMinute.get(minute);
		if (counter != null) {
			counter.incrementAndGet();
		} else {
			logger.warn("Impossible case happended, day[" + minute + "]'s request counter is null.");
		}
	}

	private void incrementTotalThreadsInSecond(int second) {
		Set<String> threads = totalThreadsInSecond.get(second);
		if (threads != null) {
			threads.add(Thread.currentThread().getName());
		} else {
			logger.warn("Impossible case happended, second[" + second + "]'s thread counter is null.");
		}
	}

	/**
	 * 重置过期的每秒请求数计数器
	 */
	public void resetRequestsInSecondCounter() {
		int second = Calendar.getInstance().get(Calendar.SECOND);
		int prev3Sec = second - 10;
		for (int i = 1; i <= 30; i++) {
			int prevSec = prev3Sec - i;
			prevSec = prevSec >= 0 ? prevSec : prevSec + 60;
			AtomicInteger counter = totalRequestsInSecond.get(prevSec);
			if (counter != null) {
				counter.set(0);
			}
			if (enableThreadsStats) {
				Set<String> threads = totalThreadsInSecond.get(prevSec);
				if (threads != null) {
					threads.clear();
				}
			}
		}
	}

	public void resetRequestsInMinuteCounter() {
		if (enableMinuteStats) {
			int min = Calendar.getInstance().get(Calendar.MINUTE);
			int prev3Sec = min - 10;
			for (int i = 1; i <= 30; i++) {
				int prevSec = prev3Sec - i;
				prevSec = prevSec >= 0 ? prevSec : prevSec + 60;
				AtomicInteger counter = totalRequestsInMinute.get(prevSec);
				if (counter != null) {
					counter.set(0);
				}
			}
		}
	}

	private void preFillData() {
		for (int sec = 0; sec < 60; sec++) {
			totalRequestsInSecond.put(sec, new AtomicInteger());
		}
		if (enableThreadsStats) {
			for (int sec = 0; sec < 60; sec++) {
				totalThreadsInSecond.put(sec, new HashSet<String>());
			}
		}
		if (enableMinuteStats) {
			for (int min = 0; min < 60; min++) {
				totalRequestsInMinute.put(min, new AtomicInteger());
			}
		}
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("requests-current:").append(requests).append(",requests-currentsecond:")
				.append(getRequestsInCurrentSecond()).append(",requests-lastsecond:").append(getRequestsInLastSecond())
				.toString();
		if (enableMinuteStats) {
			sb.append(",requests-lastminute:").append(getRequestsInLastMinute());
		}
		if (enableThreadsStats) {
			sb.append(",threads-lastsecond:").append(getThreadsInLastSecond());
		}
		return sb.toString();
	}
}
