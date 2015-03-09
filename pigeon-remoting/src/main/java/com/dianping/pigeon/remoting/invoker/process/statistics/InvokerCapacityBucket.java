package com.dianping.pigeon.remoting.invoker.process.statistics;

import java.io.Serializable;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

import com.dianping.pigeon.config.ConfigManagerLoader;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.remoting.common.domain.InvocationRequest;

public class InvokerCapacityBucket implements Serializable {
	private static final Logger logger = LoggerLoader.getLogger(InvokerCapacityBucket.class);

	private AtomicInteger requests = new AtomicInteger();

	private Map<Integer, AtomicInteger> totalRequestsInSecond = new HashMap<Integer, AtomicInteger>();

	private Map<Integer, AtomicInteger> totalRequestsInDay = new HashMap<Integer, AtomicInteger>();

	private Map<Integer, AtomicInteger> totalRequestsInMinute = new HashMap<Integer, AtomicInteger>();

	public static final boolean enableDayStats = ConfigManagerLoader.getConfigManager().getBooleanValue(
			"pigeon.invokerstat.day.enable", true);

	public static final boolean enableMinuteStats = ConfigManagerLoader.getConfigManager().getBooleanValue(
			"pigeon.invokerstat.minute.enable", true);

	public InvokerCapacityBucket(String address) {
		preFillData();
	}

	public void flowIn(InvocationRequest request) {
		Calendar now = Calendar.getInstance();
		requests.incrementAndGet();
		incrementTotalRequestsInSecond(now.get(Calendar.SECOND));
		if (enableDayStats) {
			incrementTotalRequestsInDay(now.get(Calendar.DATE));
		}
		if (enableMinuteStats) {
			incrementTotalRequestsInMinute(now.get(Calendar.MINUTE));
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

	public int getRequestsInLastSecond() {
		int lastSecond = Calendar.getInstance().get(Calendar.SECOND) - 1;
		lastSecond = lastSecond >= 0 ? lastSecond : lastSecond + 60;
		AtomicInteger counter = totalRequestsInSecond.get(lastSecond);
		return counter != null ? counter.intValue() : 0;
	}

	public int getRequestsInLastMinute() {
		int lastMinute = Calendar.getInstance().get(Calendar.MINUTE) - 1;
		lastMinute = lastMinute >= 0 ? lastMinute : lastMinute + 60;
		AtomicInteger counter = totalRequestsInMinute.get(lastMinute);
		return counter != null ? counter.intValue() : 0;
	}

	public int getRequestsInLastDay() {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -1);
		int lastDay = cal.get(Calendar.DATE);
		AtomicInteger counter = totalRequestsInDay.get(lastDay);
		return counter != null ? counter.intValue() : 0;
	}

	public int getRequestsInToday() {
		Calendar cal = Calendar.getInstance();
		int day = cal.get(Calendar.DATE);
		AtomicInteger counter = totalRequestsInDay.get(day);
		return counter != null ? counter.intValue() : 0;
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

	public void resetRequestsInMinuteCounter() {
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

	private void preFillData() {
		for (int sec = 0; sec < 60; sec++) {
			totalRequestsInSecond.put(sec, new AtomicInteger());
		}
		for (int min = 0; min < 60; min++) {
			totalRequestsInMinute.put(min, new AtomicInteger());
		}
		for (int day = 0; day < 32; day++) {
			totalRequestsInDay.put(day, new AtomicInteger());
		}
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("requests-current:").append(requests).append(",requests-lastsecond:")
				.append(getRequestsInLastSecond());
		if (enableMinuteStats) {
			sb.append(requests).append(",requests-lastminute:").append(getRequestsInLastMinute());
		}
		if (enableDayStats) {
			sb.append(",requests-lastday:").append(getRequestsInLastDay()).append(",requests-today:")
					.append(getRequestsInToday()).toString();
		}
		return sb.toString();
	}
}
