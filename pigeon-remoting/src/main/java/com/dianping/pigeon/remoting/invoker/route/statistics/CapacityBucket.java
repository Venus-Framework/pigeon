package com.dianping.pigeon.remoting.invoker.route.statistics;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.logging.log4j.Logger;

import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.common.util.Constants;

@SuppressWarnings("serial")
public class CapacityBucket implements Serializable {
	private static final Logger logger = LoggerLoader.getLogger(CapacityBucket.class);

	private String address;
	private volatile float capacity = 0f;
	private Set<Long> requestSeqs = Collections.newSetFromMap(new ConcurrentHashMap<Long, Boolean>());
	// 桶中某些容量因某些意外因素导致没有释放, 可以用这个进行Check
	public Map<Long, Object[]> requestSeqDetails = new ConcurrentHashMap<Long, Object[]>();

	private AtomicLong totalRequest = new AtomicLong(); // total request
														// send
	private AtomicLong onewayRequest = new AtomicLong(); // total oneway
															// request send

	private Map<Integer, AtomicInteger> totalRequestInSecond = new ConcurrentHashMap<Integer, AtomicInteger>();

	private Lock capacityLock = new ReentrantLock();

	public CapacityBucket(String address) {
		this.address = address;
		preFillData(); // 为了更优地计算每秒请求数, 使用预填数据代替同步数据结构
	}

	public void flowIn(InvocationRequest request) {
		Calendar now = Calendar.getInstance();
		totalRequest.incrementAndGet();
		if (request.getCallType() == Constants.CALLTYPE_NOREPLY) {
			onewayRequest.incrementAndGet();
		}
		incrementTotalRequestInSecond(now.get(Calendar.SECOND));
		if (request.getCallType() == Constants.CALLTYPE_REPLY) {
			refreshCapacity(1f);
			this.requestSeqs.add(request.getSequence());
			this.requestSeqDetails.put(request.getSequence(),
					new Object[] { now.getTimeInMillis(), request.getTimeout(), 1f });
		}
	}

	public void flowOut(InvocationRequest request) {
		if (request.getCallType() == Constants.CALLTYPE_REPLY) {
			flowOut(request.getSequence(), 1f);
		}
	}

	public void flowOut(long requestSeq, Float flow) {
		if (requestSeqs.remove(requestSeq) && flow != null) {
			refreshCapacity(-1 * flow);
		}
		requestSeqDetails.remove(requestSeq);
	}

	public int getLastSecondRequest() {
		int lastSecond = Calendar.getInstance().get(Calendar.SECOND) - 1;
		lastSecond = lastSecond >= 0 ? lastSecond : lastSecond + 60;
		AtomicInteger counter = totalRequestInSecond.get(lastSecond);
		return counter != null ? counter.intValue() : 0;
	}

	private void incrementTotalRequestInSecond(int second) {
		AtomicInteger counter = totalRequestInSecond.get(second);
		if (counter != null) {
			counter.incrementAndGet();
		} else {
			logger.error("Impossible case happended, second[" + second + "]'s request counter is null.");
		}
	}

	public void refreshCapacity(float addition) {
		capacityLock.lock();
		try {
			this.capacity += addition;
		} finally {
			capacityLock.unlock();
		}
	}

	/**
	 * 重置过期的每秒请求数计数器
	 */
	public void resetRequestInSecondCounter() {
		int second = Calendar.getInstance().get(Calendar.SECOND);
		int prev3Sec = second - 10;
		for (int i = 1; i <= 30; i++) {
			int prevSec = prev3Sec - i;
			prevSec = prevSec >= 0 ? prevSec : prevSec + 60;
			AtomicInteger counter = totalRequestInSecond.get(prevSec);
			if (counter != null) {
				counter.set(0);
			}
		}
	}

	private void preFillData() {
		for (int sec = 0; sec < 60; sec++) {
			totalRequestInSecond.put(sec, new AtomicInteger());
		}
	}

	public String getAddress() {
		return address;
	}

	public float getCapacity() {
		return capacity;
	}

	public AtomicLong getTotalRequest() {
		return totalRequest;
	}

	public AtomicLong getOnewayRequest() {
		return onewayRequest;
	}
}
