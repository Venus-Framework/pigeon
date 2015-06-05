package com.dianping.pigeon.governor.status;

import java.io.Serializable;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CapacityBucket implements Serializable {
	private static final Logger logger = LogManager.getLogger(CapacityBucket.class);

	private AtomicInteger requests = new AtomicInteger();

	private Map<Integer, AtomicInteger> totalRequestsInSecond = new HashMap<Integer, AtomicInteger>();

	public CapacityBucket(String address) {
		preFillData();
	}

	public void flowIn() {
		Calendar now = Calendar.getInstance();
		requests.incrementAndGet();
		incrementTotalRequestsInSecond(now.get(Calendar.SECOND));
	}

	public void flowOut() {
		requests.decrementAndGet();
	}

	public int getCurrentRequests() {
		return requests.get();
	}

	public Map<Integer, AtomicInteger> getRotalRequestsInSecond() {
		return totalRequestsInSecond;
	}

	public int getRequestsInLastSecond() {
		int lastSecond = Calendar.getInstance().get(Calendar.SECOND) - 1;
		lastSecond = lastSecond >= 0 ? lastSecond : lastSecond + 60;
		AtomicInteger counter = totalRequestsInSecond.get(lastSecond);
		return counter != null ? counter.intValue() : 0;
	}

	private void incrementTotalRequestsInSecond(int second) {
		AtomicInteger counter = totalRequestsInSecond.get(second);
		if (counter != null) {
			counter.incrementAndGet();
		} else {
			logger.error("Impossible case happended, second[" + second + "]'s request counter is null.");
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

	private void preFillData() {
		for (int sec = 0; sec < 60; sec++) {
			totalRequestsInSecond.put(sec, new AtomicInteger());
		}
	}

	public String toString() {
		return "current requests:" + requests + ", requests in last second:" + getRequestsInLastSecond();
	}
}
