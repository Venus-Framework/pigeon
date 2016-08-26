package com.dianping.pigeon.remoting.invoker.process.statistics;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.dianping.pigeon.log.Logger;

import com.dianping.pigeon.config.ConfigManagerLoader;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.util.ContextUtils;

public class InvokerCapacityBucket implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final Logger logger = LoggerLoader.getLogger(InvokerCapacityBucket.class);

	private AtomicInteger requests = new AtomicInteger();

	private Map<Integer, AtomicInteger> totalRequestsInSecond = new ConcurrentHashMap<Integer, AtomicInteger>();

	private Map<Integer, AtomicInteger> totalRequestsInDay = new ConcurrentHashMap<Integer, AtomicInteger>();

	private Map<Integer, AtomicInteger> totalRequestsInMinute = new ConcurrentHashMap<Integer, AtomicInteger>();

	public static final boolean enableDayStats = ConfigManagerLoader.getConfigManager().getBooleanValue(
			"pigeon.invokerstat.day.enable", true);

	public static final boolean enableMinuteStats = ConfigManagerLoader.getConfigManager().getBooleanValue(
			"pigeon.invokerstat.minute.enable", true);

	public static final boolean enableMethodStats = ConfigManagerLoader.getConfigManager().getBooleanValue(
			"pigeon.invokerstat.method.enable", true);

	private Map<String, String> requestCall = new ConcurrentHashMap<String, String>();

	public static void init() {
	}

	public InvokerCapacityBucket(String address) {
		preFillData();
	}

	public void flowIn(InvocationRequest request) {
		Calendar now = Calendar.getInstance();
		requests.incrementAndGet();
		incrementTotalRequestsInSecond(now.get(Calendar.SECOND));
		if (enableMinuteStats) {
			incrementTotalRequestsInMinute(now.get(Calendar.MINUTE));
		}
		if (enableDayStats) {
			incrementTotalRequestsInDay(now.get(Calendar.DATE));
		}
		if (enableMethodStats) {
			String callUrl = request.getServiceName() + "#" + request.getMethodName();
			if (!requestCall.containsKey(callUrl)) {
				String currentServiceUrl = (String) ContextUtils.getLocalContext("CurrentServiceUrl");
				requestCall.put(callUrl, currentServiceUrl == null ? "" : currentServiceUrl);
			}
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

	public int getRequestsInSecond(int second) {
		AtomicInteger counter = totalRequestsInSecond.get(second);
		return counter != null ? counter.get() : 0;
	}
	
	public int getRequestsInLastSecond() {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.SECOND, -1);
		int lastSecond = cal.get(Calendar.SECOND);
		AtomicInteger counter = totalRequestsInSecond.get(lastSecond);
		return counter != null ? counter.get() : 0;
	}

	public int getRequestsInCurrentSecond() {
		Calendar cal = Calendar.getInstance();
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

	public int getRequestsInLastDay() {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -1);
		int lastDay = cal.get(Calendar.DATE);
		AtomicInteger counter = totalRequestsInDay.get(lastDay);
		return counter != null ? counter.get() : 0;
	}

	public int getRequestsInToday() {
		Calendar cal = Calendar.getInstance();
		int day = cal.get(Calendar.DATE);
		AtomicInteger counter = totalRequestsInDay.get(day);
		return counter != null ? counter.get() : 0;
	}

	private void incrementTotalRequestsInDay(int day) {
		AtomicInteger counter = totalRequestsInDay.get(day);
		if (counter != null) {
			counter.incrementAndGet();
		} else {
			logger.warn("Impossible case happended, day[" + day + "]'s request counter is null.");
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

	private void incrementTotalRequestsInSecond(int second) {
		AtomicInteger counter = totalRequestsInSecond.get(second);
		if (counter != null) {
			counter.incrementAndGet();
		} else {
			logger.warn("Impossible case happended, second[" + second + "]'s request counter is null.");
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
		}
	}

	public void resetRequestsInDayCounter() {
		if (enableDayStats) {
			int day = Calendar.getInstance().get(Calendar.DATE);
			int prev3Sec = day - 10;
			for (int i = 1; i <= 15; i++) {
				int prevSec = prev3Sec - i;
				prevSec = prevSec >= 0 ? prevSec : prevSec + 31;
				AtomicInteger counter = totalRequestsInDay.get(prevSec);
				if (counter != null) {
					counter.set(0);
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
		if (enableMinuteStats) {
			for (int min = 0; min < 60; min++) {
				totalRequestsInMinute.put(min, new AtomicInteger());
			}
		}
		if (enableDayStats) {
			for (int day = 1; day < 32; day++) {
				totalRequestsInDay.put(day, new AtomicInteger());
			}
		}
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("requests-current:").append(requests).append(",requests-currentsecond:")
				.append(getRequestsInCurrentSecond()).append(",requests-lastsecond:").append(getRequestsInLastSecond());
		if (enableMinuteStats) {
			sb.append(",requests-lastminute:").append(getRequestsInLastMinute());
		}
		if (enableDayStats) {
			sb.append(",requests-lastday:").append(getRequestsInLastDay()).append(",requests-today:")
					.append(getRequestsInToday()).toString();
		}
		if (enableMethodStats) {
			sb.append(",methods:[").append(requestCall).append("]");
		}

		return sb.toString();
	}
}
