package com.dianping.pigeon.remoting.provider.process.statistics;

import java.io.Serializable;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.remoting.common.domain.InvocationRequest;

public class ProviderCapacityBucket implements Serializable {
	private static final Logger logger = LoggerLoader.getLogger(ProviderCapacityBucket.class);

	private AtomicInteger requests = new AtomicInteger();

	private Map<Integer, AtomicInteger> totalRequestsInSecond = new HashMap<Integer, AtomicInteger>();

	public ProviderCapacityBucket(String address) {
		preFillData();
	}

	public void flowIn(InvocationRequest request) {
		Calendar now = Calendar.getInstance();
		requests.incrementAndGet();
		incrementTotalRequestsInSecond(now.get(Calendar.SECOND));
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

	private void preFillData() {
		for (int sec = 0; sec < 60; sec++) {
			totalRequestsInSecond.put(sec, new AtomicInteger());
		}
	}

	public String toString() {
		return new StringBuilder().append("requests-current:").append(requests).append(",requests-lastsecond:")
				.append(getRequestsInLastSecond()).toString();
	}
}
